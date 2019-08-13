package rpg_npcs.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rpg_npcs.logging.Log;
import rpg_npcs.logging.Logged;

public class StateFactory {
	private final SupportedStateTypeRecords types;
	private final SupportedStateScopeRecords scopes;
	
	public StateFactory(SupportedStateTypeRecords types, SupportedStateScopeRecords scopes) {
		this.types = types;
		this.scopes = scopes;
	}
	
	public Logged<State<?>> makeState(String name, String typeName, String scopesString, String defaultValueString, String uuid) {
		Log log = new Log();
		
		// Resolve scope names
		List<StateScope> scopeProviders = new ArrayList<StateScope>();
		if (scopesString != null && scopesString != "") {
			String[] scopeStrings = scopesString.split(" ");
			Arrays.sort(scopeStrings);
			for (String string : scopeStrings) {
				StateScope scope = scopes.get(string); 
				
				if (scope == null) {
					log.addError("Unknown scope type: '" + string + "'");
					continue;
				}
				
				scopeProviders.add(scope);
			}
		}
		
		// Resolve type name
		StateType<?> type = types.get(typeName);
		
		if (type == null) {
			log.addError("Unknown state type: '" + typeName + "'");
			return new Logged<State<?>>(null, log);
		}
		
		// Create new type object
		State<?> newState = makeState(log, type, scopeProviders, name, defaultValueString, uuid);
		
		if (newState == null) {
		    log.addError("Error creating state " + name);
		}
		
		return new Logged<State<?>>(newState, log);
	}
	
	private <T> State<T> makeState(Log log, StateType<T> type, List<StateScope> scopeProviders, String name, String defaultValueString, String uuid) {
		T defaultValue = type.valueFromString(defaultValueString);
		
		if (defaultValue == null) {
			log.addError("Unknown " + type.getDataTypeName() + ": " + defaultValueString);
			return null;
		}
		
		return new State<T>(name, uuid, type, scopeProviders, defaultValue);
	}
}
