package rpg_npcs.script.node;

import rpg_npcs.Conversation;

public abstract class ScriptLinearNode extends ScriptNode {
	protected ScriptNode nextNode;

	public ScriptLinearNode() {
		super();
	}
	
	public void setNextNode(ScriptNode nextNode) {
		this.nextNode = nextNode;
	}
	
	protected void finished(Conversation conversation) {
		super.finished(nextNode, conversation);
	}
}
