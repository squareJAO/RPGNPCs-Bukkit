package rpg_npcs.script;

import rpg_npcs.Conversation;
import rpg_npcs.role.RoleNamedProperty;
import rpg_npcs.script.node.ScriptLinearNode;

public class Script extends ScriptLinearNode implements RoleNamedProperty {
	private final String nameString;
	
	public Script(String nameString) {
		this.nameString = nameString;
	}

	@Override
	public String getNameString() {
		return nameString;
	}

	@Override
	protected void startThis(Conversation conversation) {
		finished(conversation);
	}

	@Override
	public void stopNode(Conversation conversation) {
		// Stub
	}

	@Override
	protected String getNodeRepresentation() {
		return nameString + ": ";
	}
}
