package rpg_npcs.script.node;

import rpg_npcs.Conversation;
import rpg_npcs.RPGNPCsPlugin;

public class ScriptClearNode extends ScriptLinearNode {

	public ScriptClearNode(RPGNPCsPlugin plugin) {
		super(plugin);
	}

	@Override
	protected void startThis(Conversation conversation) {
		// Clear the bubble's text
		conversation.getSpeechBubble().clearText();
		this.onFinished(conversation);
	}

	@Override
	public void stopNode(Conversation conversation) {
		// Don't need to do anything here, running this node is a one time event so there's nothing to stop
	}

	@Override
	protected String getNodeRepresentation() {
		return "<clear>";
	}

}
