package rpg_npcs.prerequisite;

import rpg_npcs.ParseLog;

public class PrerequisiteFactory {
	public static class PrerequisiteFactoryReturnData {
		public ParseLog log = new ParseLog();
		public Prerequisite prerequisite = null;
	}
	
	public static PrerequisiteFactoryReturnData createPrerequisite(String key, String value) {
		PrerequisiteFactoryReturnData returnData = new PrerequisiteFactoryReturnData();
		
		switch (key.toLowerCase()) {
		case "range":
			try {
				double range = Double.parseDouble(value);
				returnData.prerequisite = new RangePrerequisite(range);
			} catch (NumberFormatException e) {
				returnData.log.addError("Unrecognised number for Range: '" + value + "'");
			}
			break;
		default:
			returnData.log.addError("Unrecognised prerequisite key: '" + key + "'");
			break;
		}
		
		return returnData;
	}
}
