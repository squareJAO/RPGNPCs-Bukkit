package rpg_npcs.trigger;

import java.util.Collection;

import rpg_npcs.prerequisite.Prerequisite;

public class TriggerFactory {
	public static class TriggerFactoryReturnData {
		public String errorLogString = "";
		public Trigger trigger = null;
	}
	
	public static TriggerFactoryReturnData createPrerequisite(String type, Collection<Prerequisite> prerequisites) {
		TriggerFactoryReturnData returnData = new TriggerFactoryReturnData();

		switch (type.toLowerCase()) {
		case "playermove":
			returnData.trigger = new MoveTrigger(prerequisites);
			break;
		default:
			returnData.errorLogString = "Unrecognised trigger type: '" + type + "'";
			break;
		}
		
		return returnData;
	}
}
