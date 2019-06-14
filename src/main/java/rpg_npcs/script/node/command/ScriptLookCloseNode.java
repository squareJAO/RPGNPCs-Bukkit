package rpg_npcs.script.node.command;

import java.util.logging.Level;

import org.bukkit.Bukkit;

import rpg_npcs.Conversation;
import rpg_npcs.ParsingUtils;

public class ScriptLookCloseNode extends ScriptCommandNode {
	public ScriptLookCloseNode(String argumentString) {
		super(argumentString);
	}

	@Override
	protected void startThisCommand(Conversation conversation, String argumentString) {
		boolean enable = shouldEnable(argumentString);
		conversation.getNpc().lookClose(enable);
	}

	@Override
	protected String getNodeRepresentation() {
		return "<Look Close>";
	}

	private boolean shouldEnable(String argumentString) {
		if (argumentString.length() == 0) {
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
