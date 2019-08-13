package rpg_npcs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import rpg_npcs.logging.Log;
import rpg_npcs.logging.Logged;
import rpg_npcs.prerequisite.PrerequisiteSet;
import rpg_npcs.role.Role;
import rpg_npcs.role.RolePropertyMap;
import rpg_npcs.script.Script;
import rpg_npcs.state.State;
import rpg_npcs.trigger.Trigger;

public class ConfigParser {
	public static class ConfigResult {
		public final Log log;
		public final RolePropertyMap<Role> rolesMap;
		
		public ConfigResult(Log log, RolePropertyMap<Role> rolesMap) {
			super();
			this.log = log;
			this.rolesMap = rolesMap;
		}
	}
	
	static final int[] WEIGHT_MAPPING = {100, 100, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};
	static final String[] INVALID_NAME_STRINGS = {".", ":", ";", "/", "\\", "__", "%", "&", "=", "?", Role.DEFAULT_ROLE_NAME_STRING};
	
	private final ParserFactorySet factorySet;
	private int defaultEventPriority;
	
	public ConfigParser(ParserFactorySet factorySet, int defaultEventPriority) {
		this.factorySet = factorySet;
		this.defaultEventPriority = defaultEventPriority;
	}
	
	public ConfigResult reloadConfig(Configuration config) {
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
		 *     states:
		 *       state1:
		 *         type: number/text/location
		 *         scope: npc/global
		 *         default: [default value]
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
		 *     triggers: (...)
		 *     scripts: (...)
		 */
		
		Log baselog = new Log();
		
		// Create base role
		Logged<Role> loggedBaseRole = getRole(Role.DEFAULT_ROLE_NAME_STRING, new HashSet<Role>(), config);
		baselog.addEntry(loggedBaseRole.getLog());
		Role baseRole = loggedBaseRole.getResult();
		
		// Resolve roles
		RolePropertyMap<Role> rolesMap;
		if (config.contains("roles") && config.isConfigurationSection("roles")) {
			ConfigurationSection rolesConfigSection = config.getConfigurationSection("roles");
			Logged<RolePropertyMap<Role>> loggedRolesMap = getRoles(rolesConfigSection, baseRole);
			baselog.addNamedEntry("Roles", loggedRolesMap.getLog());
			rolesMap = loggedRolesMap.getResult();
		} else {
			rolesMap = new RolePropertyMap<Role>();
			rolesMap.put(baseRole);
		}
		
		return new ConfigResult(baselog, rolesMap);
	}
	
	private Logged<RolePropertyMap<State<?>>> getStates(ConfigurationSection statesStatesConfigSection, String scopeName) {
		Log log = new Log();
		
		RolePropertyMap<State<?>> statesMap = new RolePropertyMap<State<?>>();
		Set<String> stateNameStrings = statesStatesConfigSection.getKeys(false);
		
		// Loop and resolve
		for (String stateNameString : stateNameStrings) {
			if (!statesStatesConfigSection.isConfigurationSection(stateNameString)) {
				log.addError("State " + stateNameString + " is given in an invalid format");
				continue;
			}
			
			// Check name
			String invalidPartString = checkForInvalidCharacters(stateNameString);
			if (invalidPartString != null) {
				log.addError("State name '" + stateNameString + "' cannot contain " + invalidPartString);
				continue;
			}
			
			log.addInfo("State " + stateNameString + ":");
			
			ConfigurationSection stateConfigSection = statesStatesConfigSection.getConfigurationSection(stateNameString);
			
			// Get type
			if (!stateConfigSection.isString("type")) {
				log.addError("State " + stateNameString + " does not have a type");
				continue;
			}
			String typeString = stateConfigSection.getString("type");
			
			// Get scope
			String scopeString = "";
			if (stateConfigSection.isString("scope")) {
				scopeString = stateConfigSection.getString("scope");
			}
			
			// Get default
			String defaultValueString = null;
			if (stateConfigSection.contains("default")) {
				defaultValueString = stateConfigSection.getString("default");
			}
			
			// Create UUID
			String uuid = scopeName + "." + stateNameString + "." + typeString;
			
			// Create state
			Logged<State<?>> data = factorySet.getStateFactory().makeState(stateNameString, typeString, scopeString, defaultValueString, uuid);
			if (data.getResult() != null) {
				statesMap.put(data.getResult());
				log.addInfo("Type: " + typeString);
				log.addInfo("Scope: " + scopeString);
				log.addInfo("Default: " + data.getResult().getDefaultValue().toString());
			}
			log.addNamedEntry(stateNameString, data.getLog());
		}
		
		return new Logged<RolePropertyMap<State<?>>>(statesMap, log);
	}
	
	private Logged<RolePropertyMap<Trigger>> getTriggers(ConfigurationSection triggersConfigSection) {
		Log log = new Log();
		
		RolePropertyMap<Trigger> triggerMap = new RolePropertyMap<Trigger>();
		Set<String> triggerNameStrings = triggersConfigSection.getKeys(false);
		
		log.addInfo("Found " + triggerNameStrings.size() + " triggers");
		
		// Loop and resolve
		for (String triggerNameString : triggerNameStrings) {
			// Check trigger name is valid
			String invalidPartString = checkForInvalidCharacters(triggerNameString);
			if (invalidPartString != null) {
				log.addError("Trigger name '" + triggerNameString + "' cannot contain " + invalidPartString);
				continue;
			}
			
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
			log.addInfo("Type: " + typeString);
			
			// Get priority
			int priority = defaultEventPriority;
			if (triggerConfigSection.contains("priority") && triggerConfigSection.isInt("priority")) {
				if (!triggerConfigSection.isInt("priority")) {
					log.addError("Trigger " + triggerNameString + " priority is not an int");
					continue;
				}
				
				priority = triggerConfigSection.getInt("priority");
				
				log.addInfo("Priority: " + priority);
			}
			
			// Check if any prerequisites exist
			PrerequisiteSet prerequisites;
			if (triggerConfigSection.contains("prerequisites")) {
				if (!triggerConfigSection.isConfigurationSection("prerequisites")) {
					// If prerequisites are badly formatted then skip this trigger
					log.addError("Trigger " + triggerNameString + " prerequisites are in an unrecognised format");
					continue;
				}
				
				ConfigurationSection prerequisiteConfigSection = triggerConfigSection.getConfigurationSection("prerequisites");
				
				Logged<PrerequisiteSet> loggedPrerequisites = getPrerequisites(prerequisiteConfigSection);
				prerequisites = loggedPrerequisites.getResult();
				log.addNamedEntry("Prerequisites", loggedPrerequisites.getLog());
			} else {
				prerequisites = new PrerequisiteSet();
				log.addInfo("No prerequisites");
			}
			
			// Create trigger
			Logged<Trigger> returnTriggerData = factorySet.getTriggerFactory().createTrigger(typeString, triggerNameString, prerequisites, priority);
			
			if (returnTriggerData.getResult() != null) {
				triggerMap.put(triggerNameString, returnTriggerData.getResult());
			}
			
			log.addNamedEntry(triggerNameString, returnTriggerData.getLog());
		}
		
		return new Logged<RolePropertyMap<Trigger>>(triggerMap, log);
	}

	private Logged<PrerequisiteSet> getPrerequisites(ConfigurationSection prerequisiteConfigSection) {
		// Create prerequisites
		Map<String, Object> prerequisitesConfigSet = prerequisiteConfigSection.getValues(false);
		Map<String, String> prerequisiteDataMap = new HashMap<String, String>(prerequisitesConfigSet.size());
		for (Entry<String, Object> entry : prerequisitesConfigSet.entrySet()) {
			prerequisiteDataMap.put(entry.getKey(), entry.getValue().toString());
		}
		
		return factorySet.getPrerequisiteFactory().createPrerequisiteSet(prerequisiteDataMap);
	}
	
	private Logged<RolePropertyMap<Role>> getRoles(ConfigurationSection rolesConfigSection, Role baseRole) {
		Log log = new Log();
		
		RolePropertyMap<Role> rolesMap = new RolePropertyMap<Role>();
		Set<String> roleNameStrings = rolesConfigSection.getKeys(false);
		List<String> rolesToResolve = new LinkedList<String>(roleNameStrings);
		
		rolesMap.put(baseRole);
		roleNameStrings.add(baseRole.getNameString());
		
		log.addInfo("Found " + roleNameStrings.size() + " roles");

		// Mill down roleNameStrings to resolve all resolvable role trees
		int lastChanged; // Keep track of how many roles were resolved. If this is 0 after a loop then there is a dependency loop
		while (rolesToResolve.size() > 0) {
			lastChanged = 0;
			
			// Loop through roles and resolve what can be resolved
			for (int i = rolesToResolve.size() - 1; i >= 0; i--) {
				String roleNameString = rolesToResolve.get(i);
				
				// Check name given in a valid format
				String invalidPartString = checkForInvalidCharacters(roleNameString);
				if (invalidPartString != null) {
					log.addError("Role name cannot contain '" + invalidPartString + "': '" + roleNameString + "' ");
					roleNameStrings.remove(roleNameString);
					rolesToResolve.remove(i);
					lastChanged++;
					continue;
				}
				
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

				// Collect parents
				Set<Role> parentRoles = new HashSet<Role>();
				for (String parentName : parentNames) {
					parentRoles.add(rolesMap.get(parentName));
				}
				
				Logged<Role> loggedRole = getRole(roleNameString, parentRoles, roleConfigSection);
				
				rolesMap.put(loggedRole.getResult());
				log.addNamedEntry(roleNameString, loggedRole.getLog());
				
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
		
		return new Logged<RolePropertyMap<Role>>(rolesMap, log);
	}
	
	private Logged<Role> getRole(String roleNameString, Set<Role> parentRoles, ConfigurationSection roleConfigSection) {
		Log log = new Log();
		
		// Gather parent scripts, states & triggers
		RolePropertyMap<Trigger> allTriggersMap = new RolePropertyMap<Trigger>();
		RolePropertyMap<Script> allScriptsMap = new RolePropertyMap<Script>();
		Log parentNamesLog = new Log();
		for (Role role : parentRoles) {
			allScriptsMap.putAll(role.getAllVisibleScripts());
			allTriggersMap.putAll(role.getAllVisibleTriggers());
			
			log.addInfo(role.getNameString());
		}
		log.addNamedEntry("Parents", parentNamesLog);
		
		// Generate states
		RolePropertyMap<State<?>> baseStatesMap = new RolePropertyMap<State<?>>();
		if (roleConfigSection.isConfigurationSection("states")) {
			ConfigurationSection roleStatesConfigSection = roleConfigSection.getConfigurationSection("states");
			Logged<RolePropertyMap<State<?>>> loggedStates = getStates(roleStatesConfigSection, roleNameString);
			baseStatesMap = loggedStates.getResult();
			log.addNamedEntry("States", loggedStates.getLog());
		}
		
		// Gather script commands
		RolePropertyMap<Script> roleScriptMap = new RolePropertyMap<Script>();
		if (roleConfigSection.isConfigurationSection("scripts")) {
			ConfigurationSection scriptStringsConfigSection = roleConfigSection.getConfigurationSection("scripts");
			Logged<RolePropertyMap<Script>> loggedRoleScriptMap = getScripts(scriptStringsConfigSection, allScriptsMap);
			roleScriptMap = loggedRoleScriptMap.getResult();
			log.addNamedEntry("Scripts", loggedRoleScriptMap.getLog());
		}
		allScriptsMap.putAll(roleScriptMap);
		
		// Generate role defined triggers
		RolePropertyMap<Trigger> roleTriggersMap = new RolePropertyMap<Trigger>(); 
		if (roleConfigSection.isConfigurationSection("triggers")) {
			ConfigurationSection triggersConfigSection = roleConfigSection.getConfigurationSection("triggers");
			Logged<RolePropertyMap<Trigger>> loggedRoleTriggersMap = getTriggers(triggersConfigSection);
			roleTriggersMap = loggedRoleTriggersMap.getResult();
			log.addNamedEntry("Triggers", loggedRoleTriggersMap.getLog());
		}
		allTriggersMap.putAll(roleTriggersMap);
		
		// Generate Dialogues
		DialogueMapping dialogueMap = new DialogueMapping();
		if (roleConfigSection.isConfigurationSection("dialogues")) {
			ConfigurationSection roleDialoguesConfigSection = roleConfigSection.getConfigurationSection("dialogues");
			Logged<DialogueMapping> loggedDialogueMap = getDialogues(roleDialoguesConfigSection, allTriggersMap, allScriptsMap);
			dialogueMap = loggedDialogueMap.getResult();
			log.addNamedEntry("Dialogues", loggedDialogueMap.getLog());
		}
		
		Role newRole = new Role(roleNameString, roleTriggersMap, parentRoles, dialogueMap, roleScriptMap, baseStatesMap);
		
		return new Logged<Role>(newRole, log);
	}
	
	private Logged<RolePropertyMap<Script>> getScripts(ConfigurationSection scriptStringsConfigSection, RolePropertyMap<Script> allScriptsMap) {
		Log log = new Log();
		
		Map<String, String> scriptStringsMap = new HashMap<String, String>();
		Set<String> scriptNameStrings = scriptStringsConfigSection.getKeys(false);
		
		for (String scriptNameString : scriptNameStrings) {
			// Check script name is valid
			String invalidPartString = checkForInvalidCharacters(scriptNameString);
			if (invalidPartString != null) {
				log.addError("Script name '" + scriptNameString + "' cannot contain " + invalidPartString);
				continue;
			}
			
			if (!scriptStringsConfigSection.isString(scriptNameString)) {
				log.addError("Script " + scriptNameString + " is not a string");
				continue;
			}
			
			scriptStringsMap.put(scriptNameString, scriptStringsConfigSection.getString(scriptNameString));
		}
		
		// Generate role specific scripts
		return factorySet.getScriptFactory().createConversationTree(scriptStringsMap, allScriptsMap);
	}
	
	private Logged<DialogueMapping> getDialogues(ConfigurationSection roleDialoguesConfigSection, RolePropertyMap<Trigger> triggers, RolePropertyMap<Script> allScriptMap) {
		Log log = new Log();
		
		DialogueMapping dialogueMap = new DialogueMapping();
		Set<String> roleTriggerNameStrings = roleDialoguesConfigSection.getKeys(false);
		
		for (String triggerNameString : roleTriggerNameStrings) {
			// Get trigger
			if (!triggers.containsKey(triggerNameString)) {
				log.addError("Trigger " + triggerNameString + " is not defined");
				continue;
			}
			
			// Get scripts
			Logged<WeightedSet<String>> loggedScriptNames = getDialogueScriptSet(roleDialoguesConfigSection, triggerNameString, allScriptMap);
			
			WeightedSet<String> scriptNames = loggedScriptNames.getResult();
			log.addNamedEntry(triggerNameString, loggedScriptNames.getLog());
			
			if (scriptNames.size() == 0) {
				continue;
			}
			
			dialogueMap.put(triggerNameString, scriptNames);
		}
		
		return new Logged<DialogueMapping>(dialogueMap, log);
	}
	
	private List<String> getRoleParentNames(ConfigurationSection roleConfigSection, Role baseRole) {
		if (roleConfigSection.contains("parents")) {
			return roleConfigSection.getStringList("parents");
		}
		
		List<String> parentNames = new ArrayList<String>();
		parentNames.add(baseRole.getNameString());
		return parentNames;
	}

	/**
	 * Gets the names of the scripts defined under a trigger name in the dialogue section
	 * @param dialoguesConfig the section containing all dialogues
	 * @param triggerName The name of the current dialogue
	 * @param scriptMap All of the scripts visible in the role
	 * @return A weighted set of all of the names of the scripts to bind to the event
	 */
	private Logged<WeightedSet<String>> getDialogueScriptSet(ConfigurationSection dialoguesConfig, String triggerName, RolePropertyMap<Script> scriptMap) {
		Log log = new Log();
		
		WeightedSet<String> roleTriggerScripts = new WeightedSet<String>();
		
		// A single string
		if (dialoguesConfig.isString(triggerName)) {
			String scriptNameString = dialoguesConfig.getString(triggerName);
			
			if (!scriptMap.containsKey(scriptNameString)) {
				log.addError("Script " + scriptNameString + " is not defined");
			} else {
				log.addInfo("Single script " + scriptNameString);
				
				roleTriggerScripts.addEntry(scriptNameString, 1);
			}
			
	    // A list of scripts
		} else if (dialoguesConfig.isList(triggerName)) {
			int totalWeight = 0;
			
			// Get scripts with weights
			List<Map<?, ?>> weightedScriptNames = dialoguesConfig.getMapList(triggerName);
			
			Log scriptNamesLog = new Log();
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
						log.addError("Given weight is 0 or less: " + weight);
						continue;
					}
					
					scriptNamesLog.addInfo(scriptName + ", weight: " + weight);
					
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
						scriptNamesLog.addInfo(scriptNameString);
					} else {
						scriptNamesLog.addInfo(scriptNameString + ", weight: " + uniformWeights);
					}
					
					roleTriggerScripts.addEntry(scriptNameString, uniformWeights);
				}
			}
			
			log.addNamedEntry("Script List", scriptNamesLog);
		} else {
			log.addError("Unsupported dialogue config type");
		}

		return new Logged<WeightedSet<String>>(roleTriggerScripts, log);
	}
	
	private static String checkForInvalidCharacters(String name) {
		for (String string : INVALID_NAME_STRINGS) {
			if (name.equalsIgnoreCase(string) || name.contains(string)) {
				return string;
			}
		}
		
		return null;
	}

	public void setDefaultTriggerPriority(int defaultEventPriority) {
		this.defaultEventPriority = defaultEventPriority;
	}
}
