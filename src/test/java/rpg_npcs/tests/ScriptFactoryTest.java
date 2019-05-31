package rpg_npcs.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import rpg_npcs.script.Script;
import rpg_npcs.script.ScriptFactory;
import rpg_npcs.script.ScriptFactoryState;
import rpg_npcs.script.ScriptMap;
import rpg_npcs.script.factoryPart.ScriptFactoryPart;

public class ScriptFactoryTest {
	
	public static ScriptFactory getTestableEmptyScriptFactory() {
		return new ScriptFactory(new ScriptFactoryPart[0], 1, 10, "");
	}
	
	@Test
	public void emptyTest() {
		// Generate data
		ScriptFactory emptyScriptFactory = getTestableEmptyScriptFactory();
		ScriptFactoryState resultState = emptyScriptFactory.createConversationTree(new HashMap<String, String>(), new ScriptMap());
		
		// Test
		assertEquals(0, resultState.log.errorCount());
		assertEquals(0, resultState.getAllScripts().size());
	}
	
	@Test
	public void simpleStringTest() {
		// Generate data
		ScriptFactory emptyScriptFactory = getTestableEmptyScriptFactory();
		Map<String, String> scriptMap = new HashMap<String, String>();
		scriptMap.put("script1", "testText1");
		ScriptFactoryState resultState = emptyScriptFactory.createConversationTree(scriptMap, new ScriptMap());
		
		// Test
		assertEquals(0, resultState.log.errorCount());
		assertEquals(1, resultState.getAllScripts().size());
		assertTrue(resultState.doesScriptExist("script1"));
		
		Script script1 = resultState.getScript("script1");
		assertEquals("script1", script1.nameString);
	}
}
