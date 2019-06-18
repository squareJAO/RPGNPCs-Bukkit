package rpg_npcs.trigger;

import java.util.Collection;

import rpg_npcs.ParseLog;
import rpg_npcs.prerequisite.Prerequisite;

public class TriggerFactory {
	public static class TriggerFactoryReturnData {
		public ParseLog log = new ParseLog();
		public Trigger trigger = null;
	}
	
	public TriggerFactoryReturnData createTrigger(String type, String name, Collection<Prerequisite> prerequisites, int priority) {
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
