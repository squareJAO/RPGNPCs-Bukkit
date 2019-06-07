package rpg_npcs;

import java.util.ArrayList;
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
import rpg_npcs.trigger.TriggerFactory;
import rpg_npcs.trigger.TriggerFactory.TriggerFactoryReturnData;

public class ConfigParser {
	public static class ConfigResult {
		public final ParseLog log;
		public final RoleMap rolesMap;
		
		public ConfigResult(ParseLog log, RoleMap rolesMap) {
			super();
			this.log = log;
			this.rolesMap = rolesMap;
		}
	}
	
	static final int[] WEIGHT_MAPPING = {100, 100, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};
	
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
		 *         - script1: [weight1]
		 *         - script2           <----- Weights calculated as remaining %age
		 *         - script3
		 *       eventName3:
		 *         - script1: [weight1]
		 *         - script3: [weight2]
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
		
		ParseLog log = new ParseLog();
		
		int defaultEventPriority = config.getInt("defaultEventPriority");
		
		// Resolve base triggers
		TriggerMap baseTriggerMap = new TriggerMap();
		if (config.isConfigurationSection("triggers")) {
			ConfigurationSection triggersConfigSection = config.getConfigurationSection("triggers");
			baseTriggerMap = getTriggers(log, triggersConfigSection, defaultEventPriority);
		}
		
		// Resolve base scripts
		ScriptMap baseScriptMap = new ScriptMap();
		if (config.isConfigurationSection("scripts")) {
			ConfigurationSection scriptsConfigSection = config.getConfigurationSection("scripts");
			baseScriptMap = getScripts(log, scriptsConfigSection, scriptFactory, new ScriptMap());
		}
		
		// Resolve base dialogues
		DialogueMap baseDialogueMap = new DialogueMap();
		if (config.isConfigurationSection("dialogues")) {
			ConfigurationSection roleDialoguesConfigSection = config.getConfigurationSection("dialogues");
			baseDialogueMap = getDialogues(log, roleDialoguesConfigSection, baseTriggerMap, baseScriptMap);
		}

		// Create base role
		Role baseRole = new Role(Role.DEFAULT_ROLE_NAME_STRING, baseTriggerMap, new HashSet<Role>(), baseDialogueMap, baseScriptMap);
				
		// Resolve roles
		RoleMap rolesMap;
		if (config.contains("roles") && config.isConfigurationSection("roles")) {
			ConfigurationSection rolesConfigSection = config.getConfigurationSection("roles");
			rolesMap = getRoles(log, rolesConfigSection, scriptFactory, baseRole, defaultEventPriority);
		} else {
			rolesMap = new RoleMap();
			rolesMap.put(baseRole);
		}
		
		return new ConfigResult(log, rolesMap);
	}
	
	private static Set<Prerequisite> getPrerequisites(ParseLog log, ConfigurationSection prerequisiteConfigSection) {
		Set<Prerequisite> prerequisites = new HashSet<Prerequisite>();
		
		// Create prerequisites
		Map<String, Object> prerequisitesConfigSet = prerequisiteConfigSection.getValues(false);
		log.addInfo(" - prerequisites:");
		for (String keyString : prerequisitesConfigSet.keySet()) {
			// Extract key and value
			String valueString = prerequisitesConfigSet.get(keyString).toString();
			
			// Convert to prerequisite
			PrerequisiteFactoryReturnData returnPrerequisiteData = PrerequisiteFactory.createPrerequisite(keyString, valueString);
			
			if (returnPrerequisiteData.prerequisite != null) {
				prerequisites.add(returnPrerequisiteData.prerequisite);
			}
			
			log.addInfo("   - " + keyString + ": " + valueString);
			log.add(returnPrerequisiteData.log);
		}
		
		return prerequisites;
	}

	
	private static RoleMap getRoles(ParseLog log, ConfigurationSection rolesConfigSection, ScriptFactory scriptFactory, Role baseRole, int defaultEventPriority) {
		RoleMap rolesMap = new RoleMap();
		Set<String> roleNameStrings = rolesConfigSection.getKeys(false);
		List<String> rolesToResolve = new LinkedList<>(roleNameStrings);
		
		rolesMap.put(baseRole);
		roleNameStrings.add(baseRole.roleName);
		
		log.addInfo("Found " + roleNameStrings.size() + " roles");

		// Mill down roleNameStrings to resolve all resolvable role trees
		int lastChanged; // Keep track of how many roles were resolved. If this is 0 after a loop then there is a dependency loop
		while (rolesToResolve.size() > 0) {
			lastChanged = 0;
			
			// Loop through roles and resolve what can be resolved
			for (int i = rolesToResolve.size() - 1; i >= 0; i--) {
				String roleNameString = rolesToResolve.get(i);
				ConfigurationSection roleConfigSection = rolesConfigSection.getConfigurationSection(roleNameString);

				if (roleConfigSection == null) {
					log.addError("Role " + roleNameString + " is in an invalid format");
					roleNameStrings.remove(roleNameString);
					rolesToResolve.remove(i);
					lastChanged++;
					continue;
				}
				
				// Resolve parent names into map
				List<String> parentNames = getRoleParentNames(roleConfigSection, baseRole);
				
				// Check if can be resolved ever
				if (!roleNameStrings.containsAll(parentNames)) {
					Set<String> missingParentSet = new HashSet<String>(parentNames);
					missingParentSet.removeAll(roleNameStrings);
					log.addError("Role " + roleNameString + " has undefined parents: " + String.join(", ", missingParentSet));
					roleNameStrings.remove(roleNameString);
					rolesToResolve.remove(i);
					lastChanged++;
					continue;
				}
				
				// Check if can be resolved now
				if (!rolesMap.keySet().containsAll(parentNames)) {
					continue;
				}


				log.addInfo("Role " + roleNameString + ":");

				// Collect parents
				Set<Role> parentRoles = new HashSet<Role>();
				for (String parentName : parentNames) {
					log.addInfo(" - parent: '" + parentName + "'");
					parentRoles.add(rolesMap.get(parentName));
				}
				
				// Gather parent scripts & triggers
				TriggerMap allTriggersMap = new TriggerMap();
				ScriptMap allScriptsMap = new ScriptMap();
				for (Role role : parentRoles) {
					allScriptsMap.putAll(role.getAllScripts());
					allTriggersMap.putAll(role.getAllVisibleTriggers());
				}
				
				// Gather script commands
				ScriptMap roleScriptMap = new ScriptMap();
				if (roleConfigSection.isConfigurationSection("scripts")) {
					ConfigurationSection scriptStringsConfigSection = roleConfigSection.getConfigurationSection("scripts");
					roleScriptMap = getScripts(log, scriptStringsConfigSection, scriptFactory, allScriptsMap);
				}
				allScriptsMap.putAll(roleScriptMap);
				
				// Generate role defined triggers
				TriggerMap roleTriggersMap = new TriggerMap(); 
				if (roleConfigSection.isConfigurationSection("triggers")) {
					ConfigurationSection triggersConfigSection = roleConfigSection.getConfigurationSection("triggers");
					roleTriggersMap = getTriggers(log, triggersConfigSection, defaultEventPriority);
				}
				allTriggersMap.putAll(roleTriggersMap);
				
				// Generate Dialogues
				DialogueMap dialogueMap = new DialogueMap();
				if (roleConfigSection.isConfigurationSection("dialogues")) {
					ConfigurationSection roleDialoguesConfigSection = roleConfigSection.getConfigurationSection("dialogues");
					dialogueMap = getDialogues(log, roleDialoguesConfigSection, allTriggersMap, allScriptsMap);
				}
				
				Role newRole = new Role(roleNameString, roleTriggersMap, parentRoles, dialogueMap, roleScriptMap);
				
				rolesMap.put(newRole);
				
				// Mark role as populated
				rolesToResolve.remove(i);
				lastChanged++;
			}
			
			// Check for dependency loops
			if (lastChanged == 0) {
				log.addError("Circular Role dependencies found with the following roles, unable to compile: " + String.join(", ", rolesToResolve));
				rolesToResolve.clear();
			}
		}
		
		return rolesMap;
	}
	
	private static ScriptMap getScripts(ParseLog log, ConfigurationSection scriptStringsConfigSection, ScriptFactory scriptFactory, ScriptMap allScriptsMap) {
		Map<String, String> scriptStringsMap = new HashMap<String, String>();
		Set<String> scriptNameStrings = scriptStringsConfigSection.getKeys(false);
		
		log.addInfo(" - scripts:");
		
		for (String scriptNameString : scriptNameStrings) {
			if (!scriptStringsConfigSection.isString(scriptNameString)) {
				log.addError("Script " + scriptNameString + " is not a string");
				continue;
			}
			
			scriptStringsMap.put(scriptNameString, scriptStringsConfigSection.getString(scriptNameString));
			
			log.addInfo("   - " + scriptNameString);
		}
		
		// Generate role specific scripts
		ScriptFactoryState state = scriptFactory.createConversationTree(scriptStringsMap, allScriptsMap);
		return state.getNewScripts();
	}
	
	private static TriggerMap getTriggers(ParseLog log, ConfigurationSection triggersConfigSection, int defaultEventPriority) {
		TriggerMap triggerMap = new TriggerMap();
		Set<String> triggerNameStrings = triggersConfigSection.getKeys(false);
		
		log.addInfo("Found " + triggerNameStrings.size() + " triggers");
		
		// Loop and resolve
		for (String triggerNameString : triggerNameStrings) {
			ConfigurationSection triggerConfigSection = triggersConfigSection.getConfigurationSection(triggerNameString);
			
			log.addInfo("Trigger " + triggerNameString + ":");
			
			// Get type
			if (!triggerConfigSection.contains("type")) {
				log.addError("Trigger " + triggerNameString + " does not have a type");
				continue;
			}
			if (!triggerConfigSection.isString("type")) {
				log.addError("Trigger " + triggerNameString + "'s type is not a string");
				continue;
			}
			String typeString = triggerConfigSection.getString("type");
			log.addInfo(" - type: " + typeString);
			
			// Get priority
			int priority = defaultEventPriority;
			if (triggerConfigSection.contains("priority") && triggerConfigSection.isInt("priority")) {
				if (!triggerConfigSection.isInt("priority")) {
					log.addError("Trigger " + triggerNameString + " priority is not an int");
					continue;
				}
				
				priority = triggerConfigSection.getInt("priority");
				
				log.addInfo(" - priority: " + priority);
			}
			
			// Check if any prerequisites exist
			Set<Prerequisite> prerequisites;
			if (triggerConfigSection.contains("prerequisites")) {
				if (!triggerConfigSection.isConfigurationSection("prerequisites")) {
					// If prerequisites are badly formatted then skip this trigger
					log.addError("Trigger " + triggerNameString + " prerequisites are in an unrecognised format");
					continue;
				}
				
				ConfigurationSection prerequisiteConfigSection = triggerConfigSection.getConfigurationSection("prerequisites");
				
				prerequisites = getPrerequisites(log, prerequisiteConfigSection);
			} else {
				prerequisites = new HashSet<Prerequisite>();
				log.addInfo(" - no prerequisites");
			}
			
			// Create trigger
			TriggerFactoryReturnData returnTriggerData = TriggerFactory.createTrigger(typeString, prerequisites, priority);
			
			if (returnTriggerData.trigger != null) {
				triggerMap.put(triggerNameString, returnTriggerData.trigger);
			}
			
			log.add(returnTriggerData.log);
		}
		
		return triggerMap;
	}
	
	private static DialogueMap getDialogues(ParseLog log, ConfigurationSection roleDialoguesConfigSection, TriggerMap triggers, ScriptMap allScriptMap) {
		DialogueMap dialogueMap = new DialogueMap();
		Set<String> roleTriggerNameStrings = roleDialoguesConfigSection.getKeys(false);
		
		log.addInfo(" - dialogues:");
		
		for (String triggerNameString : roleTriggerNameStrings) {
			// Get trigger
			if (!triggers.containsKey(triggerNameString)) {
				log.addError("Trigger " + triggerNameString + " is not defined");
				continue;
			}

			log.addInfo("   - " + triggerNameString + ":");
			
			// Get scripts
			WeightedSet<String> scriptNames = getDialogueScriptSet(log, roleDialoguesConfigSection, triggerNameString, allScriptMap);
			
			if (scriptNames.size() == 0) {
				continue;
			}
			
			dialogueMap.put(triggerNameString, scriptNames);
		}
		
		return dialogueMap;
	}
	
	private static List<String> getRoleParentNames(ConfigurationSection roleConfigSection, Role baseRole) {
		if (roleConfigSection.contains("parents")) {
			return roleConfigSection.getStringList("parents");
		}
		
		List<String> parentNames = new ArrayList<String>();
		parentNames.add(baseRole.roleName);
		return parentNames;
	}

	/**
	 * Gets the names of the scripts defined under a trigger name in the dialogue section
	 * @param log The current parse log
	 * @param dialoguesConfig the section containing all dialogues
	 * @param triggerName The name of the current dialogue
	 * @param scriptMap All of the scripts visible in the role
	 * @return A weighted set of all of the names of the scripts to bind to the event
	 */
	private static WeightedSet<String> getDialogueScriptSet(ParseLog log, ConfigurationSection dialoguesConfig, String triggerName, ScriptMap scriptMap) {
		WeightedSet<String> roleTriggerScripts = new WeightedSet<String>();
		
		// A single string
		if (dialoguesConfig.isString(triggerName)) {
			String scriptNameString = dialoguesConfig.getString(triggerName);
			
			if (!scriptMap.containsKey(scriptNameString)) {
				log.addError("Script " + scriptNameString + " is not defined");
				return roleTriggerScripts;
			}
			
			log.addInfo("     Single script " + scriptNameString);
			
			roleTriggerScripts.addEntry(scriptNameString, 1);
			
	    // A list of scripts
		} else if (dialoguesConfig.isList(triggerName)) {
			log.addInfo("     Script List:");
			
			int totalWeight = 0;
			
			// Get scripts with weights
			List<Map<?, ?>> weightedScriptNames = dialoguesConfig.getMapList(triggerName);
			
			for (Map<?, ?> mapGeneric : weightedScriptNames) {
				for (Object key : mapGeneric.keySet()) {
					// Get script name
					String scriptName = key.toString();
					
					if (!scriptMap.containsKey(scriptName)) {
						log.addError("Script " + scriptName + " is not defined");
						continue;
					}
					
					// Get weight
					Object valueObject = mapGeneric.get(key);
					String valueString = valueObject.toString();
					int weight;
					try {
						weight = Integer.valueOf(valueString);
					} catch (NumberFormatException e) {
						log.addError("Cannot parse weight number from given value: '" + valueString + "'");
						continue;
					}
					
					if (weight <= 0) {
						log.addError("Given weight is less than 1: " + weight);
						continue;
					}
					
					log.addInfo("      - " + scriptName + ", weight: " + weight);
					
					roleTriggerScripts.addEntry(scriptName, weight);
					totalWeight += weight;
				}
			}
			
			// Get scripts without weights
			List<String> unweightedScriptNames = dialoguesConfig.getStringList(triggerName);

			if (unweightedScriptNames.size() > 0) {
				// Calculate the weights of the unweighted scripts
				int uniformWeights;
				if (totalWeight == 0) {
					uniformWeights = 1;
				} else {
					double sigFigsDouble = Math.log10(totalWeight);
					int sigFigs = (int)Math.floor(sigFigsDouble) + 1;
					int outOf = WEIGHT_MAPPING[sigFigs];
					int remaining = outOf - totalWeight;
					uniformWeights = remaining / unweightedScriptNames.size();
				}
				
				for (String scriptNameString : unweightedScriptNames) {
					if (!scriptMap.containsKey(scriptNameString)) {
						log.addError("Script " + scriptNameString + " is not defined");
						continue;
					}
	
					if (totalWeight == 0) {
						log.addInfo("      - " + scriptNameString);
					} else {
						log.addInfo("      - " + scriptNameString + ", weight: " + uniformWeights);
					}
					
					roleTriggerScripts.addEntry(scriptNameString, uniformWeights);
				}
			}
		} else {
			log.addError("Unsupported dialogue config type");
		}
		
		return roleTriggerScripts;
	}
}
