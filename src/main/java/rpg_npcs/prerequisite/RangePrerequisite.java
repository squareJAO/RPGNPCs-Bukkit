package rpg_npcs.prerequisite;

import org.bukkit.entity.Player;

import rpg_npcs.RpgNpc;

public class RangePrerequisite extends Prerequisite {
	private final double rangeSquared;
	
	public RangePrerequisite(double range) {
		this.rangeSquared = range * range;
	}

	@Override
	public boolean isMet(Player player, RpgNpc npc) {
		double distanceSquared = player.getLocation().distanceSquared(npc.getEntity().getLocation());
		
		return distanceSquared <= rangeSquared;
	}

}
