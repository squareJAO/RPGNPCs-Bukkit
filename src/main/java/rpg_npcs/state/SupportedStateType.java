package rpg_npcs.state;

import java.util.logging.Level;

import org.bukkit.Bukkit;

import com.fathzer.soft.javaluator.AbstractEvaluator;
import com.fathzer.soft.javaluator.StaticVariableSet;
import com.sun.istack.internal.NotNull;

import rpg_npcs.RpgNpc;
import rpg_npcs.role.RolePropertyMap;
import rpg_npcs.state.State.ComparisonResult;
import rpg_npcs.state.State.StorageType;

public abstract class SupportedStateType<T> {
	private final String dataTypeName;
	private final AbstractEvaluator<T> evaluator;
	private final Class<T> typeClass;
	
	public SupportedStateType(String dataTypeName, AbstractEvaluator<T> evaluator, Class<T> typeClass) {
		super();
		this.dataTypeName = dataTypeName;
		this.evaluator = evaluator;
		this.typeClass = typeClass;
	}

	public String getDataTypeName() {
		return dataTypeName;
	}

	public final AbstractEvaluator<T> getEvaluator() {
		return evaluator;
	}

	public final Class<T> getTypeClass() {
		return typeClass;
	}
	
	public final State<T> createState(@NotNull String name, @NotNull String uuid, StorageType storageType, String defaultValueString) {
		T defaultValue = valueFromString(defaultValueString);
		
		if (defaultValue == null) {
			Bukkit.getLogger().log(Level.WARNING, "'" + defaultValueString + "' cannot be parsed to a " + dataTypeName);
			return null;
		}
		
		return new State<T>(name, uuid, this, storageType, defaultValue);
	}
	
	public T executeTypedExpression(RpgNpc npc, String expression) {
		// Get variables for use in function
		StaticVariableSet<?> variableSet = makeVariableSet(npc);
		
		// Execute
		T result = getEvaluator().evaluate(expression, variableSet);
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public StaticVariableSet<T> makeVariableSet(RpgNpc npc) {
		RolePropertyMap<State<?>> states = npc.getRole().getAllVisibleStates();
		StaticVariableSet<T> variableSet = new StaticVariableSet<T>();
		
		for (String stateName : states.keySet()) {
			State<?> state = states.get(stateName);
			if (state.getType().getTypeClass() == this.getTypeClass()) {
				variableSet.set(stateName, (T) state.getValue(npc)); // Can go unchecked as long as everyone was honest when constructing supported states
			}
		}
		
		return variableSet;
	}
	
	public abstract String valueToString(T value);
	
	public abstract T valueFromString(String string);
	
	public abstract ComparisonResult compare(T value1, T value2);
}
