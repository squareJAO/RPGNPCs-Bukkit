package rpg_npcs.script.node;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import rpg_npcs.Conversation;
import rpg_npcs.RPGNPCsPlugin;

public class ScriptPauseNode  extends ScriptLinearNode {
	private final int _delay; // The time to pause
	private Map<Conversation, BukkitTask> _pauseTasks = new HashMap<Conversation, BukkitTask>();
	
	public ScriptPauseNode(RPGNPCsPlugin plugin, int delay) {
		super(plugin);
		
		_delay = delay;
	}

	@Override
	protected void startThis(Conversation conversation) {
		ScriptPauseNode thisConversationPauseNode = this;
		
		// Add a new event to simply trigger the next event after some delay
		BukkitTask currentPause = new BukkitRunnable() {
			public void run() {
				// Clean this up
				thisConversationPauseNode.stopNode(conversation);
				
				// Trigger next event
				thisConversationPauseNode.onFinished(conversation);
			}
		}.runTaskLater(instancingPlugin, _delay);
		
		_pauseTasks.put(conversation, currentPause);
	}

	@Override
	public void stopNode(Conversation conversation) {
		// Stop delay task if there is one queued
		if (_pauseTasks.containsKey(conversation)) {
			_pauseTasks.get(conversation).cancel();
			_pauseTasks.remove(conversation);
		}
	}

	@Override
	protected String getNodeRepresentation() {
		return "<pause of " + _delay + " ticks>";
	}

}
