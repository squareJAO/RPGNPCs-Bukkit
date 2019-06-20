package rpg_npcs.state;

import java.util.HashMap;
import java.util.Map;

public class SupportedStateTypeRecords {
	private final Map<String, StateType<?>> supportedStateTypes = new HashMap<String, StateType<?>>();
	
	public void addSupportedType(StateType<?> type) {
		supportedStateTypes.put(type.getDataTypeName().toLowerCase(), type);
	}
	
	public StateType<?> get(String name) {
		return supportedStateTypes.get(name.toLowerCase());
	}
}
