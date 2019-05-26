package rpg_npcs;

import java.util.Collection;
import java.util.Map;

import com.sun.istack.internal.NotNull;

import rpg_npcs.script.Script;
import rpg_npcs.script.ScriptMap;
import rpg_npcs.trigger.Trigger;

public class Role {
	public final String roleName;
	public final Collection<Role> parentRoles;
	private final Map<Trigger, WeightedSet<String>> triggerMap;
	private final ScriptMap scripts;
	
	public Role(String roleName, @NotNull Collection<Role> parentRoles, @NotNull Map<Trigger, WeightedSet<String>> triggerMap, ScriptMap scripts) {
		this.roleName = roleName;
		this.parentRoles = parentRoles;
		this.triggerMap = triggerMap;
		this.scripts = scripts;
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
	public void putAllScriptsInMap(ScriptMap results) {
		// Recursively copy parent scripts
		for (Role role : parentRoles) {
			role.putAllScriptsInMap(results);
		}
		
		// Copy scripts
		results.putAll(scripts, "");
		results.putAll(scripts, roleName + ".");
	}
	
	public void registerNpc(RpgTrait npc) {
		for (Trigger trigger : triggerMap.keySet()) {
			WeightedSet<Script> scriptSet = triggerMap.get(trigger).zip(getAllScripts());
			trigger.registerNPC(npc, scriptSet);
		}
		
		for (Role role : parentRoles) {
			role.registerNpc(npc);
		}
	}
	
	public void unregisterNpc(RpgTrait npc) {
		for (Trigger trigger : triggerMap.keySet()) {
			trigger.unregisterNPC(npc);
		}
		
		for (Role role : parentRoles) {
			role.unregisterNpc(npc);
		}
	}
}
