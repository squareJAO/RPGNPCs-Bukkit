package rpg_npcs.script.node.status;

import rpg_npcs.Conversation;
import rpg_npcs.script.node.ScriptNode;

public class ScriptBranchNode extends ScriptNode {
	private final ScriptNode nextNode;
	
	public ScriptBranchNode(ScriptNode nextNode) {
		this.nextNode = nextNode;
	}

	@Override
	protected void startThis(Conversation conversation) {
		finished(nextNode, conversation);
	}

	@Override
	public void stopNode(Conversation conversation) {
		// Stub
	}

	@Override
	protected String getNodeRepresentation() {
		return "<Jump to different node>";
	}

}
