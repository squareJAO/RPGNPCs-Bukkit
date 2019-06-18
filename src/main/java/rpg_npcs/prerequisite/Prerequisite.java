package rpg_npcs.prerequisite;

import org.bukkit.entity.Player;

import rpg_npcs.RpgNpc;

public interface Prerequisite {
	public boolean isMet(Player player, RpgNpc npc);
}
