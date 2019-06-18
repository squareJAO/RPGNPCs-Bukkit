package rpg_npcs.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import rpg_npcs.ParserFactorySet;
import rpg_npcs.role.RolePropertyMap;
import rpg_npcs.script.Script;
import rpg_npcs.script.ScriptFactoryState;

public class ScriptFactoryTest {
	@Test
	public void emptyTest() {
		// Generate data
		ScriptFactoryState resultState = (new ParserFactorySet()).getScriptFactory().createConversationTree(new HashMap<String, String>(), new RolePropertyMap<Script>());
		
		// Test
		assertEquals(0, resultState.log.errorCount());
		assertEquals(0, resultState.getAllScripts().size());
	}
	
	@Test
	public void simpleStringTest() {
		// Generate data
		Map<String, String> scriptMap = new HashMap<String, String>();
		scriptMap.put("script1", "testText1");
		ScriptFactoryState resultState = (new ParserFactorySet()).getScriptFactory().createConversationTree(scriptMap, new RolePropertyMap<Script>());
		
		// Test
		assertEquals(0, resultState.log.errorCount());
		assertEquals(1, resultState.getAllScripts().size());
		assertTrue(resultState.doesScriptExist("script1"));
		
		Script script1 = resultState.getScript("script1");
		assertEquals("script1", script1.nameString);
	}
}
