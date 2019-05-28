package rpg_npcs.script.factoryPart;

import rpg_npcs.script.ScriptFactoryPartData;
import rpg_npcs.script.ScriptFactoryState;
import rpg_npcs.script.node.ScriptClearNode;
import rpg_npcs.script.node.ScriptLinearNode;

public class ScriptFactoryStatePart extends ScriptFactoryPart {

	public ScriptFactoryStatePart() {
		super('|', '|');
	}

	@Override
	protected ScriptFactoryPartData generateNode(ScriptFactoryState state, String instruction) {
		ScriptLinearNode newNode = null;
		
		// Parse command
		switch (instruction) {
		case "": // Clear
			newNode = new ScriptClearNode();
			break;
		default:
			return ScriptFactoryPartData.fromError("Unknown state: '" + instruction + "'");
		}
		
		return ScriptFactoryPartData.fromNode(newNode);
	}
	
	@Override
	public boolean shouldTrimSpacesBefore() {
		return true;
	}
	
	@Override
	public boolean shouldTrimSpacesAfter() {
		return true;
	}
}
