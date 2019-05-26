package rpg_npcs.script;

import rpg_npcs.script.node.ScriptLinearNode;

public class Script {
	public final String nameString;
	public final ScriptLinearNode initialNode;
	
	public Script(String nameString, ScriptLinearNode initialNode) {
		super();
		this.nameString = nameString;
		this.initialNode = initialNode;
	}
}
