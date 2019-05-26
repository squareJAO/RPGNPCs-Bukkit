package rpg_npcs;

import org.bukkit.entity.Player;

import net.citizensnpcs.api.npc.NPC;
import rpg_npcs.script.Script;
import rpg_npcs.script.node.ScriptNode;

public class Conversation {
	private Player _player;
	private NPC _npc;
	private SpeechBubble _bubble;
	private boolean _isRunning;
	private ScriptNode _currentNode;
	
	public Conversation(SpeechBubble bubble, Player player, NPC npc) {
		_player = player;
		_npc = npc;
		_bubble = bubble;
		_isRunning = false;
	}
	
	public boolean isPlayer(Player player) {
		return this._player == player;
	}
	
	public Player getPlayer() {
		if (!_isRunning) {
			return null;
		}
		
		return _player;
	}

	public NPC getNpc() {
		if (!_isRunning) {
			return null;
		}
		
		return _npc;
	}
	
	public SpeechBubble getSpeechBubble() {
		if (!_isRunning) {
			return null;
		}
		
		return _bubble;
	}

	public boolean isRunning() {
		return _isRunning;
	}

	/**
	 *  Starts a new conversation
	 */
	public void startConversation(Script script) {
		// Stop any previous conversations
		if (_isRunning) {
			stopConversation();
		}
		
		// Stop currently executing nodes
		ScriptNode currentNode = this.getCurrentNode();
		if (currentNode != null) {
			currentNode.stopNode(this);
		}
		
		// Set data
		_isRunning = true;
		_currentNode = script.initialNode;
		
		// Start conversation
		script.initialNode.startNode(this);
	}

	/**
	 *  Stops the current conversation completely
	 */
	public void stopConversation() {
		// Can't stop an already stopped conversation
		if (!_isRunning) {
			throw new ConversationNotRunningException(this);
		}
		
		// Stop currently executing nodes
		ScriptNode currentNode = this.getCurrentNode();
		currentNode.stopNode(this);
		
		// Clear data
		setCurrentNode(null);
		
		// Clear speech bubble
		_bubble.clearText();
	}

	/**
	 * @return the current node
	 */
	public ScriptNode getCurrentNode() {
		if (!_isRunning) {
			return null;
		}
		
		return _currentNode;
	}

	/**
	 * @param currentNode the node to set as currently executing
	 */
	public void setCurrentNode(ScriptNode currentNode) {
		if (!_isRunning) {
			throw new ConversationNotRunningException(this);
		}
		
		if (currentNode == null) {
			_isRunning = false;
		}
		
		this._currentNode = currentNode;
	}
	
}
