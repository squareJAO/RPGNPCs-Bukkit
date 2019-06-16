package rpg_npcs.state;

import com.fathzer.soft.javaluator.DoubleEvaluator;

import rpg_npcs.state.State.ComparisonResult;

public class NumberStateType extends SupportedStateType<Double> {
	public NumberStateType() {
		super("number", new DoubleEvaluator(), Double.class);
	}

	@Override
	public String valueToString(Double value) {
		return value.toString();
	}

	@Override
	public Double valueFromString(String string) {
		try {
			return Double.valueOf(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public ComparisonResult compare(Double value1, Double value2) {
		int comparisonResult = value1.compareTo(value2);
		
		if (comparisonResult < 0) {
			return ComparisonResult.LESS_THAN;
		} else if (comparisonResult > 0) {
			return ComparisonResult.GREATER_THAN;
		} else {
			return ComparisonResult.EQUAL_TO;
		}
	}

}
