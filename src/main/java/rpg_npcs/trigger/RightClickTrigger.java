package rpg_npcs.trigger;

import rpg_npcs.RpgNpc;
import rpg_npcs.RpgTrait;
import rpg_npcs.prerequisite.PrerequisiteSet;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class RightClickTrigger extends Trigger {

	public RightClickTrigger(String nameString, PrerequisiteSet prerequisites, Integer priority) {
		super(nameString, prerequisites, priority);
	}

	@EventHandler
	public void playerRightClick(PlayerInteractEntityEvent event) {
        NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked());
        if (npc == null) {
            return;
        }
        RpgNpc rpgNpc = npc.getTrait(RpgTrait.class);
        if (rpgNpc == null) {
            return;
        }
		trigger(event.getPlayer(), rpgNpc);
	}
}
