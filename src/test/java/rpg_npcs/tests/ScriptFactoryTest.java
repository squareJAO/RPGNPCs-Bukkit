package rpg_npcs.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import rpg_npcs.script.ScriptFactory;
import rpg_npcs.script.factoryPart.ScriptFactoryPart;

public class ScriptFactoryTest {
	
	public static ScriptFactory getTestableEmptyScriptFactory() {
		return new ScriptFactory(new ScriptFactoryPart[0], 1, 10, "");
	}

}
