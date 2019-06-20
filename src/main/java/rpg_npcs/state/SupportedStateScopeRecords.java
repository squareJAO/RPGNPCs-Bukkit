package rpg_npcs.state;

import java.util.HashMap;
import java.util.Map;

public class SupportedStateScopeRecords {
	private final Map<String, StateScope> supportedStateTypes = new HashMap<String, StateScope>();
	
	public void addSupportedType(StateScope type) {
		supportedStateTypes.put(type.getNameString().toLowerCase(), type);
	}
	
	public StateScope get(String name) {
		return supportedStateTypes.get(name.toLowerCase());
	}
}
