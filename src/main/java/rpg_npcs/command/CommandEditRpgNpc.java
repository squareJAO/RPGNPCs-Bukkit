package rpg_npcs.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.RpgNpc;
import rpg_npcs.logging.Log;
import rpg_npcs.role.Role;
import rpg_npcs.role.RolePropertyMap;
import rpg_npcs.state.State;

public class CommandEditRpgNpc implements TabExecutor {
	private final RPGNPCsPlugin plugin;
	
	public CommandEditRpgNpc(RPGNPCsPlugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> list = new ArrayList<String>();
		
		if (args.length == 1) {
			for (String string : new String[] {"list", "get", "set", "reload"}) {
				if (string.startsWith(args[0])) {
					list.add(string);
				}
			}
			return list;
		}
		
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("list")) {
				for (String string : new String[] {"npcs", "states"}) {
					if (string.startsWith(args[1])) {
						list.add(string);
					}
				}
			} else if (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set")) {
				for (String string : new String[] {"state"}) {
					if (string.startsWith(args[1])) {
						list.add(string);
					}
				}
			}
			
			return list;
		}
		
		RPGNPCsPlugin plugin = RPGNPCsPlugin.getPlugin();
		RpgNpc npc = plugin.getSelectedRpgNpc(sender);
		
		Role role;
		if (npc != null) {
			role = npc.getRole();
		} else {
			role = plugin.roles.get(Role.DEFAULT_ROLE_NAME_STRING);
		}
		
		RolePropertyMap<State<?>> statesMap = role.getAllVisibleStates();
		
		if (args.length == 3) {
			if (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set")) {
				if (args[1].equalsIgnoreCase("state")) {
					for (String string : statesMap.keySet()) {
						if (string.contains(args[2]) || string.equals(args[2])) {
							list.add(string);
						}
					}
					return list;
				}
			}
		}
		
		if (args.length == 4 || args.length == 5) {
			if ((args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set"))
					&& args[1].equalsIgnoreCase("state")
					&& statesMap.keySet().contains(args[2])) {
				
				if (args.length == 4) {
					list.add("for");
					return list;
				} else if (args[3].equalsIgnoreCase("for")) {
					// Add the names of all online players
					Set<String> playerNameSet = Bukkit.getOnlinePlayers()
							.parallelStream().map(p -> p.getName())
							.filter(playerName -> (playerName.contains(args[4]) || playerName.equals(args[4])))
							.collect(Collectors.toSet());
					list.addAll(playerNameSet);
					return list;
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
				sender.sendMessage("Selected npc: '" + selectedNpc.getNPCName() + "'");
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
		case "get":
			if (args.length == 1) {
				sender.sendMessage(ChatColor.RED + "Please give an item to get");
				return false;
			}
			
			switch (args[1].toLowerCase()) {
			case "state":
				return getState(args, selectedNpc, sender);

			default:
				sender.sendMessage("Invalid argument to get: " + args[1]);
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
			if (args.length > 1) {
				sender.sendMessage(ChatColor.RED + "Unused arguments beyond '" + args[0] + "'");
			}
			
			sender.sendMessage("Reloading npcs...");
			
			// Regenerate the dialogue trees
			Log reloadLog;
			try {
				reloadLog = plugin.reload();
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			
			// Print log
			plugin.printLogToConsole(reloadLog);
			sender.sendMessage(reloadLog.getErrors().getFormattedString());
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
			sender.sendMessage(" - Name: " + npc.getNPCName() + "\n    Role: " + npc.getRole().getNameString());
		}
		
		return true;
	}
	
	private boolean listStates(CommandSender sender, RpgNpc selectedNpc) {
		Role role;
		if (selectedNpc == null) {
			sender.sendMessage("No npc selected, global states:");
			role = plugin.roles.get(Role.DEFAULT_ROLE_NAME_STRING);
		} else {
			sender.sendMessage("States for npc '" + selectedNpc.getNPCName() + "': ");
			role = selectedNpc.getRole();
		}
		
		RolePropertyMap<State<?>> statesMap = role.getAllVisibleStates();
		String[] stateNameStrings = statesMap.keySet().toArray(new String[statesMap.size()]);
		Arrays.sort(stateNameStrings);
		for (String stateName : stateNameStrings) {
			State<?> state = statesMap.get(stateName);
			
			String variableTypeString = state.getType().getDataTypeName();
			
			if (state.getScopeProviders().isEmpty()) {
				sender.sendMessage(" - '" + stateName + "' (" + variableTypeString + "): " + state.getValue(null, null));
			} else {
				sender.sendMessage(" - '" + stateName + "' (" + variableTypeString + ")");
			}
		}
		
		return true;
	}
	
	private boolean getState(String[] args, RpgNpc selectedNpc, CommandSender sender) {
		if (args.length <= 2) {
			sender.sendMessage("No state given (not enough arguments)");
			return false;
		}
		
		Role role;
		if (selectedNpc == null) {
			role = plugin.roles.get(Role.DEFAULT_ROLE_NAME_STRING);
		} else {
			role = selectedNpc.getRole();
		}

		String variableNameString = args[2];
		
		OfflinePlayer player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		
		if (args.length >= 4) {
			if (!args[3].equalsIgnoreCase("for")) {
				sender.sendMessage("No state given (too many arguments)");
				return false;
			}
			
			String playerNameString = args[4];
			Optional<? extends Player> playerOptional = Bukkit.getOnlinePlayers().parallelStream()
					.filter(p -> p.getName().equalsIgnoreCase(playerNameString))
					.findFirst();
			
			if (!playerOptional.isPresent()) {
				sender.sendMessage("Cannot find player " + playerNameString);
				return false;
			}
			
			player = playerOptional.get();
		}
		
		RolePropertyMap<State<?>> statesMap = role.getAllVisibleStates();
		if (!statesMap.containsKey(variableNameString)) {
			sender.sendMessage(ChatColor.RED + "State " + variableNameString + " not found");
			return false;
		}
		
		State<?> stateToDisplay = statesMap.get(variableNameString);
		
		if (selectedNpc == null) {
			sender.sendMessage("No npc selected");
			return false;
		}
		
		if (player == null) {
			sender.sendMessage("No player selected");
			return false;
		}

		sender.sendMessage(variableNameString + ": " + stateToDisplay.getValue(selectedNpc, player));
		
		return true;
	}
	
	private boolean setState(String[] args, RpgNpc selectedNpc, CommandSender sender) {
		if (args.length <= 3) {
			sender.sendMessage("No value given (not enough arguments)");
			return false;
		}
		
		Role role;
		if (selectedNpc == null) {
			role = plugin.roles.get(Role.DEFAULT_ROLE_NAME_STRING);
		} else {
			role = selectedNpc.getRole();
		}

		String variableNameString = args[2];
		int expressionStartIndex = 3; // Where the last non-expression argument is
		
		OfflinePlayer player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		
		if (args[3].equalsIgnoreCase("for")) {
			if (args.length <= 4) {
				sender.sendMessage("No player given (not enough arguments)");
				return false;
			}
			
			String playerNameString = args[4];
			Optional<? extends Player> playerOptional = Bukkit.getOnlinePlayers().parallelStream()
					.filter(p -> p.getName().equalsIgnoreCase(playerNameString))
					.findFirst();
			
			if (!playerOptional.isPresent()) {
				sender.sendMessage("Cannot find player " + playerNameString);
				return false;
			}
			
			player = playerOptional.get();
			
			expressionStartIndex = 5;
		}
		
		String expression = String.join(" ", Arrays.copyOfRange(args, expressionStartIndex, args.length));
		
		RolePropertyMap<State<?>> statesMap = role.getAllVisibleStates();
		if (!statesMap.containsKey(variableNameString)) {
			sender.sendMessage(ChatColor.RED + "State " + variableNameString + " not found");
			return false;
		}
		
		State<?> stateToStoreIn = statesMap.get(variableNameString);
		
		if (selectedNpc == null) {
			sender.sendMessage("No npc selected");
			return false;
		}
		
		if (player == null) {
			sender.sendMessage("No player selected");
			return false;
		}
		
		try {
			return storeGivenValueInState(sender, expression, selectedNpc, player, stateToStoreIn);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(ChatColor.RED + "Expression '" + expression + "' cannot be resolved: " + e.getMessage());
			return true;
		}
	}
	
	// Typed function workaround to appease compiler
	private <T> boolean storeGivenValueInState(CommandSender sender, String expression, RpgNpc npc, OfflinePlayer player, State<T> state) throws IllegalArgumentException{
		T value = state.getType().executeTypedExpression(npc, player, expression);
		
		if (value == null) {
			sender.sendMessage(ChatColor.RED + "'" + expression + "' cannot be converted to " + state.getType().getDataTypeName());
			return false;
		}
		
		state.setValue(npc, player, value);
		sender.sendMessage("Stored the value " + value.toString() + " in " + state.getNameString());
		return true;
	}
}
