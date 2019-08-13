package rpg_npcs.trigger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import rpg_npcs.prerequisite.PrerequisiteSet;

public class MoveTrigger extends Trigger {

	public MoveTrigger(String nameString, PrerequisiteSet prerequisites, Integer priority) {
		super(nameString, prerequisites, priority);
	}
	
	@EventHandler
	public void playerMove(PlayerMoveEvent event) {
		triggerAll(event.getPlayer());
	}
}
