package rpg_npcs.trigger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import rpg_npcs.prerequisite.PrerequisiteSet;

public class MoveTrigger extends Trigger {

	public MoveTrigger(String nameString, PrerequisiteSet prerequisites, int priority) {
		super(nameString, prerequisites, priority);
	}
	
	@EventHandler
	public void playerMove(PlayerMoveEvent event) {
		// Check wasn't just a head move
		if (event.getFrom().distanceSquared(event.getTo()) < 0.00001) {
			return;
		}
		
		triggerAll(event.getPlayer());
	}
}
