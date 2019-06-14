package rpg_npcs.script.node.command;

import java.util.logging.Level;

import org.bukkit.Bukkit;

import rpg_npcs.Conversation;
import rpg_npcs.role.RolePropertyMap;
import rpg_npcs.state.State;
import rpg_npcs.state.SupportedStateType;

public class ScriptStoreNode extends ScriptCommandNode {
	public ScriptStoreNode(String argumentString) {
		super(argumentString);
	}

	@Override
	protected void startThisCommand(Conversation conversation, String argumentString) {
		if (argumentString.length() == 0) {
			Bukkit.getLogger().log(Level.WARNING, "No argument given for store command");
			return;
		}
		
		String[] argumentPartStrings = argumentString.split(" ");
		String destinationName = argumentPartStrings[0];
		
		if (argumentPartStrings.length == 1) {
			Bukkit.getLogger().log(Level.WARNING, "No function given for store in '" + destinationName + "' command");
			return;
		}
		
		String expression = argumentString.substring(destinationName.length() + 1);
		
		// Get visible states
		RolePropertyMap<State<?>> states = conversation.getNpc().getRole().getAllVisibleStates();
		
		// Get state to store result in
		State<?> resultState = states.get(destinationName);
		
		if (resultState == null) {
			Bukkit.getLogger().log(Level.WARNING, "State to store result in '" + resultState + "' not found");
			return;
		}
		
		executeAndStore(resultState, conversation, expression);
	}
	
	// Need a typed function because java doesn't like generics
	private <T> void executeAndStore(State<T> resultState, Conversation conversation, String expression) {
		SupportedStateType<T> type = resultState.getType();
		
		T result = type.executeTypedExpression(conversation.getNpc(), expression);
		
		resultState.setValue(conversation.getNpc(), result);
	}

	@Override
	protected String getNodeRepresentation() {
		return "<Store the result of a function in a variable>";
	}

}
