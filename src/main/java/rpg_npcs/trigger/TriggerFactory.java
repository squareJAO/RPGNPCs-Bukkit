package rpg_npcs.trigger;

import java.util.Collection;

import rpg_npcs.prerequisite.Prerequisite;

public class TriggerFactory {
	public static class TriggerFactoryReturnData {
		public String errorLogString = "";
		public Trigger trigger = null;
	}
	
	public static TriggerFactoryReturnData createTrigger(String type, Collection<Prerequisite> prerequisites, int priority) {
		TriggerFactoryReturnData returnData = new TriggerFactoryReturnData();

		switch (type.toLowerCase()) {
		case "playermove":
			returnData.trigger = new MoveTrigger(prerequisites, priority);
			break;
		default:
			returnData.errorLogString = "Unrecognised trigger type: '" + type + "'";
			break;
		}
		
		return returnData;
	}
}
