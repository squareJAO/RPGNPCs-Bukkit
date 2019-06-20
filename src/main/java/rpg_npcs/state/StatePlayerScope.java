package rpg_npcs.state;

import org.bukkit.OfflinePlayer;

import rpg_npcs.RpgNpc;

public class StatePlayerScope implements StateScope {

	@Override
	public String getUuidString(RpgNpc npc, OfflinePlayer player) {
		return player.getUniqueId().toString();
	}

	@Override
	public String getNameString() {
		return "player";
	}

}
