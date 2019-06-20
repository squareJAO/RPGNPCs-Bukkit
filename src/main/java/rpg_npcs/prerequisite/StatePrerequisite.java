package rpg_npcs.prerequisite;

import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.RpgNpc;
import rpg_npcs.state.State.ComparisonResult;
import rpg_npcs.state.StateType;

public class StatePrerequisite implements Prerequisite {
	public static enum Comparison {
		LESS_THAN,
		GREATER_THAN,
		EQUAL_TO,
		LESS_THAN_OR_EQUAL_TO,
		GREATER_THAN_OR_EQUAL_TO,
		NOT_EQUAL_TO
	}
	
	private final Comparison comparison;
	private final StateType<?> supportedStateType;
	private final String lhsExpression;
	private final String rhsExpression;
	
	public StatePrerequisite(Comparison comparison, StateType<?> supportedStateType, String lhsExpression, String rhsExpression) {
		this.comparison = comparison;
		this.supportedStateType = supportedStateType;
		this.lhsExpression = lhsExpression;
		this.rhsExpression = rhsExpression;
	}
	
	@Override
	public boolean isMet(Player player, RpgNpc npc) {
		// Resolve placeholders
		String localLhsExpression = lhsExpression;
		String localRhsExpression = rhsExpression;
		if (RPGNPCsPlugin.hasPlaceholderAPI()) {
			localLhsExpression = PlaceholderAPI.setPlaceholders(player, localLhsExpression);
			localRhsExpression = PlaceholderAPI.setPlaceholders(player, localRhsExpression);
		}
		
		// Execute expressions
		ComparisonResult result;
		try {
			result = executeAndCompare(player, npc, supportedStateType, localLhsExpression, localRhsExpression);
		} catch (IllegalArgumentException e) {
			Bukkit.getLogger().warning(e.getMessage());
			Bukkit.getLogger().warning("for npc " + npc.getNPCName() + ", player " + player.getDisplayName());
			return false;
		}
		
		// Match up result and comparison
		switch (comparison) {
		case EQUAL_TO:
			return result == ComparisonResult.EQUAL_TO;
		case GREATER_THAN:
			return result == ComparisonResult.GREATER_THAN;
		case GREATER_THAN_OR_EQUAL_TO:
			return result == ComparisonResult.GREATER_THAN || result == ComparisonResult.EQUAL_TO;
		case LESS_THAN:
			return result == ComparisonResult.LESS_THAN;
		case LESS_THAN_OR_EQUAL_TO:
			return result == ComparisonResult.LESS_THAN || result == ComparisonResult.EQUAL_TO;
		case NOT_EQUAL_TO:
			return result != ComparisonResult.EQUAL_TO;
		default:
			throw new NotImplementedException();
		}
	}
	
	private static <T> ComparisonResult executeAndCompare(Player player, RpgNpc npc, StateType<T> supportedStateType, String localLhsExpression, String localRhsExpression) {
		T lhs = supportedStateType.executeTypedExpression(npc, player, localLhsExpression);
		T rhs = supportedStateType.executeTypedExpression(npc, player, localRhsExpression);
		return supportedStateType.compare(lhs, rhs);
	}
}
