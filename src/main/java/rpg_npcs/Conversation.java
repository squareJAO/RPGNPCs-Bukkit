package rpg_npcs;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import rpg_npcs.script.Script;
import rpg_npcs.script.node.ScriptNode;

public class Conversation {
	private final Player player;
	private final RpgNpc npc;
	private final SpeechBubble bubble;
	private final int conversationPriority;
	public final Plugin instancingPlugin;
	private boolean isRunning;
	private ScriptNode currentNode;
	
	public Conversation(Plugin instancingPlugin, SpeechBubble bubble, Player player, RpgNpc npc, int conversationPriority) {
		this.player = player;
		this.npc = npc;
		this.bubble = bubble;
		isRunning = false;
		this.conversationPriority = conversationPriority;
		this.instancingPlugin = instancingPlugin;
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

	public RpgNpc getNpc() {
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
		currentNode = script;
		
		// Start conversation
		script.startNode(this);
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
