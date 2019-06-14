package rpg_npcs.state;

import java.util.HashMap;
import java.util.Map;

public class SupportedStateTypeRecords {
	private final Map<String, SupportedStateType<?>> supportedStateTypes = new HashMap<String, SupportedStateType<?>>();
	
	public void addSupportedType(SupportedStateType<?> type) {
		supportedStateTypes.put(type.getDataTypeName().toLowerCase(), type);
	}
	
	public SupportedStateType<?> get(String name) {
		return supportedStateTypes.get(name.toLowerCase());
	}
}
