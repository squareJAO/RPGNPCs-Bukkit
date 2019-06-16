package rpg_npcs.state;

import rpg_npcs.ParseLog;
import rpg_npcs.state.State.StorageType;

public class StateFactory {
	public static class StateFactoryReturnData {
		public ParseLog log = new ParseLog();
		public State<?> state = null;
	}
	
	private final SupportedStateTypeRecords types;
	
	public StateFactory(SupportedStateTypeRecords types) {
		this.types = types;
	}
	
	public StateFactoryReturnData makeState(String name, String typeName, String scopeString, Object defaultValue, String uuid) {
		StateFactoryReturnData data = new StateFactoryReturnData();
		
		// Resolve scope name
		StorageType scope;
		switch (scopeString.toLowerCase()) {
		case "global":
			scope = StorageType.GLOBAL;
			break;
		case "npc":
			scope = StorageType.NPC;
			break;
		default:
			data.log.addError("Unknown state scope: '" + scopeString + "'");
			return data;
		}
		
		// Resolve type name
		SupportedStateType<?> type = types.get(typeName);
		
		if (type == null) {
			data.log.addError("Unknown state type: '" + typeName + "'");
			return data;
		}
		
		// Create new type object
		State<?> newState = type.createState(name, uuid, scope, defaultValue.toString());
		
		if (newState == null) {
			data.log.addError("Error creating state " + name);
			return data;
		}
		
		data.state = newState;
		
		return data;
	}
}
