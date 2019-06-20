package rpg_npcs.state;

import org.bukkit.OfflinePlayer;

import rpg_npcs.RpgNpc;

public class StateWorldScope implements StateScope {

	@Override
	public String getUuidString(RpgNpc npc, OfflinePlayer player) {
		return npc.getEntity().getWorld().getUID().toString();
	}

	@Override
	public String getNameString() {
		return "world";
	}

}
