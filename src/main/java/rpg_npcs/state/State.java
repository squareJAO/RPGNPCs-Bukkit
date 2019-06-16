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
	
	private final String stateUUIDString; // The identifier, unique to this state, used when storing this state in the database
	
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
		this.stateUUIDString = uuid;
		this.type = type;
		
		// Initialise variables
		switch (storageType) {
		case GLOBAL:
    		globalValueCache = defaultValue; // Assume default until requests cleared
    		getStoredGlobalValueAsync();
			break;
		case NPC:
			this.npcValueMapCache = new HashMap<RpgNpc, T>();
			break;
		}
	}
	
	private void getStoredGlobalValueAsync() {
		// load from database using multithreading
		new Thread() {
			@Override
		    public void run() 
		    {
	    		globalValueCache = getStoredValue(stateUUIDString);
		    }
		}.run();
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
    		} else { // If 0 results, store default value
	    		String valueString = type.valueToString(defaultValue);
	    		
	    		String insertCommand = "INSERT INTO global_states (state_uuid, value) VALUES (?, ?)";
		    	PreparedStatement insertStatement = connection.prepareStatement(insertCommand);
		    	
		    	insertStatement.setString(1, uuidString);
		    	insertStatement.setString(2, valueString);
		    	
		    	insertStatement.execute();
		    	
		    	value = defaultValue;
    		}
	    	
	    	connection.close();
	    	
	    	return value;
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
		return defaultValue;
	}
	
	private String makeNpcUuidString(RpgNpc npc) {
		return npc.getUUIDString() + "." + stateUUIDString;
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
			
			// First time a value is requested a blocking request is unavoidable
			return getStoredValue(makeNpcUuidString(npc));
			
		default:
			throw new NotImplementedException();
		}
	}
	
	public void setValue(RpgNpc npc, T value) {
		String valueString = type.valueToString(value);
		
		String storageUUIDString;
		switch (storageType) {
		case GLOBAL:
			storageUUIDString = stateUUIDString;
			break;
		case NPC:
			storageUUIDString = makeNpcUuidString(npc);
			break;
		default:
			throw new NotImplementedException("Storage type " + storageType.toString() + " saving not supported");
		}
		
		// Store in database using multithreading
		new Thread() {
			@Override
		    public void run() 
		    { 
				// Check that an entry exists and that it needs updating
				T storedValue = getStoredValue(storageUUIDString);
				
				if (storedValue.equals(value)) {
					return;
				}
				
		    	try {
		    		Connection connection = RPGNPCsPlugin.sql.connect();
			    	
		    		String command = "UPDATE global_states SET value = ? WHERE state_uuid = ?";
			    	PreparedStatement statement = connection.prepareStatement(command);
			    	
			    	statement.setString(1, valueString);
			    	statement.setString(2, storageUUIDString);
			    	
			    	statement.executeUpdate();
			    	
			    	connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					return;
				}
		    }
		}.run();
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
