package rpg_npcs.role;

import java.util.HashMap;

public class RolePropertyMap<T extends RoleNamedProperty> extends HashMap<String, T> {

	private static final long serialVersionUID = -642777224009927595L;
	
	public void put(T property) {
		this.put(property.nameString, property);
	}

	public void putAll(RolePropertyMap<T> newProperties, String prefixString) {
		for (String newName : newProperties.keySet()) {
			T newPrperty = newProperties.get(newName);
			this.put(prefixString + newName, newPrperty);
		}
	}

	public RolePropertyMap<T> copy() {
		RolePropertyMap<T> copyMap = new RolePropertyMap<T>();
		copyMap.putAll(this);
		return copyMap;
	}
}
