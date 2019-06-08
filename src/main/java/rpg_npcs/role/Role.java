package rpg_npcs.role;

import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import com.sun.istack.internal.NotNull;

import rpg_npcs.DialogueMapping;
import rpg_npcs.RpgTrait;
import rpg_npcs.WeightedSet;
import rpg_npcs.script.Script;
import rpg_npcs.trigger.Trigger;

public class Role extends RoleNamedProperty {
	private final RolePropertyMap<Trigger> triggers;
	private final Set<Role> parentRoles;
	private final DialogueMapping dialogueMap;
	private final RolePropertyMap<Script> scripts;
	
	public static final String DEFAULT_ROLE_NAME_STRING = "Base Role";
	
	public Role(String roleName, @NotNull RolePropertyMap<Trigger> triggers, @NotNull Set<Role> parentRoles,
			@NotNull DialogueMapping dialogueMap, @NotNull RolePropertyMap<Script> scripts) {
		super(roleName);
		this.triggers = triggers;
		this.parentRoles = parentRoles;
		this.dialogueMap = dialogueMap;
		this.scripts = scripts;
	}
	
	/**
	 * @return The map of all of the scripts in this and all parent roles
	 */
	public RolePropertyMap<Script> getAllVisibleScripts() {
		RolePropertyMap<Script> results = new RolePropertyMap<Script>();

		// Recursively copy parent scripts
		for (Role role : getImmediateParentRoles()) {
			results.putAll(role.getAllVisibleScripts());
		}
		
		// Put this's scripts in
		results.putAll(this.scripts, "");
		results.putAll(this.scripts, this.nameString + ".");
		
		return results;
	}
	
	/**
	 * @return a set of all of the triggers visible to this role
	 */
	public RolePropertyMap<Trigger> getAllVisibleTriggers() {
		RolePropertyMap<Trigger> results = new RolePropertyMap<Trigger>();

		// Recursively copy parent triggers
		for (Role role : getImmediateParentRoles()) {
			results.putAll(role.getAllVisibleTriggers());
		}
		
		// Put this's triggers in
		results.putAll(this.triggers, "");
		results.putAll(this.triggers, this.nameString + ".");
		
		return results;
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
	public DialogueMapping getDialogueNamesMap() {
		return dialogueMap;
	}

	/**
	 * @return the mapping of triggers to scripts defined by this role
	 */
	public Map<Trigger, WeightedSet<Script>> getLocalDialogueMap() {
		return dialogueMap.zip(triggers, scripts);
	}

	/**
	 * @return the mapping of triggers to scripts visible by this role
	 */
	public Map<Trigger, WeightedSet<Script>> getDialogueMap() {
		return dialogueMap.zip(getAllVisibleTriggers(), getAllVisibleScripts());
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
