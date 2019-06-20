package rpg_npcs.state;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.RpgNpc;
import rpg_npcs.role.RoleNamedProperty;

public class State <T> extends RoleNamedProperty {
	public enum ComparisonResult {
		LESS_THAN,
		EQUAL_TO,
		GREATER_THAN,
		UNDEFINED
	}
	
	private final String stateUUIDString; // The identifier, unique to this state, used when storing this state in the database
	private final StateType<T> type;
	private final List<StateScope> scopeProviders;
	private final T defaultValue;
	private final Map<String, T> valueCache;
	
	public State(String name, String uuid, StateType<T> type, List<StateScope> scopeProviders, T defaultValue) {
		super(name);
		
		this.defaultValue = defaultValue;
		this.stateUUIDString = uuid;
		this.type = type;
		this.scopeProviders = scopeProviders;
		
		// Initialise cache
		valueCache = new HashMap<String, T>();
	}
	
	private String getUuidString(RpgNpc npc, OfflinePlayer player) {
		String scopeUUIDString = stateUUIDString;
		
		for (StateScope stateScope : scopeProviders) {
			scopeUUIDString += "&" + stateScope.getNameString() + "=" + stateScope.getUuidString(npc, player);
		}
		
		return scopeUUIDString;
	}
	
	public StateType<T> getType() {
		return type;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}
	
	public List<StateScope> getScopeProviders() {
		return scopeProviders;
	}
	
	public T getValue(RpgNpc npc, OfflinePlayer player) {
		String uuidString = getUuidString(npc, player);
		
		if (valueCache.containsKey(uuidString)) {
			return valueCache.get(uuidString);
		}
		
		T cachedT = getStoredValue(uuidString);
		
		// Collections shouldn't be modified in gets in case of async so add a task to update the cache
		new BukkitRunnable() {
			@Override
			public void run() {
				// Check for other events having set a value in the meantime
				if (valueCache.containsKey(uuidString)) {
					return;
				}
				
				setValue(npc, player, cachedT);
			}
		}.runTaskLater(RPGNPCsPlugin.getPlugin(), 1);
		
		return cachedT;
	}
	
	public void setValue(RpgNpc npc, OfflinePlayer player, T value) {
		String uuidString = getUuidString(npc, player);

		valueCache.put(uuidString, value);
		
		// Store in database using multithreading
		setStoredValueAsync(uuidString, value);
	}
	
	private T getStoredValue(String uuidString) {
		T value;
		
    	try {
    		Connection connection = RPGNPCsPlugin.sql.connect();
	    	
    		String selectCommand = "SELECT value FROM global_states WHERE state_uuid = ?";
	    	PreparedStatement selectStatement = connection.prepareStatement(selectCommand);
	    	
	    	selectStatement.setString(1, uuidString);
	    	
	    	ResultSet results = selectStatement.executeQuery();
	    	
	    	if (results.next()) { // If results
				String valueString = results.getString("value");
				value = type.valueFromString(valueString);
				
				if (value == null) {
					Bukkit.getLogger().log(Level.WARNING, "Could not parse stored state value '" + valueString +
							"' to " + defaultValue.getClass().getCanonicalName());

					value = defaultValue;
				}
    		} else { // If 0 results, return default value
		    	value = defaultValue;
    		}
	    	
	    	connection.close();
	    	
	    	return value;
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
		return defaultValue;
	}
	
	private void setStoredValueAsync(String uuidString, T value) {
		new Thread() {
			@Override
		    public void run() 
		    {
				String valueString = type.valueToString(value);
				
		    	try {
		    		Connection connection = RPGNPCsPlugin.sql.connect();
			    	
		    		String command = "INSERT OR REPLACE INTO global_states (state_uuid, value) VALUES (?, ?)";
			    	PreparedStatement statement = connection.prepareStatement(command);

			    	statement.setString(1, uuidString);
			    	statement.setString(2, valueString);
			    	
			    	statement.executeUpdate();
			    	
			    	connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					return;
				}
		    }
		}.run();
	}
}
