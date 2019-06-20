package rpg_npcs.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rpg_npcs.ParseLog;

public class StateFactory {
	public static class StateFactoryReturnData {
		public ParseLog log = new ParseLog();
		public State<?> state = null;
	}
	
	private final SupportedStateTypeRecords types;
	private final SupportedStateScopeRecords scopes;
	
	public StateFactory(SupportedStateTypeRecords types, SupportedStateScopeRecords scopes) {
		this.types = types;
		this.scopes = scopes;
	}
	
	public StateFactoryReturnData makeState(String name, String typeName, String scopesString, String defaultValueString, String uuid) {
		StateFactoryReturnData data = new StateFactoryReturnData();
		
		// Resolve scope names
		List<StateScope> scopeProviders = new ArrayList<StateScope>();
		if (scopesString != null && scopesString != "") {
			String[] scopeStrings = scopesString.split(" ");
			Arrays.sort(scopeStrings);
			for (String string : scopeStrings) {
				StateScope scope = scopes.get(string); 
				
				if (scope == null) {
					data.log.addError("Unknown scope type: '" + string + "'");
					continue;
				}
				
				scopeProviders.add(scope);
			}
		}
		
		// Resolve type name
		StateType<?> type = types.get(typeName);
		
		if (type == null) {
			data.log.addError("Unknown state type: '" + typeName + "'");
			return data;
		}
		
		// Create new type object
		State<?> newState = makeState(data, type, scopeProviders, name, defaultValueString, uuid);
		
		if (newState == null) {
			data.log.addError("Error creating state " + name);
			return data;
		}
		
		data.state = newState;
		
		return data;
	}
	
	private <T> State<T> makeState(StateFactoryReturnData data, StateType<T> type, List<StateScope> scopeProviders, String name, String defaultValueString, String uuid) {
		T defaultValue = type.valueFromString(defaultValueString);
		
		if (defaultValue == null) {
			data.log.addError("Unknown " + type.getDataTypeName() + ": " + defaultValueString);
			return null;
		}
		
		return new State<T>(name, uuid, type, scopeProviders, defaultValue);
	}
}
