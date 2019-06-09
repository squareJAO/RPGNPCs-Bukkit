package rpg_npcs.state;

import java.math.BigDecimal;

import rpg_npcs.RpgTrait;

public class NumberState extends State<BigDecimal> {
	public NumberState(String name, BigDecimal defaultValue, StorageType type, String uuid) {
		super(name, defaultValue, type, uuid);
	}

	@Override
	public ComparisonResult compareTo(RpgTrait npc, State<BigDecimal> state) {
		int comparisonResult = this.getValue(npc).compareTo(state.getValue(npc));
		
		if (comparisonResult < 0) {
			return ComparisonResult.LESS_THAN;
		} else if (comparisonResult > 0) {
			return ComparisonResult.GREATER_THAN;
		} else {
			return ComparisonResult.EQUAL_TO;
		}
	}

	@Override
	public BigDecimal fromString(String string) {
		try {
			return new BigDecimal(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public String toString(BigDecimal value) {
		return value.toString();
	}

}
