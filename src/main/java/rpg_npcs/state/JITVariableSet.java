package rpg_npcs.state;

import org.bukkit.OfflinePlayer;

import com.fathzer.soft.javaluator.AbstractVariableSet;

import rpg_npcs.RpgNpc;
import rpg_npcs.role.RolePropertyMap;

public class JITVariableSet<T> implements AbstractVariableSet<T> {
	
	private final RpgNpc npc;
	private final OfflinePlayer player;
	private final StateType<T> stateType;
	private final RolePropertyMap<State<?>> states;
	
	public JITVariableSet(RpgNpc npc, OfflinePlayer player, StateType<T> stateType) {
		this.npc = npc;
		this.player = player;
		this.stateType = stateType;
		
		states = npc.getRole().getAllVisibleStates();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(String variableName) {
		State<?> state = states.get(variableName);
		
		if (state == null) {
			return null;
		}

		if (state.getType().getTypeClass() != stateType.getTypeClass()) {
			return null;
		}
		
		T value = (T) state.getValue(npc, player);
		
		return value;
	}

}
