package rpg_npcs.script.factoryPart;

import java.util.LinkedList;
import java.util.List;

import rpg_npcs.script.ScriptFactoryPartData;
import rpg_npcs.script.ScriptFactoryState;
import rpg_npcs.script.node.QuestionOption;
import rpg_npcs.script.node.ScriptQuestionNode;

public class ScriptFactoryQuestionPart extends ScriptFactoryPart {
	public ScriptFactoryQuestionPart() {
		super('<', '>');
	}

	@Override
	protected ScriptFactoryPartData generateNode(ScriptFactoryState state, String instruction) {
		// Get separate questions
		String[] optionStrings = instruction.split("\\|");
		List<QuestionOption> optionList = new LinkedList<QuestionOption>();
		
		// Populate question options
		for (String optionString : optionStrings) {
			String[] partStrings = optionString.split(":");
			
			// Check data validity
			if (partStrings.length < 2) {
				return ScriptFactoryPartData.fromError("Question " + optionString + " has less than 2 parts");
			} else if (partStrings.length > 2) {
				return ScriptFactoryPartData.fromError("Question " + optionString + " has more than 2 parts");
			}
			
			String questionString = partStrings[0];
			String idString = partStrings[1];
			
			// Check valid
			if (!state.doesScriptExist(idString)) {
				return ScriptFactoryPartData.fromError("QuestionID " + idString + " isn't defined");
			}
			
			optionList.add(new QuestionOption(questionString, state.getScript(idString)));
		}
		
		// Create question node
		ScriptQuestionNode questionNode = new ScriptQuestionNode(optionList);
		
		
		return ScriptFactoryPartData.fromNode(questionNode);
	}
	
	@Override
	public boolean shouldTrimSpacesBefore() {
		return true;
	}
}
