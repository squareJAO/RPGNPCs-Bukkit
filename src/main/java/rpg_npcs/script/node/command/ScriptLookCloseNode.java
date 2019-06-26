package rpg_npcs.script.node.command;

import java.util.logging.Level;

import org.bukkit.Bukkit;

import rpg_npcs.Conversation;
import rpg_npcs.ParsingUtils;
import rpg_npcs.prerequisite.PrerequisiteSet;

public class ScriptLookCloseNode extends ScriptCommandNode {
	public ScriptLookCloseNode(String argumentString, PrerequisiteSet prerequisiteSet) {
		super(argumentString, prerequisiteSet);
	}

	@Override
	protected void startThisCommand(Conversation conversation, String argumentString) {
		boolean enable = shouldEnable(argumentString);
		conversation.getNpc().lookClose(enable);
	}

	private boolean shouldEnable(String argumentString) {
		if (argumentString == null || argumentString.length() == 0) {
			return true;
		}
		
		if (ParsingUtils.isPositive(argumentString)) {
			return true;
		}
		
		if (ParsingUtils.isNegative(argumentString)) {
			return false;
		}
		
		Bukkit.getLogger().log(Level.WARNING, "'" + argumentString + "' is not a truthy or falsy value\nFor: LookClose");
		return true;
	}

}
