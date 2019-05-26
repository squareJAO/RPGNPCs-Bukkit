package rpg_npcs.script.node;

import rpg_npcs.script.Script;

public class QuestionOption {
	public final String textString;
	public final Script script;
	
	public QuestionOption(String textString, Script script) {
		this.textString = textString;
		this.script = script;
	}
}
