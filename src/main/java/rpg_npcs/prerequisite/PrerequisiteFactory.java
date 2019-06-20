package rpg_npcs.prerequisite;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.NotImplementedException;

import rpg_npcs.ParseLog;
import rpg_npcs.ParserFactorySet;
import rpg_npcs.prerequisite.StatePrerequisite.Comparison;
import rpg_npcs.state.StateType;

public class PrerequisiteFactory {
	public static class PrerequisiteFactoryReturnData {
		public ParseLog log = new ParseLog();
		public Prerequisite prerequisite = null;
	}
	
	private final ParserFactorySet parserFactorySet;
	
	public PrerequisiteFactory(ParserFactorySet parserFactorySet) {
		this.parserFactorySet = parserFactorySet;
	}
	
	public PrerequisiteFactoryReturnData createPrerequisite(String key, String value) {
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
		case "state":
			String valueTypeString = value.split(" ")[0];
			StateType<?> supportedStateType = parserFactorySet.getSupportedStateTypeRecords().get(valueTypeString);
			
			if (supportedStateType == null) {
				returnData.log.addError("Unrecognised state type: '" + valueTypeString + "'");
				break;
			}
			
			// Get expression
			String inequalityString = value.substring(valueTypeString.length() + 1);
			Matcher inequalityMatcher = Pattern.compile("(.+)((?:<|>)=?|==|!=)(.+)").matcher(inequalityString);
			
			if (!inequalityMatcher.matches()) {
				returnData.log.addError("Invalid inequality: '" + inequalityString + "'");
				break;
			}
			
			String lhsExpressionString = inequalityMatcher.group(1);
			String inequalitySignString = inequalityMatcher.group(2);
			String rhsExpressionString = inequalityMatcher.group(3);
			
			StatePrerequisite.Comparison comparison;
			switch (inequalitySignString) {
			case "<":
				comparison = Comparison.LESS_THAN;
				break;
			case "<=":
				comparison = Comparison.LESS_THAN_OR_EQUAL_TO;
				break;
			case "=":
			case "==":
				comparison = Comparison.EQUAL_TO;
				break;
			case ">":
				comparison = Comparison.GREATER_THAN;
				break;
			case ">=":
				comparison = Comparison.GREATER_THAN_OR_EQUAL_TO;
				break;
			case "!=":
				comparison = Comparison.NOT_EQUAL_TO;
				break;

			default:
				throw new NotImplementedException();
			}
			
			returnData.prerequisite = new StatePrerequisite(comparison, supportedStateType, lhsExpressionString, rhsExpressionString);
			
			break;
		default:
			returnData.log.addError("Unrecognised prerequisite key: '" + key + "'");
			break;
		}
		
		return returnData;
	}

	public PrerequisiteSet createPrerequisiteSet(ParseLog log, Map<String, String> prerequisiteDataMap) {
		PrerequisiteSet prerequisites = new PrerequisiteSet();
		
		// Create prerequisites
		log.addInfo(" - prerequisites:");
		for (Entry<String, String> entry : prerequisiteDataMap.entrySet()) {
			// Convert to prerequisite
			PrerequisiteFactoryReturnData returnPrerequisiteData = createPrerequisite(entry.getKey(), entry.getValue());
			
			if (returnPrerequisiteData.prerequisite != null) {
				prerequisites.add(returnPrerequisiteData.prerequisite);
			}
			
			log.addInfo("   - " + entry.getKey() + ": " + entry.getValue());
			log.add(returnPrerequisiteData.log);
		}
		
		return prerequisites;
	}
}
