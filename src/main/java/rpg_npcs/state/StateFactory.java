package rpg_npcs.state;

import java.math.BigDecimal;

import rpg_npcs.ParseLog;
import rpg_npcs.state.State.StorageType;

public class StateFactory {
	public static class StateFactoryReturnData {
		public ParseLog log = new ParseLog();
		public State<?> state = null;
	}
	
	public static StateFactoryReturnData makeState(String name, String type, String scopeString, Object defaultValue, String uuid) {
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
		
		switch (type.toLowerCase()) {
		case "number":
			BigDecimal defaultBigDecimal = BigDecimal.valueOf(0);
			if (defaultValue != null) {
				if ((defaultValue instanceof Integer) || (defaultValue instanceof Double) || (defaultValue instanceof String)) {
					try {
						defaultBigDecimal = new BigDecimal(defaultValue.toString());
					} catch (NumberFormatException e) {
						data.log.addError("Invalid number: '" + defaultValue + "'");
					}
				} else {
					data.log.addError("Unknown number class: '" + defaultValue.getClass().getCanonicalName() + "'");
				}
			}
			data.state = new NumberState(name, defaultBigDecimal, scope, uuid);
			break;

		default:
			data.log.addError("Unknown state type: '" + type + "'");
			return data;
		}
		
		return data;
	}
}
