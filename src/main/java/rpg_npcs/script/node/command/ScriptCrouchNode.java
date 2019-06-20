package rpg_npcs.script.node.command;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import rpg_npcs.Conversation;
import rpg_npcs.ParsingUtils;
import rpg_npcs.prerequisite.PrerequisiteSet;

public class ScriptCrouchNode extends ScriptCommandNode {
	public ScriptCrouchNode(String arguments, PrerequisiteSet prerequisiteSet) {
		super(arguments, prerequisiteSet);
	}

	@Override
	protected void startThisCommand(Conversation conversation, String argumentString) {
		Entity npcEntity = conversation.getNpc().getEntity();
		
		if (npcEntity instanceof Player) {
			((Player) npcEntity).setSneaking(shouldCrouch(argumentString));
		}
	}

	@Override
	protected String getNodeRepresentation() {
		return "<crouch>";
	}
	
	private boolean shouldCrouch(String argumentString) {
		if (argumentString == null || argumentString.length() == 0) {
			return true;
		}
		
		if (ParsingUtils.isPositive(argumentString)) {
			return true;
		}
		
		if (ParsingUtils.isNegative(argumentString)) {
			return false;
		}
		
		Bukkit.getLogger().log(Level.WARNING, "'" + argumentString + "' is not a truthy or falsy value\\nFor: Crouch");
		return true;
	}
}
