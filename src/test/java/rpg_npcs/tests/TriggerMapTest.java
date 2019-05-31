package rpg_npcs.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import rpg_npcs.TriggerMap;

public class TriggerMapTest {

	@Test
	public void copyTest() {
		TriggerMap map1 = new TriggerMap();
		map1.put("trigger1", null);
		TriggerMap map2 = map1.copy();
		map2.put("trigger2", null);
		
		assertTrue(map1.containsKey("trigger1"));
		assertTrue(map2.containsKey("trigger1"));
		
		assertFalse(map1.containsKey("trigger2"));
		assertTrue(map2.containsKey("trigger2"));
	}

}
