package rpg_npcs.script.factoryPart;

import org.bukkit.Bukkit;

import rpg_npcs.script.Script;
import rpg_npcs.script.ScriptFactoryPartData;
import rpg_npcs.script.ScriptFactoryState;
import rpg_npcs.script.node.ScriptNode;
import rpg_npcs.script.node.status.ScriptBranchNode;
import rpg_npcs.script.node.status.ScriptClearNode;

public class ScriptFactoryStatusPart extends ScriptFactoryPart {

	public ScriptFactoryStatusPart() {
		super('|', '|');
	}

	@Override
	protected ScriptFactoryPartData generateNode(ScriptFactoryState state, String instruction) {
		ScriptNode newNode = null;
		
		if (instruction == "") {
			newNode = new ScriptClearNode();
		} else {
			Bukkit.getLogger().info("Visable scripts: " + String.join(", ", state.getAllScripts().keySet()));
			// Interpret as node to jump to
			if (!state.doesScriptExist(instruction)) {
				return ScriptFactoryPartData.fromError("Unknown script to branch to: " + instruction);
			}
			
			Script script = state.getScript(instruction);
			newNode = new ScriptBranchNode(script);
			state.BranchDone = true;
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
