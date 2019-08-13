package rpg_npcs.prerequisite;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.entity.Player;

import rpg_npcs.RpgNpc;
import rpg_npcs.logging.Logged;

public interface Prerequisite {
	/**
	 * WARNING: This method is only ever called asynchronously so great care should be taken to ensure it doesn't modify anything
	 * @param player The Player that the prerequisite is being tested against
	 * @param npc The NPC that the prerequisite is being tested against
	 * @return True if the prerequisite is met
	 */
	public boolean isMet(Player player, RpgNpc npc);
	
	public static Logged<Prerequisite> makePrerequisite(String arguments) {
		throw new NotImplementedException();
	}
}
