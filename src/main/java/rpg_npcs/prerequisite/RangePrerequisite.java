package rpg_npcs.prerequisite;

import org.bukkit.entity.Player;

import rpg_npcs.RpgTrait;

public class RangePrerequisite extends Prerequisite {
	private final double rangeSquared;
	
	public RangePrerequisite(double range) {
		this.rangeSquared = range * range;
	}

	@Override
	public boolean isMet(Player player, RpgTrait npc) {
		double distanceSquared = player.getLocation().distanceSquared(npc.getNPC().getEntity().getLocation());
		
		return distanceSquared <= rangeSquared;
	}

}
