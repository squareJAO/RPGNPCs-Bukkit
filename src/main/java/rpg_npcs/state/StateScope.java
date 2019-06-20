package rpg_npcs.state;

import org.bukkit.OfflinePlayer;

import rpg_npcs.RpgNpc;

public interface StateScope {
	/**
	 * Creates a custom string UUID for the given data such that the UUID stored is the same for cases where the state is shared
	 * E.g. for a global state a constant is returned, for an npc-specific state a UUID dependent only on the npc is returned, etc.
	 * @param npc The npc this state is stored inside of
	 * @param player The player this state is stored with respect to
	 * @return A String UUID unique for each state
	 */
	String getUuidString(RpgNpc npc, OfflinePlayer player);
	
	String getNameString();
}
