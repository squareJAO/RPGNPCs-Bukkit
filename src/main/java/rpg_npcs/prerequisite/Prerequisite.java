package rpg_npcs.prerequisite;

import org.bukkit.entity.Player;

import rpg_npcs.RpgTrait;

public abstract class Prerequisite {
	public abstract boolean isMet(Player player, RpgTrait npc);
}
