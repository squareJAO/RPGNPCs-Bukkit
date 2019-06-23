package rpg_npcs.trigger;

import rpg_npcs.prerequisite.PrerequisiteSet;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class BreakBlockTrigger extends Trigger {
	public BreakBlockTrigger(String nameString, PrerequisiteSet prerequisites, int priority) {
		super(nameString, prerequisites, priority);
	}
	
	@EventHandler
	public void changeBlockEvent(EntityChangeBlockEvent event) {
		if (event.getTo() == Material.AIR && event.getEntityType() == EntityType.PLAYER) {
			triggerAll((Player) event.getEntity());
		}
	}
}
