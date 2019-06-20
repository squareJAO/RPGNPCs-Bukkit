package rpg_npcs.state;

import org.bukkit.OfflinePlayer;

import com.fathzer.soft.javaluator.AbstractEvaluator;
import com.fathzer.soft.javaluator.AbstractVariableSet;

import rpg_npcs.RpgNpc;
import rpg_npcs.state.State.ComparisonResult;

public abstract class StateType<T> {
	private final String dataTypeName;
	private final AbstractEvaluator<T> evaluator;
	private final Class<T> typeClass;
	
	public StateType(String dataTypeName, AbstractEvaluator<T> evaluator, Class<T> typeClass) {
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
	
	public T executeTypedExpression(RpgNpc npc, OfflinePlayer player, String expression) throws IllegalArgumentException {
		// Get variables for use in function
		AbstractVariableSet<T> variableSet = new JITVariableSet<T>(npc, player, this);
		
		// Execute
		T result = getEvaluator().evaluate(expression, variableSet);
		
		return result;
	}
	
	public abstract String valueToString(T value);
	
	public abstract T valueFromString(String string);
	
	public abstract ComparisonResult compare(T value1, T value2);
}
