package rpg_npcs.tests;

import rpg_npcs.state.StateFactory;
import rpg_npcs.state.SupportedStateTypeRecords;

public class StateFactoryTest {
	
	public static StateFactory getTestableEmptyStateFactory() {
		return new StateFactory(new SupportedStateTypeRecords());
	}
}
