package rpg_npcs;

import org.bukkit.entity.Player;

import net.citizensnpcs.api.npc.NPC;
import rpg_npcs.script.Script;
import rpg_npcs.script.node.ScriptNode;

public class Conversation {
	private final Player player;
	private final NPC npc;
	private final SpeechBubble bubble;
	private final int conversationPriority;
	private boolean isRunning;
	private ScriptNode currentNode;
	
	public Conversation(SpeechBubble bubble, Player player, NPC npc, int conversationPriority) {
		this.player = player;
		this.npc = npc;
		this.bubble = bubble;
		isRunning = false;
		this.conversationPriority = conversationPriority;
	}
	
	public int getPriority() {
		return conversationPriority;
	}
	
	public boolean isPlayer(Player player) {
		return this.player == player;
	}
	
	public Player getPlayer() {
		if (!isRunning) {
			return null;
		}
		
		return player;
	}

	public NPC getNpc() {
		if (!isRunning) {
			return null;
		}
		
		return npc;
	}
	
	public SpeechBubble getSpeechBubble() {
		if (!isRunning) {
			return null;
		}
		
		return bubble;
	}

	public boolean isRunning() {
		return isRunning;
	}

	/**
	 *  Starts a new conversation
	 */
	public void startConversation(Script script) {
		// Stop any previous conversations
		if (isRunning) {
			stopConversation();
		}
		
		// Stop currently executing nodes
		ScriptNode currentNode = this.getCurrentNode();
		if (currentNode != null) {
			currentNode.stopNode(this);
		}
		
		// Set data
		isRunning = true;
		currentNode = script.initialNode;
		
		// Start conversation
		script.initialNode.startNode(this);
	}

	/**
	 *  Stops the current conversation completely
	 */
	public void stopConversation() {
		// Can't stop an already stopped conversation
		if (!isRunning) {
			throw new ConversationNotRunningException(this);
		}
		
		// Stop currently executing nodes
		ScriptNode currentNode = this.getCurrentNode();
		currentNode.stopNode(this);
		
		// Clear data
		setCurrentNode(null);
		
		// Clear speech bubble
		bubble.clearText();
	}

	/**
	 * @return the current node
	 */
	public ScriptNode getCurrentNode() {
		if (!isRunning) {
			return null;
		}
		
		return currentNode;
	}

	/**
	 * @param currentNode the node to set as currently executing
	 */
	public void setCurrentNode(ScriptNode currentNode) {
		if (!isRunning) {
			throw new ConversationNotRunningException(this);
		}
		
		if (currentNode == null) {
			isRunning = false;
		}
		
		this.currentNode = currentNode;
	}
	
}
