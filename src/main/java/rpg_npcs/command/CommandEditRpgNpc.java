package rpg_npcs.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.RpgNpc;
import rpg_npcs.role.Role;
import rpg_npcs.role.RolePropertyMap;
import rpg_npcs.state.State;

public class CommandEditRpgNpc implements TabExecutor {
	private static final String[] SECONDARY_COMMAND_STRINGS = new String[] {"list", "set"};

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> list = new ArrayList<String>();
		
		if (args.length == 1) {
			for (String string : SECONDARY_COMMAND_STRINGS) {
				if (string.startsWith(args[0])) {
					list.add(string);
				}
			}
		} else if (args.length == 2) {
			if (args[0] == "list") {
				for (String string : new String[] {"npcs", "states"}) {
					if (string.startsWith(args[1])) {
						list.add(string);
					}
				}
			} else if (args[0] == "set") {
				for (String string : new String[] {"state"}) {
					if (string.startsWith(args[1])) {
						list.add(string);
					}
				}
			}
		} else if (args.length == 2) {
			if (args[0] == "set") {
				if (args[1] == "state") {
					RPGNPCsPlugin plugin = (RPGNPCsPlugin) Bukkit.getPluginManager().getPlugin("RPGNPCs");
					RpgNpc npc = plugin.getSelectedRpgNpc(sender);
					if (npc != null) {
						RolePropertyMap<State<?>> statesMap = npc.getRole().getAllVisibleStates();
						for (String string : statesMap.keySet()) {
							if (string.contains(args[2])) {
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
		RPGNPCsPlugin plugin = (RPGNPCsPlugin) Bukkit.getPluginManager().getPlugin("RPGNPCs");
		RpgNpc selectedNpc = plugin.getSelectedRpgNpc(sender);
		
		if (args.length == 0) {
			if (selectedNpc == null) {
				sender.sendMessage("No npc selected");
			} else {
				sender.sendMessage("Selected npc: '" + selectedNpc.getName() + "'");
			}
			
			return true;
		} else if (args.length >= 2) {
			if (args[0] == "list") {
				if (args[0] == "npcs") {
					sender.sendMessage("Found " + plugin.npcs.size() + " RpgNpcs:");
					for (RpgNpc npc : plugin.npcs) {
						sender.sendMessage(" - Name:'" + npc.getName() + "', Role: " + npc.getRole().nameString);
					}
					return true;
				}
				if (args[0] == "states") {
					Role role;
					if (selectedNpc == null) {
						sender.sendMessage("No npc selected, global states:");
						role = plugin.roles.get(Role.DEFAULT_ROLE_NAME_STRING);
					} else {
						sender.sendMessage("States for npc '" + selectedNpc.getName() + "': ");
						role = selectedNpc.getRole();
					}
					
					RolePropertyMap<State<?>> statesMap = role.getAllVisibleStates();
					for (String stateName : statesMap.keySet()) {
						sender.sendMessage(" - '" + stateName + "': " + statesMap.get(stateName).toString());
					}
					
					return true;
				}
				
				sender.sendMessage("Invalid argument: " + args[0]);
			} else if (args[0] == "set") {
				if (args[1] == "state") {
					if (args.length <= 3) {
						sender.sendMessage("No variable/value given (not enough arguments)");
					}
					
					if (selectedNpc == null) {
						sender.sendMessage("No npc selected");
						return false;
					}
					
					String variableNameString = args[2];
					String variableValueString = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
					
					RolePropertyMap<State<?>> statesMap = selectedNpc.getRole().getAllVisibleStates();
					if (!statesMap.containsKey(variableNameString)) {
						sender.sendMessage("State " + variableNameString + " not found");
						return false;
					}
					
					State<?> stateToStoreIn = statesMap.get(variableNameString);
					return storeGivenValueInState(sender, variableValueString, selectedNpc, stateToStoreIn);
				}
			}
		}
		
		return false;
	}
	
	private <T> boolean storeGivenValueInState(CommandSender sender, String valueString, RpgNpc npc, State<T> state) {
		T value = state.getType().valueFromString(valueString);
		
		if (value == null) {
			sender.sendMessage("'" + valueString + "' cannot be converted to " + state.getType().getDataTypeName());
			return false;
		}
		
		state.setValue(npc, value);
		return true;
	}
}
