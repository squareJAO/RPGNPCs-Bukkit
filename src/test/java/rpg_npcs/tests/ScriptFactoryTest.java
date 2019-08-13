package rpg_npcs.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import rpg_npcs.ParserFactorySet;
import rpg_npcs.logging.Logged;
import rpg_npcs.role.RolePropertyMap;
import rpg_npcs.script.Script;

public class ScriptFactoryTest {
	@Test
	public void emptyTest() {
		// Generate data
		Logged<RolePropertyMap<Script>> resultState = (new ParserFactorySet(1)).getScriptFactory().createConversationTree(new HashMap<String, String>(), new RolePropertyMap<Script>());
		
		// Test
		assertEquals(0, resultState.getLog().countErrors());
		assertEquals(0, resultState.getResult().size());
	}
	
	@Test
	public void simpleStringTest() {
		// Generate data
		Map<String, String> scriptMap = new HashMap<String, String>();
		scriptMap.put("script1", "testText1");
		Logged<RolePropertyMap<Script>> resultState = (new ParserFactorySet(1)).getScriptFactory().createConversationTree(scriptMap, new RolePropertyMap<Script>());
		
		// Test
		assertEquals(0, resultState.getLog().countErrors());
		assertEquals(1, resultState.getResult().size());
		assertTrue(resultState.getResult().containsKey("script1"));
		
		Script script1 = resultState.getResult().get("script1");
		assertEquals("script1", script1.getNameString());
	}
}
