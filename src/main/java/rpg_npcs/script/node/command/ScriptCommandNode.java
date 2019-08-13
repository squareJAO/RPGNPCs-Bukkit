package rpg_npcs.script.node.command;

import me.clip.placeholderapi.PlaceholderAPI;
import rpg_npcs.Conversation;
import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.prerequisite.PrerequisiteSet;
import rpg_npcs.script.node.ScriptLinearNode;

public abstract class ScriptCommandNode extends ScriptLinearNode {
	private final String arguments;
	private final PrerequisiteSet prerequisiteSet;

	public ScriptCommandNode(String arguments, PrerequisiteSet prerequisiteSet) {
		super();
		
		this.arguments = arguments;
		this.prerequisiteSet = prerequisiteSet;
	}
	
	@Override
	protected final void startThis(Conversation conversation) {
		if (prerequisiteSet.areMet(conversation.getPlayer(), conversation.getNpc())) {
			String finalArguments = arguments;
			
			// Add papi if loaded
			if (RPGNPCsPlugin.getPlaceholderAPI() != null) {
				finalArguments = PlaceholderAPI.setPlaceholders(conversation.getPlayer(), finalArguments);
			}
			
			startThisCommand(conversation, finalArguments);
		}
		
		finished(conversation);
	}
	
	@Override
	public final void stopNode(Conversation conversation) {
		// Stub -> Commands finish immediately
	}
	
	protected abstract void startThisCommand(Conversation conversation, String arguments);
}
