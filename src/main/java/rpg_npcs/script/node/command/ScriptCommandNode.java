package rpg_npcs.script.node.command;

import me.clip.placeholderapi.PlaceholderAPI;
import rpg_npcs.Conversation;
import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.script.node.ScriptLinearNode;

public abstract class ScriptCommandNode extends ScriptLinearNode {
	private final String arguments;

	public ScriptCommandNode(String arguments) {
		super();
		
		this.arguments = arguments;
	}
	
	@Override
	protected final void startThis(Conversation conversation) {
		String finalArguments = arguments;
		
		// Add papi if loaded
		if (RPGNPCsPlugin.hasPlaceholderAPI()) {
			finalArguments = PlaceholderAPI.setPlaceholders(conversation.getPlayer(), finalArguments);
		}
		
		startThisCommand(conversation, finalArguments);
		onFinished(conversation);
	}
	
	@Override
	public final void stopNode(Conversation conversation) {
		// Stub -> Commands finish immediately
	}
	
	protected abstract void startThisCommand(Conversation conversation, String arguments);
}
