package rpg_npcs.script.factoryPart;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import rpg_npcs.script.ScriptFactoryPartData;
import rpg_npcs.script.ScriptFactoryState;
import rpg_npcs.script.node.command.ScriptCommandNode;

public class ScriptFactoryCommandPart extends ScriptFactoryPart {
	private final Map<Pattern, Class<? extends ScriptCommandNode>> commandNodesMap = new HashMap<Pattern, Class<? extends ScriptCommandNode>>();
	
	public ScriptFactoryCommandPart() {
		super('[', ']');
	}
	
	public void addCommandNodeGenerator(Pattern regex, Class<? extends ScriptCommandNode> newCommandNodeClass) {
		commandNodesMap.put(regex, newCommandNodeClass);
	}

	@Override
	protected ScriptFactoryPartData generateNode(ScriptFactoryState state, String instruction) {
		String[] wordStrings = instruction.split(" ");
		
		if (wordStrings.length == 0) {
			return ScriptFactoryPartData.fromError("No command keyword given");
		}
		
		String keyWordString = wordStrings[0].toLowerCase();
		String argumentString = instruction.substring(wordStrings[0].length()).trim();
		
		for (Pattern regex : commandNodesMap.keySet()) {
			if (regex.matcher(keyWordString).matches()) {
				// Get the class to instantiate
				Class<? extends ScriptCommandNode> commandClass = commandNodesMap.get(regex);
				
				// Generate a new node with the arguments string
				try {
					Constructor<? extends ScriptCommandNode> constructor = commandClass.getDeclaredConstructor(String.class);
					ScriptCommandNode node = constructor.newInstance(argumentString);
					return ScriptFactoryPartData.fromNode(node);
				} catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
					return ScriptFactoryPartData.fromError("Malformed constructor for command class for: " + keyWordString);
				}
			}
		}
		
		return ScriptFactoryPartData.fromError("Invalid command: " + keyWordString);
	}

}
