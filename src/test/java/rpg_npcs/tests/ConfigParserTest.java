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

import com.sun.org.apache.bcel.internal.generic.NEW;

import rpg_npcs.ConfigParser;
import rpg_npcs.DialogueMap;
import rpg_npcs.Role;
import rpg_npcs.TriggerMap;
import rpg_npcs.WeightedSet;
import rpg_npcs.ConfigParser.ConfigResult;
import rpg_npcs.script.ScriptMap;
import rpg_npcs.trigger.Trigger;

public class ConfigParserTest {

	@Test
	public void emptyConfigBaseRoleTest() {
		Configuration testConfigurationSection = new MemoryConfiguration();
		
		ConfigResult result = ConfigParser.reloadConfig(ScriptFactoryTest.getTestableEmptyScriptFactory(), testConfigurationSection);
		
		assertEquals(result.log.getFormattedString(), 0, result.log.errorCount());
		assertEquals(1, result.rolesMap.size());
		assertTrue(result.rolesMap.containsKey(Role.DEFAULT_ROLE_NAME_STRING));
		
		Role baseRole = result.rolesMap.get(Role.DEFAULT_ROLE_NAME_STRING);
		
		assertEquals(Role.DEFAULT_ROLE_NAME_STRING, baseRole.roleName);
		assertEquals(0, baseRole.getAllScripts().size());
		assertEquals(0, baseRole.getImmediateParentRoles().size());
		assertEquals(0, baseRole.getDialogueNamesMap().size());
		assertEquals(0, baseRole.getAllVisibleTriggers().size());
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
			List<String> roleNames = new ArrayList<String>(roles + 1);
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
					scriptsConfigurationSection.set(scriptNameString, "");
					roleScriptSet.add(scriptNameString);
				}
			}
			
			// Generate result
			ConfigResult result = ConfigParser.reloadConfig(ScriptFactoryTest.getTestableEmptyScriptFactory(), testConfigurationSection);
			
			// Test
			assertEquals(result.log.getFormattedString(), 0, result.log.errorCount());
			
			// Check roles are present
			assertEquals(roles + 1, result.rolesMap.size());
			
			assertTrue(result.rolesMap.keySet().containsAll(roleNames));
			
			// Check scripts are present
			for (int i = 0; i < roles; i++) {
				String roleNameString = roleNames.get(i);
				Role role = result.rolesMap.get(roleNameString);
				ScriptMap roleScriptsMap = role.getAllScripts();
				Set<String> roleScriptSet = roleScriptSets.get(i);

				assertEquals(1, role.getImmediateParentRoles().size());
				assertEquals(0, role.getAllVisibleTriggers().size());
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
		assertEquals(result.log.getFormattedString(), 0, result.log.errorCount());
		assertEquals(1, result.rolesMap.size());
		
		Role baseRole = result.rolesMap.get(Role.DEFAULT_ROLE_NAME_STRING);
		TriggerMap triggerMap = baseRole.getAllVisibleTriggers();
		
		assertEquals(triggerCount, triggerMap.size());

		assertTrue(triggerMap.keySet().containsAll(triggerNameSet));

		assertEquals(111, triggerMap.get("trigger0").getPriority());
		assertEquals(111, triggerMap.get("trigger1").getPriority());
		assertEquals(111, triggerMap.get("trigger2").getPriority());
		assertEquals(4, triggerMap.get("trigger3").getPriority());
		assertEquals(5, triggerMap.get("trigger4").getPriority());
		assertEquals(6, triggerMap.get("trigger5").getPriority());

		assertEquals(0, triggerMap.get("trigger0").getPrerequisites().size());
		assertEquals(0, triggerMap.get("trigger1").getPrerequisites().size());
		assertEquals(1, triggerMap.get("trigger2").getPrerequisites().size());
		assertEquals(0, triggerMap.get("trigger3").getPrerequisites().size());
		assertEquals(0, triggerMap.get("trigger4").getPrerequisites().size());
		assertEquals(1, triggerMap.get("trigger5").getPrerequisites().size());
	}
	
	ConfigResult roleDialoguesResult;
	int roleDialoguesTriggerCount = 6;
	List<WeightedSet<String>> roleDialoguesSets;
	Role roleDialoguesBaseRole;
	
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
		
		ConfigurationSection roleScriptsSection = testConfigurationSection.createSection("scripts");
		roleScriptsSection.set("script1", "");
		roleScriptsSection.set("script2", "");
		roleScriptsSection.set("script3", "");
		roleScriptsSection.set("script4", "");
		roleScriptsSection.set("script5", "");
		
		ConfigurationSection roleDialoguesSection = testConfigurationSection.createSection("dialogues");

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
		roleDialoguesBaseRole = roleDialoguesResult.rolesMap.get(Role.DEFAULT_ROLE_NAME_STRING);
		DialogueMap dialogueMap = roleDialoguesBaseRole.getDialogueNamesMap();
		
		roleDialoguesSets = new ArrayList<WeightedSet<String>>(roleDialoguesTriggerCount);
		for (int i = 0; i < roleDialoguesTriggerCount; i++) {
			String triggerNameString = triggerNameStrings[i];
			roleDialoguesSets.add(dialogueMap.get(triggerNameString));
		}
	}

	@Test
	public void roleDialoguesConfigTest() {
		// Test
		assertEquals(roleDialoguesResult.log.getFormattedString(), 0, roleDialoguesResult.log.errorCount());
		assertEquals(roleDialoguesTriggerCount, roleDialoguesBaseRole.getDialogueNamesMap().size());
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
	public void roleParentsSingleInheritanceConfigTest() {
		/*
		 * Test:
		 * 
		 *    base
		 *      |
		 *      a
		 *     / \
		 *    b   c
		 *    |
		 *    d
		 * 
		 * base:
		 * script1: ""
		 * trigger1: playermove
		 * trigger1 -> script1
		 * 
		 * a:
		 * trigger2: playermove
		 * trigger2 -> script1 (2)
		 * 
		 * b:
		 * script1: "" (1)
		 * script2: ""
		 * trigger1 -> script2 (3)
		 * trigger2 -> script2 (4)
		 * 
		 * c: (6)
		 * 
		 * d:
		 * trigger2 -> a.script1 (5)
		 * 
		 * Sub Tests:
		 * 1. script overriding
		 * 2. inherited script referencing
		 * 3. inherited trigger referencing
		 * 4: dialogue overriding
		 * 5: namespaces
		 * 6: multi-depth inheritance
		 */
		
		Configuration testConfigurationSection = new MemoryConfiguration();
		ConfigurationSection rolesConfigurationSection = testConfigurationSection.createSection("roles");

		ConfigurationSection baseScriptsConfigurationSection = testConfigurationSection.createSection("scripts");
		ConfigurationSection baseTriggersConfigurationSection = testConfigurationSection.createSection("triggers");
		ConfigurationSection baseDialoguesConfigurationSection = testConfigurationSection.createSection("dialogues");
		
		baseScriptsConfigurationSection.set("script1", "");

		ConfigurationSection trigger1ConfigurationSection = baseTriggersConfigurationSection.createSection("trigger1");
		trigger1ConfigurationSection.set("type", "playermove");
		
		baseDialoguesConfigurationSection.set("trigger1", "script1");
		
		ConfigurationSection roleConfigurationSectionA = rolesConfigurationSection.createSection("a");
		ConfigurationSection roleConfigurationSectionB = rolesConfigurationSection.createSection("b");
		ConfigurationSection roleConfigurationSectionC = rolesConfigurationSection.createSection("c");
		ConfigurationSection roleConfigurationSectionD = rolesConfigurationSection.createSection("c");

		// A
		ConfigurationSection triggersConfigurationSectionA = roleConfigurationSectionA.createSection("triggers");
		ConfigurationSection trigger2ConfigurationSection = triggersConfigurationSectionA.createSection("trigger2");
		trigger2ConfigurationSection.set("type", "playermove");
		
		ConfigurationSection dialoguesConfigurationSectionA = roleConfigurationSectionA.createSection("dialogues");
		dialoguesConfigurationSectionA.set("trigger1", "script1");

		// B
		roleConfigurationSectionB.set("parents", Arrays.asList(new String[] {"a"}));
		
		ConfigurationSection scriptsConfigurationSectionB = roleConfigurationSectionB.createSection("scripts");
		scriptsConfigurationSectionB.set("script1", "");
		scriptsConfigurationSectionB.set("script2", "");
		
		ConfigurationSection dialoguesConfigurationSectionb = roleConfigurationSectionB.createSection("dialogues");
		dialoguesConfigurationSectionb.set("trigger1", "script2");
		dialoguesConfigurationSectionb.set("trigger2", "script2");
		
		// C
		roleConfigurationSectionC.set("parents", Arrays.asList(new String[] {"a"}));
		
		// D
		roleConfigurationSectionD.set("parents", Arrays.asList(new String[] {"b"}));
		
		ConfigurationSection dialoguesConfigurationSectiond = roleConfigurationSectionD.createSection("dialogues");
		dialoguesConfigurationSectiond.set("trigger2", "a.script1");
		
		
		// Generate result
		ConfigResult result = ConfigParser.reloadConfig(ScriptFactoryTest.getTestableEmptyScriptFactory(), testConfigurationSection);
		
		Role baseRole = result.rolesMap.get(Role.DEFAULT_ROLE_NAME_STRING);
		Role roleA = result.rolesMap.get("a");
		Role roleB = result.rolesMap.get("b");
		Role roleC = result.rolesMap.get("c");
		Role roleD = result.rolesMap.get("d");
		
		// Test
		assertEquals(result.log.getFormattedString(), 0, result.log.errorCount());
		
		// 1
		assertNotSame(baseRole.getAllScripts().get("script1"), roleB.getAllScripts().get("script1"));
		
		// 2
		Trigger roleATrigger2 = roleA.getAllVisibleTriggers().get("trigger2");
		assertSame(baseRole.getAllScripts().get("script1"), roleA.getDialogueMap().get(roleATrigger2).findEntryWithWeight(0));
		
		// 3
		Trigger roleBTrigger1 = roleB.getAllVisibleTriggers().get("trigger1");
		assertSame(roleB.getAllScripts().get("script2"), roleB.getDialogueMap().get(roleBTrigger1).findEntryWithWeight(0));
		
		// 4
		Trigger roleBTrigger2 = roleB.getAllVisibleTriggers().get("trigger2");
		assertSame(roleB.getAllScripts().get("script2"), roleB.getDialogueMap().get(roleBTrigger2).findEntryWithWeight(0));
		
		// 5
		Trigger roleDTrigger2 = roleD.getAllVisibleTriggers().get("trigger2");
		assertSame(baseRole.getAllScripts().get("script1"), roleD.getDialogueMap().get(roleDTrigger2).findEntryWithWeight(0));
		
		// 6
		assertTrue(baseRole.getAllScripts().equals(roleC.getAllScripts()));
		assertSame(baseRole.getAllVisibleTriggers().get("trigger1"), roleC.getAllVisibleTriggers().get("trigger1"));
	}
}
