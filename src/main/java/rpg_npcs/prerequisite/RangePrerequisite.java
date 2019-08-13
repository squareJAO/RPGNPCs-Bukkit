package rpg_npcs.prerequisite;

import org.bukkit.entity.Player;

import rpg_npcs.RpgNpc;
import rpg_npcs.logging.Log;
import rpg_npcs.logging.LogEntry.MessageType;
import rpg_npcs.logging.Logged;

public class RangePrerequisite implements Prerequisite {
	private final double rangeSquared;
	
	public RangePrerequisite(double range) {
		this.rangeSquared = range * range;
	}

	@Override
	public boolean isMet(Player player, RpgNpc npc) {
		double distanceSquared = player.getLocation().distanceSquared(npc.getEntity().getLocation());
		
		return distanceSquared <= rangeSquared;
	}
	
	public static Logged<Prerequisite> makePrerequisite(String arguments) {
		try {
			double range = Double.parseDouble(arguments);
			return new Logged<Prerequisite>(new RangePrerequisite(range), new Log());
		} catch (NumberFormatException e) {
			return new Logged<Prerequisite>(null, Log.fromString(e.getLocalizedMessage(), MessageType.ERROR));
		}
	}
}
