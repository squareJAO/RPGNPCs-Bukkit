package rpg_npcs;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

public class CommandReloadScripts implements TabExecutor {
	RPGNPCsPlugin instance;
	
	public CommandReloadScripts(RPGNPCsPlugin instance) {
		this.instance = instance;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		// No tab completion
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		sender.sendMessage("Reloading npcs...");
		
		// Regenerate the dialogue trees
		ParseLog reloadLog = instance.reloadData();
		
		// Print log
		instance.printLogToConsole(reloadLog);
		sender.sendMessage(reloadLog.getFormattedString());
		sender.sendMessage("Reloaded.");
		
		return true;
	}

}
