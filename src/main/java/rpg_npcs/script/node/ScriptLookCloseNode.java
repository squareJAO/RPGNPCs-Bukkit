package rpg_npcs.script.node;

import net.citizensnpcs.trait.LookClose;
import rpg_npcs.Conversation;
import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.SpeechBubble;

public class ScriptLookCloseNode extends ScriptActionNode {
	protected final boolean _enable;
	
	public ScriptLookCloseNode(RPGNPCsPlugin plugin, boolean enable) {
		super(plugin);
		
		_enable = enable;
	}

	@Override
	protected void startThis(Conversation conversation) {
		SpeechBubble bubble = conversation.getSpeechBubble();
		
		boolean lookingClose = getNPC(bubble).getTrait(LookClose.class).toggle();
		if (_enable ^ lookingClose) {
			getNPC(bubble).getTrait(LookClose.class).toggle();
		}
		
		onFinished(conversation);
	}

	@Override
	public void stopNode(Conversation conversation) {
		// Stub
	}

	@Override
	protected String getNodeRepresentation() {
		// TODO Auto-generated method stub
		return _enable ? "<Turn to face nearest player>" : "<Stop facing nearest player>";
	}

}
