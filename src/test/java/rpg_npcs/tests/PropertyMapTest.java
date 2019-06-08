package rpg_npcs.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import rpg_npcs.role.RolePropertyMap;
import rpg_npcs.trigger.Trigger;

public class PropertyMapTest {

	@Test
	public void copyTest() {
		RolePropertyMap<Trigger> map1 = new RolePropertyMap<Trigger>();
		map1.put("trigger1", null);
		RolePropertyMap<Trigger> map2 = map1.copy();
		map2.put("trigger2", null);
		
		assertTrue(map1.containsKey("trigger1"));
		assertTrue(map2.containsKey("trigger1"));
		
		assertFalse(map1.containsKey("trigger2"));
		assertTrue(map2.containsKey("trigger2"));
	}

}
