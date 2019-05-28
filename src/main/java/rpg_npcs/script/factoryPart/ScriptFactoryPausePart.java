package rpg_npcs.script.factoryPart;

import rpg_npcs.script.ScriptFactoryPartData;
import rpg_npcs.script.ScriptFactoryState;
import rpg_npcs.script.node.ScriptPauseNode;

public class ScriptFactoryPausePart extends ScriptFactoryPart {
	public ScriptFactoryPausePart() {
		super('~', '~');
	}

	@Override
	protected ScriptFactoryPartData generateNode(ScriptFactoryState state, String instruction) {
		// Parse text to int
		int givenDelay = 0;
		try {
			givenDelay = Integer.parseInt(instruction);
		} catch (NumberFormatException e) {
			return ScriptFactoryPartData.fromError("Given delay '" + instruction + "' is not a valid integer");
		}
		
		if (givenDelay < 0) {
			return ScriptFactoryPartData.fromError("Given delay " + givenDelay + " cannot be negative");
		}
		
		ScriptPauseNode newNode = new ScriptPauseNode(givenDelay);
		return ScriptFactoryPartData.fromNode(newNode);
		
	}

}
