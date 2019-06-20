package rpg_npcs.role;

import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;

import rpg_npcs.DialogueMapping;
import rpg_npcs.RpgNpc;
import rpg_npcs.WeightedSet;
import rpg_npcs.script.Script;
import rpg_npcs.state.State;
import rpg_npcs.trigger.Trigger;

public class Role implements RoleNamedProperty {
	private final String roleName;
	private final Set<Role> parentRoles;
	private final DialogueMapping dialogueMap;
	private final RolePropertyMap<Trigger> triggers;
	private final RolePropertyMap<Script> scripts;
	private final RolePropertyMap<State<?>> states;
	
	public static final String DEFAULT_ROLE_NAME_STRING = "__baseRole__";
	
	public Role(String roleName, RolePropertyMap<Trigger> triggers, Set<Role> parentRoles,
			DialogueMapping dialogueMap, RolePropertyMap<Script> scripts,
			RolePropertyMap<State<?>> states) {
		this.roleName = roleName;
		this.triggers = triggers;
		this.parentRoles = parentRoles;
		this.dialogueMap = dialogueMap;
		this.scripts = scripts;
		this.states = states;
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
		results.putAll(this.scripts, this.getNameString() + ".");
		
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
		results.putAll(this.triggers, this.getNameString() + ".");
		
		return results;
	}
	
	/**
	 * @return a set of all of the states visible to this role
	 */
	public RolePropertyMap<State<?>> getAllVisibleStates() {
		RolePropertyMap<State<?>> results = new RolePropertyMap<State<?>>();

		// Recursively copy parent states
		for (Role role : getImmediateParentRoles()) {
			results.putAll(role.getAllVisibleStates());
		}
		
		// Put this's states in
		results.putAll(this.states, "");
		results.putAll(this.states, this.getNameString() + ".");
		
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
	
	public void registerNpc(RpgNpc npc) {
		Map<Trigger, WeightedSet<Script>> mapping = getDialogueMap();
		for (Trigger trigger : mapping.keySet()) {
			WeightedSet<Script> scriptSet = mapping.get(trigger);
			trigger.registerNPC(npc, scriptSet);
		}
		
		for (Role role : getImmediateParentRoles()) {
			role.registerNpc(npc);
		}
	}
	
	public void unregisterNpc(RpgNpc npc) {
		for (Trigger trigger : triggers.values()) {
			trigger.unregisterNPC(npc);
		}
		
		for (Role role : getImmediateParentRoles()) {
			role.unregisterNpc(npc);
		}
	}

	@Override
	public String getNameString() {
		return roleName;
	}
}
