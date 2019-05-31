package rpg_npcs;

import java.util.HashMap;

public class RoleMap extends HashMap<String, Role> {

	private static final long serialVersionUID = 8674058045156483244L;

	public void put(Role role) {
		this.put(role.roleName, role);
	}
}
