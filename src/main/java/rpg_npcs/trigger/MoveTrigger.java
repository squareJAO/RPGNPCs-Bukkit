package rpg_npcs.trigger;

import java.util.Collection;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import rpg_npcs.RpgNpc;
import rpg_npcs.prerequisite.Prerequisite;

public class MoveTrigger extends Trigger {

	public MoveTrigger(String nameString, Collection<Prerequisite> prerequisites, int priority) {
		super(nameString, prerequisites, priority);
	}
	
	@EventHandler
	public void playerMove(PlayerMoveEvent event) {
		// Check wasn't just a head move
		if (event.getFrom().distanceSquared(event.getTo()) < (0.00001 * 0.00001)) {
			return;
		}
		
		for (RpgNpc npc : npcScripts.keySet()) {
			if (npc.isSpawned() && npc.getEntity().getWorld() == event.getPlayer().getWorld()) {
				trigger(event.getPlayer(), npc);
			}
		}
	}
}
