package rpg_npcs.script.node;

import rpg_npcs.Conversation;
import rpg_npcs.ConversationNotRunningException;
import rpg_npcs.RPGNPCsPlugin;

public abstract class ScriptNode {
	protected RPGNPCsPlugin instancingPlugin;
	
	public ScriptNode(RPGNPCsPlugin plugin) {
		instancingPlugin = plugin;
	}
	
	public void startNode(Conversation conversation) {
		// Check conversation hasn't ended
		if (!conversation.isRunning()) {
			throw new ConversationNotRunningException(conversation);
		}
		
		conversation.setCurrentNode(this);
		
		startThis(conversation);
	}
	
	// Called to start changing a bubble
	protected abstract void startThis(Conversation conversation);
	
	// Called when a node has finished changing the bubble
	protected static void finished(ScriptNode nextNode, Conversation conversation) {
		conversation.setCurrentNode(nextNode);
		
		// If next node is null then the conversation is over
		if (nextNode != null) {
			nextNode.startThis(conversation);
		}
	}
	
	// Stops everything that this node is in the process of doing
	public abstract void stopNode(Conversation conversation);
	
	@Override
	public String toString() {
		return this.getNodeRepresentation();
	}
	
	protected abstract String getNodeRepresentation();
}
