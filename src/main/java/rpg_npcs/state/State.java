package rpg_npcs.state;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;

import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.RpgNpc;
import rpg_npcs.role.RoleNamedProperty;

public class State <T> extends RoleNamedProperty {
	private final StorageType storageType; // Global = same for all npcs with a role, Npc = unique to NPCs
	
	private final String uuidString; // The identifier, unique to this state, used when storing this state in the database
	
	private final SupportedStateType<T> type;
	
	private final T defaultValue;
	
	private Map<RpgNpc, T> npcValueMapCache;
	private T globalValueCache;
	
	public enum StorageType {
		GLOBAL,
		NPC
	}
	
	public State(String name, String uuid, SupportedStateType<T> type, StorageType storageType, T defaultValue) {
		super(name);
		
		this.defaultValue = defaultValue;
		this.storageType = storageType;
		this.uuidString = uuid;
		this.type = type;
		
		// Initialise variables
		switch (storageType) {
		case GLOBAL:
    		globalValueCache = defaultValue; // Assume default until requests cleared
			getStoredGlobalValue();
			break;
		case NPC:
			this.npcValueMapCache = new HashMap<RpgNpc, T>();
			break;
		}
	}
	
	private void getStoredGlobalValue() {
		// load from database using multithreading
		new Thread() {
			@Override
		    public void run() 
		    {
		    	try {
		    		Connection connection = RPGNPCsPlugin.sql.connect();
			    	
		    		String selectCommand = "SELECT value FROM global_states WHERE state_uuid = ?";
			    	PreparedStatement selectStatement = connection.prepareStatement(selectCommand);
			    	
			    	selectStatement.setString(1, uuidString);
			    	
			    	ResultSet results = selectStatement.executeQuery();
			    	
			    	if (results.next()) { // If results
						String valueString = results.getString("value");
						T value = type.valueFromString(valueString);
						
						if (value == null) {
							Bukkit.getLogger().log(Level.WARNING, "Could not parse stored state value '" + valueString +
									"' to " + defaultValue.getClass().getCanonicalName());

				    		globalValueCache = defaultValue;
						} else {
							globalValueCache = value;
						}
		    		} else { // If 0 results
			    		globalValueCache = defaultValue;
			    		
			    		String valueString = type.valueToString(defaultValue);
			    		
			    		String insertCommand = "INSERT INTO global_states (state_uuid, value) VALUES (?, ?)";
				    	PreparedStatement insertStatement = connection.prepareStatement(insertCommand);
				    	
				    	insertStatement.setString(1, uuidString);
				    	insertStatement.setString(2, valueString);
				    	
				    	insertStatement.execute();
		    		}
			    	
			    	connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					return;
				}
		    }
		}.run();
	}
	
	public T getValue(RpgNpc npc) {
		switch (storageType) {
		case GLOBAL:
			// Assume value always cached
			return globalValueCache;

		case NPC:
			// Check if value cached
			if (npcValueMapCache.containsKey(npc)) {
				return npcValueMapCache.get(npc);
			}
			
			// Check if value stored in npc database
			Map<String, String> stateMap = npc.getStateMap();
			if (stateMap.containsKey(uuidString)) {
				// Parse & store in cache
				String storedString = stateMap.get(uuidString);
				T storedValue = type.valueFromString(storedString);
				
				if (storedValue != null) {
					npcValueMapCache.put(npc, storedValue);
					
					// Return parsed value
					return storedValue;
				} else {
					Bukkit.getLogger().log(Level.WARNING, "Could not parse stored state value '" + storedString +
							"' to " + defaultValue.getClass().getCanonicalName());
				}
			}
			
			// Return the default value if no other value was found
			return defaultValue;
			
		default:
			throw new NotImplementedException();
		}
	}
	
	public void setValue(RpgNpc npc, T value) {
		switch (storageType) {
		case GLOBAL:
			globalValueCache = value;
			
			// Store in database using multithreading
			new Thread() {
				@Override
			    public void run() 
			    { 
					String valueString = type.valueToString(value);
			    	try {
			    		Connection connection = RPGNPCsPlugin.sql.connect();
				    	
			    		String command = "UPDATE global_states SET value = ? WHERE state_uuid = ?";
				    	PreparedStatement statement = connection.prepareStatement(command);
				    	
				    	statement.setString(1, valueString);
				    	statement.setString(2, uuidString);
				    	
				    	statement.executeUpdate();
				    	
				    	connection.close();
					} catch (SQLException e) {
						e.printStackTrace();
						return;
					}
			    }
			}.run();
			
			return;
		case NPC:
			// Store in cache
			npcValueMapCache.put(npc, value);
			
			// Store in database
			npc.setStateValue(uuidString, type.valueToString(value));
			break;
		default:
			break;
		}
	}
	
	public SupportedStateType<T> getType() {
		return type;
	}
	
	public T getDefaultValue() {
		return defaultValue;
	}
	
	public StorageType getStorageType() {
		return storageType;
	}
	
	public enum ComparisonResult {
		LESS_THAN,
		EQUAL_TO,
		GREATER_THAN,
		UNDEFINED
	}
}
