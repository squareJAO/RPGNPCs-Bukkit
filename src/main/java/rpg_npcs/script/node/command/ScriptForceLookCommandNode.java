package rpg_npcs.script.node.command;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import rpg_npcs.Conversation;
import rpg_npcs.ParsingUtils;
import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.RpgNpc;
import rpg_npcs.prerequisite.PrerequisiteSet;

public class ScriptForceLookCommandNode extends ScriptCommandNode {
	private static Map<Player, BukkitTask> forcedPlayers = new HashMap<Player, BukkitTask>();

	public ScriptForceLookCommandNode(String arguments, PrerequisiteSet prerequisiteSet) {
		super(arguments, prerequisiteSet);
	}

	@Override
	protected void startThisCommand(Conversation conversation, String argumentString) {
		Player player = conversation.getPlayer();
		RpgNpc npc = conversation.getNpc();
		
		String[] arguments = argumentString.split(" ");
		
		if (arguments.length == 0 || ParsingUtils.isPositive(arguments[0])) {
			
			double speed = 0.08;
			if (arguments.length > 1) {
				try {
					speed = Double.parseDouble(arguments[1]);
				} catch (NumberFormatException e) {
					Bukkit.getLogger().warning("Malformed double: '" + arguments[1] + "'");
				}
			}
			
			if (arguments.length > 2) {
				Bukkit.getLogger().warning("Unused arguments past " + arguments[1] + ": " + argumentString);
			}
			
			// If forcing do nothing
			if (forcedPlayers.containsKey(player)) {
				return;
			}
			
			final double finalSpeed = speed;
			
			BukkitTask lookTask = new BukkitRunnable() {
				
				Location playerLocation = player.getLocation();
				
				@Override
				public void run() {
					if (!conversation.isRunning()) {
						stop(player);
						return;
					}
					
					Vector currentDirection = player.getLocation().getDirection();
					
					Vector npcLocationVector = npc.getEntity().getLocation().toVector();
					Vector playerLocationVector = player.getLocation().toVector();
					Vector aimDirection = npcLocationVector.subtract(playerLocationVector).normalize();
					
					// Lerp
					Vector newDirection = aimDirection.multiply(finalSpeed).add(currentDirection.multiply(1 - finalSpeed));
					playerLocation.setDirection(newDirection);
					player.teleport(playerLocation);
				}
			}.runTaskTimer(RPGNPCsPlugin.getPlugin(), 1, 1);
			
			forcedPlayers.put(player, lookTask);
		} else if (ParsingUtils.isNegative(arguments[0])) {
			if (arguments.length > 1) {
				Bukkit.getLogger().warning("Unused arguments past " + arguments[0] + ": " + argumentString);
			}
			
			// Stop forcing if was being forced
			stop(player);
		} else {
			Bukkit.getLogger().warning("Unknown positive or negative argument for look close: " + arguments);
		}
	}
	
	private void stop(Player player) {
		if (forcedPlayers.containsKey(player)) {
			forcedPlayers.get(player).cancel();
			forcedPlayers.remove(player);
		}
	}
}
