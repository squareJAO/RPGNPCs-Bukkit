package rpg_npcs.script.node.command;

import org.bukkit.Bukkit;

import conj.Shop.control.Manager;
import conj.Shop.data.Page;
import rpg_npcs.Conversation;
import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.prerequisite.PrerequisiteSet;

public class ScriptOpenShopCommandNode extends ScriptCommandNode {

	public ScriptOpenShopCommandNode(String arguments, PrerequisiteSet prerequisiteSet) {
		super(arguments, prerequisiteSet);
	}

	@Override
	protected void startThisCommand(Conversation conversation, String arguments) {
		if (!RPGNPCsPlugin.hasShop()) {
			Bukkit.getLogger().warning("Shop plugin for open shop command not found");
			return;
		}
		
		Page page = Manager.get().getPage(arguments);
		
		if (page == null) {
			Bukkit.getLogger().warning("Shop page " + arguments + " not found");
			return;
		}
		
		page.openPage(conversation.getPlayer());
	}
}
