package rpg_npcs;

import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import com.sun.istack.internal.NotNull;

import rpg_npcs.script.Script;
import rpg_npcs.script.ScriptMap;
import rpg_npcs.trigger.Trigger;

public class Role {
	public final String roleName;
	private final TriggerMap triggers;
	private final Set<Role> parentRoles;
	private final DialogueMap dialogueMap;
	private final ScriptMap scripts;
	
	public static final String DEFAULT_ROLE_NAME_STRING = "Base Role";
	
	public Role(String roleName, @NotNull TriggerMap triggers, @NotNull Set<Role> parentRoles,
			@NotNull DialogueMap dialogueMap, @NotNull ScriptMap scripts) {
		this.roleName = roleName;
		this.triggers = triggers;
		this.parentRoles = parentRoles;
		this.dialogueMap = dialogueMap;
		this.scripts = scripts;
	}
	
	/**
	 * @return a set of all of the triggers visible to this role
	 */
	public TriggerMap getAllVisibleTriggers() {
		TriggerMap allTriggers = triggers.copy();
		
		for (Role role : getImmediateParentRoles()) {
			allTriggers.putAll(role.getAllVisibleTriggers());
		}
		
		return allTriggers;
	}
	
	public void registerTriggerListeners(Plugin plugin) {
		for (Trigger trigger : triggers.values()) {
			Bukkit.getPluginManager().registerEvents(trigger, plugin);
		}
	}
	
	public void unregisterTriggerListeners(Plugin plugin) {
		for (Trigger trigger : triggers.values()) {
			HandlerList.unregisterAll(trigger);
		}
	}
	
	/**
	 * @return a set of the immediate parents to this role
	 */
	public Set<Role> getImmediateParentRoles() {
		return parentRoles;
	}

	/**
	 * @return the mapping of trigger names to script names
	 */
	public DialogueMap getDialogueNamesMap() {
		return dialogueMap;
	}

	/**
	 * @return the mapping of triggers to scripts
	 */
	public Map<Trigger, WeightedSet<Script>> getDialogueMap() {
		return dialogueMap.zip(triggers, scripts);
	}
	
	/**
	 * Gets all of the scripts contained within this role
	 * @return The map of all of the scripts in this and all parent roles
	 */
	public ScriptMap getAllScripts() {
		ScriptMap results = new ScriptMap();
		
		putAllScriptsInMap(results);
		
		return results;
	}

	/**
	 * Gets all of the scripts contained within this role
	 * @param results the map to place the results in
	 */
	private void putAllScriptsInMap(ScriptMap results) {
		// Recursively copy parent scripts
		for (Role role : getImmediateParentRoles()) {
			role.putAllScriptsInMap(results);
		}
		
		// Copy scripts
		results.putAll(scripts, "");
		results.putAll(scripts, roleName + ".");
	}
	
	public void registerNpc(RpgTrait npc) {
		Map<Trigger, WeightedSet<Script>> mapping = getDialogueMap();
		for (Trigger trigger : mapping.keySet()) {
			WeightedSet<Script> scriptSet = mapping.get(trigger);
			trigger.registerNPC(npc, scriptSet);
		}
		
		for (Role role : getImmediateParentRoles()) {
			role.registerNpc(npc);
		}
	}
	
	public void unregisterNpc(RpgTrait npc) {
		for (String triggerName : dialogueMap.keySet()) {
			Trigger trigger = triggers.get(triggerName);
			trigger.unregisterNPC(npc);
		}
		
		for (Role role : getImmediateParentRoles()) {
			role.unregisterNpc(npc);
		}
	}
}
