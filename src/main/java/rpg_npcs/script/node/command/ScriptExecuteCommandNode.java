package rpg_npcs.script.node.command;

import org.bukkit.Bukkit;

import rpg_npcs.Conversation;
import rpg_npcs.prerequisite.PrerequisiteSet;

public class ScriptExecuteCommandNode extends ScriptCommandNode{

	public ScriptExecuteCommandNode(String arguments, PrerequisiteSet prerequisiteSet) {
		super(arguments, prerequisiteSet);
	}

	@Override
	protected void startThisCommand(Conversation conversation, String arguments) {
		Bukkit.getServer().dispatchCommand(conversation.getNpc().getEntity(), arguments);
	}
}
