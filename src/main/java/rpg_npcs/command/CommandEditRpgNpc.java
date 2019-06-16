package rpg_npcs.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import net.md_5.bungee.api.ChatColor;
import rpg_npcs.ParseLog;
import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.RpgNpc;
import rpg_npcs.role.Role;
import rpg_npcs.role.RolePropertyMap;
import rpg_npcs.state.State;
import rpg_npcs.state.State.StorageType;

public class CommandEditRpgNpc implements TabExecutor {
	private final RPGNPCsPlugin plugin;
	
	public CommandEditRpgNpc(RPGNPCsPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> list = new ArrayList<String>();
		
		if (args.length == 1) {
			for (String string : new String[] {"list", "set", "reload"}) {
				if (string.startsWith(args[0])) {
					list.add(string);
				}
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("list")) {
				for (String string : new String[] {"npcs", "states"}) {
					if (string.startsWith(args[1])) {
						list.add(string);
					}
				}
			} else if (args[0].equalsIgnoreCase("set")) {
				for (String string : new String[] {"state"}) {
					if (string.startsWith(args[1])) {
						list.add(string);
					}
				}
			}
		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("set")) {
				if (args[1].equalsIgnoreCase("state")) {
					RPGNPCsPlugin plugin = (RPGNPCsPlugin) Bukkit.getPluginManager().getPlugin("RPGNPCs");
					RpgNpc npc = plugin.getSelectedRpgNpc(sender);
					if (npc != null) {
						RolePropertyMap<State<?>> statesMap = npc.getRole().getAllVisibleStates();
						for (String string : statesMap.keySet()) {
							if (string.contains(args[2]) || string.equals(args[2])) {
								list.add(string);
							}
						}
					}
				}
			}
		}
		
		return list;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Get selected npc
		RpgNpc selectedNpc = plugin.getSelectedRpgNpc(sender);
		
		if (args.length == 0) {
			if (selectedNpc == null) {
				sender.sendMessage("No npc selected");
			} else {
				sender.sendMessage("Selected npc: '" + selectedNpc.getName() + "'");
			}
			
			return true;
		}
		
		switch (args[0].toLowerCase()) {
		case "list":
			if (args.length == 1) {
				sender.sendMessage(ChatColor.RED + "Please give an object to list");
				return false;
			}
			
			if (args.length >= 3) {
				sender.sendMessage(ChatColor.RED + "Unused arguments beyond '" + args[1] + "'");
			}
			
			switch (args[1].toLowerCase()) {
			case "npcs":
				return listNPCs(sender);
				
			case "states":
				return listStates(sender, selectedNpc);

			default:
				sender.sendMessage("Invalid argument: " + args[1]);
				return false;
			}
		case "set":
			if (args.length == 1) {
				sender.sendMessage(ChatColor.RED + "Please give an item to set");
				return false;
			}
			
			switch (args[1].toLowerCase()) {
			case "state":
				return setState(args, selectedNpc, sender);

			default:
				sender.sendMessage("Invalid argument to set: " + args[1]);
				return false;
			}
		case "reload":
			if (args.length >= 1) {
				sender.sendMessage(ChatColor.RED + "Unused arguments beyond '" + args[0] + "'");
			}
			
			sender.sendMessage("Reloading npcs...");
			
			// Regenerate the dialogue trees
			ParseLog reloadLog = plugin.reloadData();
			
			// Print log
			plugin.printLogToConsole(reloadLog);
			sender.sendMessage(reloadLog.getFormattedString());
			sender.sendMessage("Reloaded.");
			
			return true;
		default:
			sender.sendMessage("Invalid subcommand: " + args[0]);
			return false;
		}
	}

	private boolean listNPCs(CommandSender sender) {
		sender.sendMessage("Found " + plugin.npcs.size() + " RpgNpc(s)");
		for (RpgNpc npc : plugin.npcs) {
			sender.sendMessage(" - Name: " + npc.getName() + "\n    Role: " + npc.getRole().nameString);
		}
		
		return true;
	}
	
	private boolean listStates(CommandSender sender, RpgNpc selectedNpc) {
		Role role;
		if (selectedNpc == null) {
			sender.sendMessage("No npc selected, global states:");
			role = plugin.roles.get(Role.DEFAULT_ROLE_NAME_STRING);
		} else {
			sender.sendMessage("States for npc '" + selectedNpc.getName() + "': ");
			role = selectedNpc.getRole();
		}
		
		RolePropertyMap<State<?>> statesMap = role.getAllVisibleStates();
		String[] stateNameStrings = statesMap.keySet().toArray(new String[statesMap.size()]);
		Arrays.sort(stateNameStrings);
		for (String stateName : stateNameStrings) {
			State<?> state = statesMap.get(stateName);
			
			String variableTypeString = state.getType().getDataTypeName();
			
			// Get stored value string
			String valueString = "[Select an NPC to see a value]";
			if (selectedNpc == null) {
				if (state.getStorageType() == StorageType.GLOBAL) {
					valueString = state.getValue(null).toString();
				}
			} else {
				valueString = state.getValue(selectedNpc).toString();
			}
			
			sender.sendMessage(" - '" + stateName + "' (" + variableTypeString + "): " + valueString);
		}
		
		return true;
	}
	
	private boolean setState(String[] args, RpgNpc selectedNpc, CommandSender sender) {
		if (args.length <= 3) {
			sender.sendMessage("No variable/value given (not enough arguments)");
			return false;
		}
		
		if (selectedNpc == null) {
			sender.sendMessage("No npc selected");
			return false;
		}
		
		String variableNameString = args[2];
		String expression = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
		
		RolePropertyMap<State<?>> statesMap = selectedNpc.getRole().getAllVisibleStates();
		if (!statesMap.containsKey(variableNameString)) {
			sender.sendMessage(ChatColor.RED + "State " + variableNameString + " not found");
			return false;
		}
		
		State<?> stateToStoreIn = statesMap.get(variableNameString);
		try {
			return storeGivenValueInState(sender, expression, selectedNpc, stateToStoreIn);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + "Expression '" + expression + "' cannot be resolved: " + e.getMessage());
			return true;
		}
	}
	
	// Typed function workaround to appease compiler
	private <T> boolean storeGivenValueInState(CommandSender sender, String expression, RpgNpc npc, State<T> state) throws IllegalArgumentException{
		T value = state.getType().executeTypedExpression(npc, expression);
		
		if (value == null) {
			sender.sendMessage(ChatColor.RED + "'" + expression + "' cannot be converted to " + state.getType().getDataTypeName());
			return false;
		}
		
		state.setValue(npc, value);
		sender.sendMessage("Stored the value " + value.toString() + " in " + state.nameString);
		return true;
	}
}
