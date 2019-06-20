package rpg_npcs.script.node.status;

import rpg_npcs.Conversation;
import rpg_npcs.prerequisite.PrerequisiteSet;
import rpg_npcs.script.node.ScriptLinearNode;
import rpg_npcs.script.node.ScriptNode;

public class ScriptBranchNode extends ScriptLinearNode {
	private final ScriptNode nodeIfMet;
	private final PrerequisiteSet prerequisiteSet;
	
	public ScriptBranchNode(ScriptNode nodeIfMet, PrerequisiteSet prerequisiteSet) {
		this.nodeIfMet = nodeIfMet;
		this.prerequisiteSet = prerequisiteSet;
	}

	@Override
	protected void startThis(Conversation conversation) {
		if (prerequisiteSet.areMet(conversation.getPlayer(), conversation.getNpc())) {
			finished(nodeIfMet, conversation);
		} else {
			finished(conversation); // Continue on linearly
		}
	}

	@Override
	public void stopNode(Conversation conversation) {
		// Stub
	}

	@Override
	protected String getNodeRepresentation() {
		return "<Jump to different node>";
	}

}
