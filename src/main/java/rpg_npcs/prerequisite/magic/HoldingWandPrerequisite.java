package rpg_npcs.prerequisite.magic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.api.magic.MagicAPI;

import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.RpgNpc;
import rpg_npcs.logging.Log;
import rpg_npcs.logging.Logged;
import rpg_npcs.prerequisite.Prerequisite;

public class HoldingWandPrerequisite implements Prerequisite {
	
	public HoldingWandPrerequisite() {
		if (RPGNPCsPlugin.getMagic() == null) {
			Bukkit.getLogger().warning("Missing Magic plugin for prerequisite isMage!");
		}
	}

	@Override
	public boolean isMet(Player player, RpgNpc npc) {
		Plugin magic = RPGNPCsPlugin.getMagic();
		if (magic == null) {
			return false;
		}
		
		MagicAPI api = (MagicAPI) magic;
		
		return api.isWand(player.getInventory().getItemInMainHand()) ||
			   api.isWand(player.getInventory().getItemInOffHand());
	}
	
	public static Logged<Prerequisite> makePrerequisite(String arguments) {
		return new Logged<Prerequisite>(new HoldingWandPrerequisite(), new Log());
	}
}
