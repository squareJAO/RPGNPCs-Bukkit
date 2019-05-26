package rpg_npcs.script.node;

import rpg_npcs.Conversation;
import rpg_npcs.RPGNPCsPlugin;

public abstract class ScriptLinearNode extends ScriptNode {
	
	protected ScriptNode _nextNode;

	public ScriptLinearNode(RPGNPCsPlugin plugin) {
		super(plugin);
	}
	
	public void setNextNode(ScriptNode nextNode) {
		_nextNode = nextNode;
	}
	
	@Override
	public String toString() {
		if (_nextNode != null) {
			return this.getNodeRepresentation() + _nextNode.toString();
		}
		
		return this.getNodeRepresentation();
	}
	
	protected void onFinished(Conversation conversation) {
		super.finished(_nextNode, conversation);
	}
}
