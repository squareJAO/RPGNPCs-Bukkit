package rpg_npcs.state;

import org.bukkit.OfflinePlayer;

import rpg_npcs.RpgNpc;

public class StateNpcScope implements StateScope {

	@Override
	public String getUuidString(RpgNpc npc, OfflinePlayer player) {
		return npc.getUUIDString();
	}

	@Override
	public String getNameString() {
		return "npc";
	}

}
