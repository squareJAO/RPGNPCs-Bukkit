package rpg_npcs.prerequisite;

import org.bukkit.entity.Player;

import rpg_npcs.RpgNpc;

public abstract class Prerequisite {
	public abstract boolean isMet(Player player, RpgNpc npc);
}
