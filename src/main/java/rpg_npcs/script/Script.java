package rpg_npcs.script;

import rpg_npcs.role.RoleNamedProperty;
import rpg_npcs.script.node.ScriptLinearNode;

public class Script extends RoleNamedProperty {
	public final ScriptLinearNode initialNode;
	
	public Script(String nameString, ScriptLinearNode initialNode) {
		super(nameString);
		this.initialNode = initialNode;
	}
}
