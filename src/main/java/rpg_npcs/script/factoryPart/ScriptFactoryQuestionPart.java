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
				state.errors.add("Question " + optionString + " has less than 2 parts");
				continue;
			} else if (partStrings.length > 2) {
				state.errors.add("Question " + optionString + " has more than 2 parts");
				continue;
			}
			
			String questionString = partStrings[0];
			String idString = partStrings[1];
			
			// Check valid
			if (!state.doesScriptExist(idString)) {
				state.errors.add("QuestionID " + idString + " isn't defined");
				continue;
			}
			
			optionList.add(new QuestionOption(questionString, state.getScript(idString)));
		}
		
		// Create question node
		ScriptQuestionNode questionNode = new ScriptQuestionNode(plugin, optionList);
		
		// Can't add any text after a question so mark the branch as done
		state.BranchDone = true;
		
		
		return ScriptFactoryPartData.fromNode(questionNode);
	}
	
	@Override
	public boolean shouldTrimSpacesBefore() {
		return true;
	}
}
