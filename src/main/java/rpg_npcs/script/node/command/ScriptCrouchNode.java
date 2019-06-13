package rpg_npcs.script.node;

import org.bukkit.entity.Player;

import rpg_npcs.Conversation;

public class ScriptCrouchNode extends ScriptActionNode {
	protected final boolean _crouch;

	public ScriptCrouchNode(boolean crouch) {
		super();
		
		_crouch = crouch;
	}

	@Override
	protected void startThis(Conversation conversation) {
		Player player = getPlayer(conversation.getSpeechBubble());
		
		if (player != null) {
			player.setSneaking(_crouch);
		}
		
		onFinished(conversation);
	}

	@Override
	public void stopNode(Conversation conversation) {
		// Stubbington haha love you jjoe xxxxxx
	}

	@Override
	protected String getNodeRepresentation() {
		return _crouch ? "<crouch>" : "<uncrouch>";
	}

}
