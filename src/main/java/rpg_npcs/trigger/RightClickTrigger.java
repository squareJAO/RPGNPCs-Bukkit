package rpg_npcs.trigger;

import rpg_npcs.RpgNpc;
import rpg_npcs.prerequisite.PrerequisiteSet;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class RightClickTrigger extends Trigger {

	public RightClickTrigger(String nameString, PrerequisiteSet prerequisites, Integer priority) {
		super(nameString, prerequisites, priority);
	}

	@EventHandler
	public void playerRightClick(PlayerInteractEntityEvent event) {
		for (RpgNpc npc : getNpcs()) {
			if (npc.getEntity().equals(event.getRightClicked())) {
				trigger(event.getPlayer(), npc);
				return;
			}
		}
	}
}
