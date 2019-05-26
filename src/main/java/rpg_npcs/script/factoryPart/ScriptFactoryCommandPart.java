package rpg_npcs.script.factoryPart;

import rpg_npcs.script.ScriptFactoryPartData;
import rpg_npcs.script.ScriptFactoryState;
import rpg_npcs.script.node.ScriptCrouchNode;
import rpg_npcs.script.node.ScriptLinearNode;
import rpg_npcs.script.node.ScriptLookCloseNode;

public class ScriptFactoryCommandPart extends ScriptFactoryPart {
	public ScriptFactoryCommandPart() {
		super('[', ']');
	}

	@Override
	protected ScriptFactoryPartData generateNode(ScriptFactoryState state, String instruction) {
		ScriptLinearNode newNode = null;
		
		// Parse command
		switch (instruction) {
		case "lookclose:true": // Start looking close
			newNode = new ScriptLookCloseNode(plugin, true);
			break;
		case "lookclose:false": // Stop looking close
			newNode = new ScriptLookCloseNode(plugin, false);
			break;
		case "crouch": // Start crouching
			newNode = new ScriptCrouchNode(plugin, true);
			break;
		case "uncrouch": // Stop crouching
			newNode = new ScriptCrouchNode(plugin, false);
			break;
		default:
			return ScriptFactoryPartData.fromError("Unknown command: '" + instruction + "'");
		}
		
		return ScriptFactoryPartData.fromNode(newNode);
	}

}
