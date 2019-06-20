package rpg_npcs.trigger;

import rpg_npcs.ParseLog;
import rpg_npcs.prerequisite.PrerequisiteSet;

public class TriggerFactory {
	public static class TriggerFactoryReturnData {
		public ParseLog log = new ParseLog();
		public Trigger trigger = null;
	}
	
	public TriggerFactoryReturnData createTrigger(String type, String name, PrerequisiteSet prerequisites, int priority) {
		TriggerFactoryReturnData returnData = new TriggerFactoryReturnData();

		switch (type.toLowerCase()) {
		case "playermove":
			returnData.trigger = new MoveTrigger(name, prerequisites, priority);
			break;
		default:
			returnData.log.addError("Unrecognised trigger type: '" + type + "', name '" + name + "'");
			break;
		}
		
		return returnData;
	}
}
