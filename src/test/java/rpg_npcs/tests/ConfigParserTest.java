package rpg_npcs.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.Before;
import org.junit.Test;

import rpg_npcs.ConfigParser;
import rpg_npcs.Role;
import rpg_npcs.WeightedSet;
import rpg_npcs.ConfigParser.ConfigResult;
import rpg_npcs.script.ScriptMap;
import rpg_npcs.trigger.Trigger;

public class ConfigParserTest {

	@Test
	public void emptyConfigTest() {
		Configuration testConfigurationSection = new MemoryConfiguration();
		
		ConfigResult result = ConfigParser.reloadConfig(ScriptFactoryTest.getTestableEmptyScriptFactory(), testConfigurationSection);
		
		assertEquals(0, result.log.errorCount());
		assertEquals(0, result.triggerMap.size());
		assertEquals(0, result.rolesMap.size());
	}

	@Test
	public void roleBaseScriptsConfigTest() {
		/* Create batches of 0 to x roles
		 * Each batch has n roles each with 0 to n scripts
		 * role 0 has 0 scripts, etc.
		 */
		
		for (int roles = 0; roles < 100; roles++) {
			// Create test config data
			Configuration testConfigurationSection = new MemoryConfiguration();
			ConfigurationSection roleConfigurationSection = testConfigurationSection.createSection("roles");
			
			ConfigurationSection[] roleConfigurationSections = new ConfigurationSection[roles];
			List<String> roleNames = new ArrayList<String>(roles);
			List<Set<String>> roleScriptSets = new ArrayList<Set<String>>(roles);
			
			for (int role = 0; role < roles; role++) {
				String roleNameString = "role" + role;
				roleConfigurationSections[role] = roleConfigurationSection.createSection(roleNameString);
				roleNames.add(roleNameString);
				
				ConfigurationSection scriptsConfigurationSection = roleConfigurationSections[role].createSection("scripts");

				Set<String> roleScriptSet = new HashSet<String>();
				roleScriptSets.add(roleScriptSet);
				
				for (int script = 0; script < role; script++) {
					String scriptNameString = roleNameString + "script" + script;
					scriptsConfigurationSection.set(scriptNameString, "testScript");
					roleScriptSet.add(scriptNameString);
				}
			}
			
			// Generate result
			ConfigResult result = ConfigParser.reloadConfig(ScriptFactoryTest.getTestableEmptyScriptFactory(), testConfigurationSection);
			
			// Test
			assertEquals(0, result.log.errorCount());
			assertEquals(0, result.triggerMap.size());
			
			// Check roles are present
			assertEquals(roles, result.rolesMap.size());
			
			assertTrue(result.rolesMap.keySet().containsAll(roleNames));
			assertTrue(roleNames.containsAll(result.rolesMap.keySet()));
			
			// Check scripts are present
			for (int i = 0; i < roles; i++) {
				String roleNameString = roleNames.get(i);
				Role role = result.rolesMap.get(roleNameString);
				ScriptMap roleScriptsMap = role.getAllScripts();
				Set<String> roleScriptSet = roleScriptSets.get(i);

				assertEquals(0, role.parentRoles.size());
				assertEquals(roleNameString, role.roleName);
				assertEquals(2 * roleScriptSet.size(), roleScriptsMap.size());
				
				for (String scriptName : roleScriptSet) {
					assertTrue(roleScriptsMap.containsKey(scriptName));
					assertTrue(roleScriptsMap.containsKey(roleNameString + "." + scriptName));
				}
			}
		}
	}

	@Test
	public void triggerConfigTest() {
		// Create test data
		Configuration testConfigurationSection = new MemoryConfiguration();
		testConfigurationSection.set("defaultEventPriority", 111);
		ConfigurationSection triggersConfigurationSection = testConfigurationSection.createSection("triggers");
		
		int triggerCount = 6;
		ConfigurationSection[] triggerConfigurationSections = new ConfigurationSection[triggerCount];
		Set<String> triggerNameSet = new HashSet<String>();
		for (int i = 0; i < triggerCount; i++) {
			String tiggerNameString = "trigger" + i;
			triggerConfigurationSections[i] = triggersConfigurationSection.createSection(tiggerNameString);
			triggerNameSet.add(tiggerNameString);
		}
		
		triggerConfigurationSections[0].set("type", "playermove");
		
		triggerConfigurationSections[1].set("type", "playermove");
		triggerConfigurationSections[1].createSection("prerequisites");

		triggerConfigurationSections[2].set("type", "playermove");
		ConfigurationSection trigger3PrerequisitesSection = triggerConfigurationSections[2].createSection("prerequisites");
		trigger3PrerequisitesSection.set("range", 5);
		
		triggerConfigurationSections[3].set("type", "playermove");
		triggerConfigurationSections[3].set("priority", 4);
		
		triggerConfigurationSections[4].set("type", "playermove");
		triggerConfigurationSections[4].set("priority", 5);
		triggerConfigurationSections[4].createSection("prerequisites");

		triggerConfigurationSections[5].set("type", "playermove");
		triggerConfigurationSections[5].set("priority", 6);
		ConfigurationSection trigger6PrerequisitesSection = triggerConfigurationSections[5].createSection("prerequisites");
		trigger6PrerequisitesSection.set("range", 5);
		
		// Generate result
		ConfigResult result = ConfigParser.reloadConfig(ScriptFactoryTest.getTestableEmptyScriptFactory(), testConfigurationSection);
		
		// Test
		assertEquals(0, result.log.errorCount());
		assertEquals(0, result.rolesMap.size());
		
		assertEquals(triggerCount, result.triggerMap.size());

		assertTrue(result.triggerMap.keySet().containsAll(triggerNameSet));

		assertEquals(111, result.triggerMap.get("trigger0").getPriority());
		assertEquals(111, result.triggerMap.get("trigger1").getPriority());
		assertEquals(111, result.triggerMap.get("trigger2").getPriority());
		assertEquals(4, result.triggerMap.get("trigger3").getPriority());
		assertEquals(5, result.triggerMap.get("trigger4").getPriority());
		assertEquals(6, result.triggerMap.get("trigger5").getPriority());

		assertEquals(0, result.triggerMap.get("trigger0").getPrerequisites().size());
		assertEquals(0, result.triggerMap.get("trigger1").getPrerequisites().size());
		assertEquals(1, result.triggerMap.get("trigger2").getPrerequisites().size());
		assertEquals(0, result.triggerMap.get("trigger3").getPrerequisites().size());
		assertEquals(0, result.triggerMap.get("trigger4").getPrerequisites().size());
		assertEquals(1, result.triggerMap.get("trigger5").getPrerequisites().size());
	}
	
	ConfigResult roleDialoguesResult;
	int roleDialoguesTriggerCount = 6;
	List<WeightedSet<String>> roleDialoguesSets;
	
	@Before
	public void roleDialoguesConfigSetup() {
		// Create test data
		Configuration testConfigurationSection = new MemoryConfiguration();
		
		ConfigurationSection triggersConfigurationSection = testConfigurationSection.createSection("triggers");
		
		String[] triggerNameStrings = new String[roleDialoguesTriggerCount];
		for (int i = 0; i < roleDialoguesTriggerCount; i++) {
			triggerNameStrings[i] = "trigger" + (i + 1);
			ConfigurationSection triggerConfigurationSection = triggersConfigurationSection.createSection(triggerNameStrings[i]);
			triggerConfigurationSection.set("type", "playermove");
		}
		
		ConfigurationSection rolesConfigurationSection = testConfigurationSection.createSection("roles");
		
		ConfigurationSection roleConfigurationSection = rolesConfigurationSection.createSection("role");
		ConfigurationSection roleScriptsSection = roleConfigurationSection.createSection("scripts");
		roleScriptsSection.set("script1", "testScript");
		roleScriptsSection.set("script2", "testScript");
		roleScriptsSection.set("script3", "testScript");
		roleScriptsSection.set("script4", "testScript");
		roleScriptsSection.set("script5", "testScript");
		
		ConfigurationSection roleDialoguesSection = roleConfigurationSection.createSection("dialogues");

		// trigger1
		roleDialoguesSection.set("trigger1", "script1");

		// trigger2
		roleDialoguesSection.set("trigger2", Arrays.asList(new String[] {"script1"}));

		// trigger3
		roleDialoguesSection.set("trigger3", Arrays.asList(new String[] {"script1", "script2"}));

		// trigger4
		Object[] trigger4Objects = new Object[] {
				"script1", "script2", "script3"
		};
		roleDialoguesSection.set("trigger4", Arrays.asList(trigger4Objects));

		// trigger5
		Map<String, Integer> trigger5WeightedScript1 = new HashMap<String, Integer>();
		trigger5WeightedScript1.put("script3", 70);
		Object[] trigger5Objects = new Object[] {
				"script1", "script2", trigger5WeightedScript1, "script4"
		};
		roleDialoguesSection.set("trigger5", Arrays.asList(trigger5Objects));

		// trigger6
		Map<String, Integer> trigger6WeightedScript1 = new HashMap<String, Integer>();
		trigger6WeightedScript1.put("script1", 10);
		Map<String, Integer> trigger6WeightedScript2 = new HashMap<String, Integer>();
		trigger6WeightedScript1.put("script2", 20);
		Map<String, Integer> trigger6WeightedScript3 = new HashMap<String, Integer>();
		trigger6WeightedScript1.put("script3", 30);
		Map<String, Integer> trigger6WeightedScript4 = new HashMap<String, Integer>();
		trigger6WeightedScript1.put("script4", 40);
		Map<String, Integer> trigger6WeightedScript5 = new HashMap<String, Integer>();
		trigger6WeightedScript1.put("script5", 50);
		Object[] trigger6Objects = new Object[] {
				trigger6WeightedScript1, trigger6WeightedScript2, trigger6WeightedScript3, trigger6WeightedScript4, trigger6WeightedScript5
		};
		roleDialoguesSection.set("trigger6", Arrays.asList(trigger6Objects));
		
		// Generate result
		roleDialoguesResult = ConfigParser.reloadConfig(ScriptFactoryTest.getTestableEmptyScriptFactory(), testConfigurationSection);
		
		// Extract data
		Role role = roleDialoguesResult.rolesMap.get("role");
		Map<Trigger, WeightedSet<String>> triggerMap = role.getTriggerMap();
		
		roleDialoguesSets = new ArrayList<WeightedSet<String>>(roleDialoguesTriggerCount);
		for (int i = 0; i < roleDialoguesTriggerCount; i++) {
			String triggerNameString = triggerNameStrings[i];
			Trigger trigger = roleDialoguesResult.triggerMap.get(triggerNameString);
			roleDialoguesSets.add(triggerMap.get(trigger));
		}
	}

	@Test
	public void roleDialoguesConfigTest() {
		// Test
		assertEquals(0, roleDialoguesResult.log.errorCount());
		assertEquals(roleDialoguesTriggerCount, roleDialoguesResult.triggerMap.size());
		assertEquals(1, roleDialoguesResult.rolesMap.size());
	}

	@Test
	public void roleDialoguesConfigTestTrigger1() {
		// trigger1
		assertEquals(1, roleDialoguesSets.get(0).size());
		assertEquals("script1", roleDialoguesSets.get(0).findEntryWithWeight(0));
	}

	@Test
	public void roleDialoguesConfigTestTrigger2() {
		// trigger2
		assertEquals(1, roleDialoguesSets.get(1).size());
		assertEquals("script1", roleDialoguesSets.get(1).findEntryWithWeight(0));
	}

	@Test
	public void roleDialoguesConfigTestTrigger3() {
		// trigger3
		assertEquals(2, roleDialoguesSets.get(2).size());
		Map<String, Integer> set3EntryMap = roleDialoguesSets.get(2).getEntryMap();
		assertTrue(set3EntryMap.keySet().contains("script1"));
		assertTrue(set3EntryMap.keySet().contains("script2"));
		assertEquals(set3EntryMap.get("script1"), set3EntryMap.get("script2"));
	}

	@Test
	public void roleDialoguesConfigTestTrigger4() {
		// trigger4
		assertEquals(3, roleDialoguesSets.get(3).size());
		Map<String, Integer> set4EntryMap = roleDialoguesSets.get(3).getEntryMap();
		assertTrue(set4EntryMap.keySet().contains("script1"));
		assertTrue(set4EntryMap.keySet().contains("script2"));
		assertTrue(set4EntryMap.keySet().contains("script3"));
		assertEquals(set4EntryMap.get("script1"), set4EntryMap.get("script2"));
		assertEquals(set4EntryMap.get("script1"), set4EntryMap.get("script3"));
	}

	@Test
	public void roleDialoguesConfigTestTrigger5() {
		// trigger5
		assertEquals(4, roleDialoguesSets.get(4).size());
		Map<String, Integer> set5EntryMap = roleDialoguesSets.get(4).getEntryMap();
		assertTrue(set5EntryMap.keySet().contains("script1"));
		assertTrue(set5EntryMap.keySet().contains("script2"));
		assertTrue(set5EntryMap.keySet().contains("script3"));
		assertTrue(set5EntryMap.keySet().contains("script4"));
		assertEquals(Integer.valueOf(10), set5EntryMap.get("script1"));
		assertEquals(Integer.valueOf(10), set5EntryMap.get("script2"));
		assertEquals(Integer.valueOf(70), set5EntryMap.get("script3"));
		assertEquals(Integer.valueOf(10), set5EntryMap.get("script4"));
	}

	@Test
	public void roleDialoguesConfigTestTrigger6() {
		// trigger6
		assertEquals(5, roleDialoguesSets.get(5).size());
		Map<String, Integer> set6EntryMap = roleDialoguesSets.get(5).getEntryMap();
		assertTrue(set6EntryMap.keySet().contains("script1"));
		assertTrue(set6EntryMap.keySet().contains("script2"));
		assertTrue(set6EntryMap.keySet().contains("script3"));
		assertTrue(set6EntryMap.keySet().contains("script4"));
		assertTrue(set6EntryMap.keySet().contains("script5"));
		assertEquals(Integer.valueOf(10), set6EntryMap.get("script1"));
		assertEquals(Integer.valueOf(20), set6EntryMap.get("script2"));
		assertEquals(Integer.valueOf(30), set6EntryMap.get("script3"));
		assertEquals(Integer.valueOf(40), set6EntryMap.get("script4"));
		assertEquals(Integer.valueOf(50), set6EntryMap.get("script5"));
	}

	@Test
	public void roleParentsConfigTest() {
		/*
		 * Tests:
		 * 1.   a
		 *     /
		 *    b
		 * 
		 * 2.   a
		 *     / \
		 *    b   c
		 * 
		 * 3.   a
		 *     /
		 *    b
		 *     \
		 *      c
		 *      
		 * Sub Tests
		 * a. script overriding
		 * b. 
		 */
	}
}
