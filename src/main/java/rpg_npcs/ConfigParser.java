package rpg_npcs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import rpg_npcs.prerequisite.Prerequisite;
import rpg_npcs.prerequisite.PrerequisiteFactory;
import rpg_npcs.prerequisite.PrerequisiteFactory.PrerequisiteFactoryReturnData;
import rpg_npcs.script.ScriptFactory;
import rpg_npcs.script.ScriptFactoryState;
import rpg_npcs.script.ScriptMap;
import rpg_npcs.trigger.Trigger;
import rpg_npcs.trigger.TriggerFactory;
import rpg_npcs.trigger.TriggerFactory.TriggerFactoryReturnData;

public class ConfigParser {
	public static class ConfigResult {
		public final String logString;
		public final Map<String, Trigger> triggerMap;
		public final Map<String, Role> rolesMap;
		
		public ConfigResult(String logString, Map<String, Trigger> triggerMap, Map<String, Role> rolesMap) {
			super();
			this.logString = logString;
			this.triggerMap = triggerMap;
			this.rolesMap = rolesMap;
		}
	}
	
	public static ConfigResult reloadConfig(ScriptFactory scriptFactory, Configuration config) {
		/*
		 * Config design
		 * [] required
		 * () optional
		 * 
		 * defaultEventPriority: [number]
		 * triggers:
		 *   eventName1:
		 *     type: [typeName]
		 *     priority: [number]
		 *     prerequisites:
		 *       (prerequisite1: val1)
		 *       (prerequisite2: val2)
		 *     (...)
		 * roles:
		 *   roleName1:
		 *     parents:
		 *       - [parent1]
		 *       - [parent2]
		 *     dialogues:
		 *       eventName1: script1
		 *       eventName2:
		 *         - script1
		 *         - script3
		 *       eventName3:
		 *         - name: script1
		 *           weight: 10
		 *         - name: script3
		 *           weight: 5
		 *     scripts:
		 *       script1: "script"
		 *       script2: "script" 
		 *       script3: "script" 
		 *   playerName1:
		 *     parents:
		 *       - [parent1]
		 *       - [parent2]
		 *     events: (...)
		 *     scripts: (...)
		 */
		
		String log = "";
		
		int defaultEventPriority = config.getInt("defaultEventPriority");
		
		// Resolve events
		Map<String, Trigger> triggerMap = new HashMap<String, Trigger>();
		ConfigurationSection triggersConfigSection = config.getConfigurationSection("triggers");
		log += getTriggers(triggersConfigSection, triggerMap, defaultEventPriority);
		
		// Resolve roles
		Map<String, Role> rolesMap = new HashMap<String, Role>();
		ConfigurationSection rolesConfigSection = config.getConfigurationSection("roles");
		log += getRoles(rolesConfigSection, triggerMap, scriptFactory, rolesMap);
		
		return new ConfigResult(log, triggerMap, rolesMap);
	}
	
	private static String getTriggers(ConfigurationSection triggersConfigSection, Map<String, Trigger> triggerMap, int defaultEventPriority) {
		Set<String> triggerNameStrings = triggersConfigSection.getKeys(false);
		
		// Log
		String log = "§fFound " + triggerNameStrings.size() + " triggers\n";
		
		// Loop and resolve
		for (String triggerNameString : triggerNameStrings) {
			ConfigurationSection triggerConfigSection = triggersConfigSection.getConfigurationSection(triggerNameString);
			
			log += "§fTrigger " + triggerNameString + ":\n";
			
			// Get type
			if (!triggerConfigSection.contains("type")) {
				log += "§4Trigger " + triggerNameString + " does not have a type";
				continue;
			}
			if (!triggerConfigSection.isString("type")) {
				log += "§4Trigger " + triggerNameString + "'s type is not a string";
				continue;
			}
			String typeString = triggerConfigSection.getString("type");
			log += "§f - type: " + typeString + "\n";
			
			// Get priority
			int priority = defaultEventPriority;
			if (triggersConfigSection.contains("priority") && triggersConfigSection.isInt("priority")) {
				priority = triggersConfigSection.getInt("priority");
				
				log += "§f - priority: " + priority + "\n";
			}
			
			// Check if any prerequisites exist
			Collection<Prerequisite> prerequisites = new HashSet<Prerequisite>();
			if (triggerConfigSection.contains("prerequisites")) {
				if (!triggerConfigSection.isConfigurationSection("prerequisites")) {
					// If prerequisites are badly formatted then skip this trigger
					log += "§4Trigger " + triggerNameString + " prerequisites are in an unrecognised format";
					continue;
				}
				
				// Create prerequisites
				Map<String, Object> prerequisitesConfigSet = triggerConfigSection.getConfigurationSection("prerequisites").getValues(false);
				log += "§f - prerequisites: \n";
				for (String keyString : prerequisitesConfigSet.keySet()) {
					// Extract key and value
					String valueString = prerequisitesConfigSet.get(keyString).toString();
					
					// Convert to prerequisite
					PrerequisiteFactoryReturnData returnPrerequisiteData = PrerequisiteFactory.createPrerequisite(keyString, valueString);
					
					if (returnPrerequisiteData.prerequisite != null) {
						prerequisites.add(returnPrerequisiteData.prerequisite);
					}
					
					// Log
					log += "§f   - " + keyString + ": " + valueString + "\n";
					log += "§4" + returnPrerequisiteData.errorLogString;
				}
			} else {
				log += "§f - no prerequisites\n";
			}
			
			// Create trigger
			TriggerFactoryReturnData returnTriggerData = TriggerFactory.createTrigger(typeString, prerequisites, priority);
			
			if (returnTriggerData.trigger != null) {
				triggerMap.put(triggerNameString, returnTriggerData.trigger);
			}
			
			// Log
			log += "§4" + returnTriggerData.errorLogString;
		}
		
		return log;
	}

	
	private static String getRoles(ConfigurationSection rolesConfigSection, Map<String, Trigger> triggerMap, ScriptFactory scriptFactory, Map<String, Role> rolesMap) {
		Set<String> roleNameStrings = rolesConfigSection.getKeys(false);
		// Log
		String log = "§fFound " + roleNameStrings.size() + " roles\n";
		
		// Loop and resolve parent names
		Map<String, List<String>> roleParentsMap = new HashMap<String, List<String>>();
		for (String roleNameString : roleNameStrings) {
			ConfigurationSection roleConfigSection = rolesConfigSection.getConfigurationSection(roleNameString);
			
			// Resolve parent names into map
			List<String> parentNames = new ArrayList<String>();
			if (roleConfigSection.contains("parents")) {
				parentNames = roleConfigSection.getStringList("parents");
			}
			
			roleParentsMap.put(roleNameString, parentNames);
		}
		
		// Mill down roleNameStrings to resolve all resolvable role trees
		List<String> rolesToResolve = new LinkedList<>(roleNameStrings);
		int lastChanged; // Keep track of how many roles were resolved. If this is 0 after a loop then there is a dependency loop
		while (rolesToResolve.size() > 0) {
			lastChanged = 0;
			
			// Loop through roles and resolve what can be resolved
			for (int i = rolesToResolve.size() - 1; i >= 0; i--) {
				String roleNameString = rolesToResolve.get(i);
				List<String> parentNames = roleParentsMap.get(roleNameString);
				
				// Check if can be resolved ever
				if (!roleNameStrings.containsAll(parentNames)) {
					Set<String> missingParentSet = new HashSet<String>(parentNames);
					missingParentSet.removeAll(roleNameStrings);
					log += "§4Role " + roleNameString + " has undefined parents: " + String.join(", ", missingParentSet) + "\n";
					roleNameStrings.remove(roleNameString);
					rolesToResolve.remove(i);
					lastChanged++;
					continue;
				}
				
				// Check if can be resolved now
				if (!rolesMap.keySet().containsAll(parentNames)) {
					continue;
				}

				ConfigurationSection roleConfigSection = rolesConfigSection.getConfigurationSection(roleNameString);

				log += "§fRole " + roleNameString + ":\n";
				
				// Collect parents
				Collection<Role> parentRoles = new LinkedList<Role>();
				for (String parentName : parentNames) {
					log += "§f - parent" + parentName + "\n";
					parentRoles.add(rolesMap.get(parentName));
				}
				
				// Gather parent scripts
				ScriptMap parentScriptsMap = new ScriptMap();
				for (Role role : parentRoles) {
					role.putAllScriptsInMap(parentScriptsMap);
				}
				
				// Gather script commands
				Map<String, String> scriptStringsMap = new HashMap<String, String>();
				if (roleConfigSection.contains("scripts")) {
					ConfigurationSection scriptStringsConfigSection = roleConfigSection.getConfigurationSection("scripts");
					Set<String> scriptNameStrings = scriptStringsConfigSection.getKeys(false);
					
					log += "§f - scripts:\n";
					
					for (String scriptNameString : scriptNameStrings) {
						if (!scriptStringsConfigSection.isString(scriptNameString)) {
							log += "§4Script " + scriptNameString + " is not a string\n";
							continue;
						}
						
						scriptStringsMap.put(scriptNameString, scriptStringsConfigSection.getString(scriptNameString));
						
						log += "§f   - " + scriptNameString + "\n";
					}
				}
				
				// Generate role specific scripts
				ScriptFactoryState state = scriptFactory.createConversationTree(scriptStringsMap, parentScriptsMap);
				ScriptMap roleScriptMap = state.getNewScripts();
				ScriptMap allScriptMap = state.getAllScripts();
				
				// Generate Script Triggers
				Map<Trigger, WeightedSet<String>> roleTriggerMap = new HashMap<Trigger, WeightedSet<String>>();
				if (roleConfigSection.contains("triggers")) {
					ConfigurationSection roleTriggersConfigSection = roleConfigSection.getConfigurationSection("triggers");
					Set<String> roleTriggerNameStrings = roleTriggersConfigSection.getKeys(false);
					
					log += "§f - triggers:\n";
					
					for (String roleTriggerNameString : roleTriggerNameStrings) {
						// Get trigger
						if (!triggerMap.containsKey(roleTriggerNameString)) {
							log += "§4Trigger " + roleTriggerNameString + " is not defined\n";
							continue;
						}
						
						Trigger roleTrigger = triggerMap.get(roleTriggerNameString);
						
						// Get scripts
						WeightedSet<String> roleTriggerScripts = new WeightedSet<String>();

						log += "§f   - " + roleTriggerNameString + ":\n";
						
						if (roleTriggersConfigSection.isString(roleTriggerNameString)) {
							String scriptNameString = roleTriggersConfigSection.getString(roleTriggerNameString);
							
							if (!allScriptMap.containsKey(scriptNameString)) {
								log += "§4Script " + scriptNameString + " is not defined\n";
								continue;
							}
							
							log += "§f     Single script " + scriptNameString + "\n";
							
							roleTriggerScripts.addEntry(scriptNameString, 1);
						} else if (roleTriggersConfigSection.isList(roleTriggerNameString)) {
							List<String> scriptNameStringList = roleTriggersConfigSection.getStringList(roleTriggerNameString);

							log += "§f     Script List: \n";
							
							for (String scriptNameString : scriptNameStringList) {
								if (!allScriptMap.containsKey(scriptNameString)) {
									log += "§4Script " + scriptNameString + " is not defined\n";
									continue;
								}
								
								log += "§f      - " + roleTriggerNameString + "\n";
								
								roleTriggerScripts.addEntry(scriptNameString, 1);
							}
						}
						
						if (roleTriggerScripts.size() == 0) {
							continue;
						}
						
						roleTriggerMap.put(roleTrigger, roleTriggerScripts);
					}
				}
				
				// Create and add role to map
				Role newRole = new Role(roleNameString, parentRoles, roleTriggerMap, roleScriptMap);
				rolesMap.put(roleNameString, newRole);
				
				// Mark role as populated
				rolesToResolve.remove(i);
				lastChanged++;
			}
			
			// Check for dependency loops
			if (lastChanged == 0) {
				log += "§4Circular Role dependencies found with the following roles, unable to compile: " + String.join(", ", rolesToResolve);
				rolesToResolve.clear();
			}
		}
		
		return log;
	}
}
