package rpg_npcs.trigger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import rpg_npcs.RpgNpc;
import rpg_npcs.WeightedSet;
import rpg_npcs.prerequisite.Prerequisite;
import rpg_npcs.role.RoleNamedProperty;
import rpg_npcs.script.Script;

public abstract class Trigger extends RoleNamedProperty implements Listener {
	private final Collection<Prerequisite> prerequisites;
	protected final Map<RpgNpc, WeightedSet<Script>> npcScripts;
	private final int priority;
	
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
	
	protected void trigger(Player player, RpgNpc npc) {
		if (arePrerequisitesMet(player, npc) && priority > npc.getConversationPriority()) {
			WeightedSet<Script> scriptSet = npcScripts.get(npc);
			Script script = scriptSet.getRandom();
			
			if (scriptSet.size() == 0) {
				Bukkit.getLogger().warning("Script set is empty");
			}
			
			if (script != null) {
				npc.startConversation(script, player, priority);
			} else {
				Bukkit.getLogger().warning("Script was null");
			}
		}
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
