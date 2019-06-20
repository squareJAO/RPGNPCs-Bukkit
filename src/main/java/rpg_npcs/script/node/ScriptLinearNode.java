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
	
	@Override
	public String toString() {
		if (nextNode != null) {
			return this.getNodeRepresentation() + nextNode.toString();
		}
		
		return this.getNodeRepresentation();
	}
	
	protected void finished(Conversation conversation) {
		super.finished(nextNode, conversation);
	}
}
