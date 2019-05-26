package rpg_npcs.prerequisite;

public class PrerequisiteFactory {
	public static class PrerequisiteFactoryReturnData {
		public String errorLogString = "";
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
				returnData.errorLogString = "Unrecognised number for Range: '" + value + "'";
			}
			break;
		default:
			returnData.errorLogString = "Unrecognised prerequisite key: '" + key + "'";
			break;
		}
		
		return returnData;
	}
}
