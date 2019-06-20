package rpg_npcs.trigger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.RpgNpc;
import rpg_npcs.WeightedSet;
import rpg_npcs.prerequisite.Prerequisite;
import rpg_npcs.role.RoleNamedProperty;
import rpg_npcs.script.Script;

public abstract class Trigger extends RoleNamedProperty implements Listener {
	private final Collection<Prerequisite> prerequisites;
	private final Map<RpgNpc, WeightedSet<Script>> npcScripts;
	private final int priority;
	
	// A set of all players who have triggered this recently or are having prerequisites checked and so
	// should not be triggered again
	private final Set<Player> lockedPlayers = new HashSet<Player>();
	
	public Trigger(String nameString, Collection<Prerequisite> prerequisites, int priority) {
		super(nameString);
		this.npcScripts = new HashMap<RpgNpc, WeightedSet<Script>>();
		this.prerequisites = prerequisites;
		this.priority = priority;
	}
	
	public void registerNPC(RpgNpc npc, WeightedSet<Script> weightedScriptSet) {
		npcScripts.put(npc, weightedScriptSet);
	}
	
	public void unregisterNPC(RpgNpc npc) {
		npcScripts.remove(npc);
	}
	
	public int getPriority() {
		return priority;
	}
	
	public Collection<Prerequisite> getPrerequisites() {
		return prerequisites;
	}
	
	protected void trigger(Player player) {
		// Check if player is locked out
		if (lockedPlayers.contains(player)) {
			return;
		}
		
		lockedPlayers.add(player);
		
		BukkitRunnable prerequisitesCheckTask = new BukkitRunnable() {
			@Override
			public void run() {
				try {
					for (RpgNpc npc : npcScripts.keySet()) {
						if (npc.isSpawned() && npc.getEntity().getWorld() == player.getWorld()
							&& priority > npc.getConversationPriority() && arePrerequisitesMet(player, npc)) {
							// Return to being synchronous
							startConversationSynchronous(player, npc);
						}
					}
				} finally { // Ensure player is unlocked but still propagate exception
					// Unlock player after a short delay
					unlockPlayerSynchronous(player, 10);
				}
			}
		};
		
		// Check prerequisites asynchronously
		prerequisitesCheckTask.runTaskAsynchronously(RPGNPCsPlugin.getPlugin());
	}
	
	private void startConversationSynchronous(Player player, RpgNpc npc) {
		BukkitRunnable startConversationTask = new BukkitRunnable() {
			@Override
			public void run() {
				WeightedSet<Script> scriptSet = npcScripts.get(npc);
				Script script = scriptSet.getRandom();
				
				if (scriptSet.size() == 0) {
					Bukkit.getLogger().warning("Script set is empty");
					return;
				}
				
				if (script == null) {
					Bukkit.getLogger().warning("Script was null");
					return;
				}
				
				npc.startConversation(script, player, priority);
			}
		};
		
		startConversationTask.runTaskLater(RPGNPCsPlugin.getPlugin(), 1);
	}
	
	private void unlockPlayerSynchronous(Player player, int delay) {
		BukkitRunnable unlockPlayerTask = new BukkitRunnable() {
			@Override
			public void run() {
				lockedPlayers.remove(player);
			}
		};
		
		unlockPlayerTask.runTaskLater(RPGNPCsPlugin.getPlugin(), delay);
	}

	/**
	 * Checks if all of the prerequisites for this event are met
	 * @param player The player that the event is triggered by
	 * @return A boolean, true if prerequisites are met
	 */
	private final boolean arePrerequisitesMet(Player player, RpgNpc npc) {
		for (Prerequisite dialoguePrerequisite : prerequisites) {
			if (!dialoguePrerequisite.isMet(player, npc)) {
				return false;
			}
		}
		
		return true;
	}
}
