package rpg_npcs.prerequisite;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import rpg_npcs.RpgNpc;

public class PrerequisiteSet {
	private final Set<Prerequisite> prerequisites;
	
	public PrerequisiteSet() {
		prerequisites = new HashSet<Prerequisite>();
	}
	
	public void add(Prerequisite prerequisite) {
		prerequisites.add(prerequisite);
	}

	public int size() {
		return prerequisites.size();
	}

	/**
	 * Checks if all of the prerequisites for this set are met
	 * @param player The player to check with respect to
	 * @param npc The npc to check with respect to
	 * @return A boolean, true if prerequisites are met
	 */
	public final boolean areMet(Player player, RpgNpc npc) {
		for (Prerequisite prerequisite : prerequisites) {
			if (!prerequisite.isMet(player, npc)) {
				return false;
			}
		}
		
		return true;
	}
}
