package rpg_npcs.script.factoryPart;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rpg_npcs.ParserFactorySet;
import rpg_npcs.logging.Log;
import rpg_npcs.logging.Logged;
import rpg_npcs.prerequisite.PrerequisiteSet;
import rpg_npcs.script.ScriptFactoryPartData;
import rpg_npcs.script.ScriptFactoryState;
import rpg_npcs.script.node.command.ScriptCommandNode;

public class ScriptFactoryCommandPart extends ScriptFactoryPart {
	private final Map<Pattern, Class<? extends ScriptCommandNode>> commandNodesMap = new HashMap<Pattern, Class<? extends ScriptCommandNode>>();
	
	private final ParserFactorySet factorySet;
	
	public ScriptFactoryCommandPart(ParserFactorySet factorySet) {
		super('[', ']');
		
		this.factorySet = factorySet;
	}
	
	public void addCommandNodeGenerator(String regexString, Class<? extends ScriptCommandNode> newCommandNodeClass) {
		addCommandNodeGenerator(Pattern.compile(regexString), newCommandNodeClass);
	}
	
	public void addCommandNodeGenerator(Pattern regex, Class<? extends ScriptCommandNode> newCommandNodeClass) {
		commandNodesMap.put(regex, newCommandNodeClass);
	}

	@Override
	protected ScriptFactoryPartData generateNode(ScriptFactoryState state, String instruction) {
		// Split instruction
		Matcher instructionMatcher = Pattern.compile("\\G((?<prerequisites>([^:;]+):([^:;]+)(;([^:;]+):([^:;]+))*)\\s*\\?\\s*)?(?<command>[a-zA-Z]+)( (?<arguments>.+))?$").matcher(instruction);
		
		if (!instructionMatcher.matches()) {
			return ScriptFactoryPartData.fromError("Malformed command: " + instruction);
		}
		
		String prerequisitesString = instructionMatcher.group("prerequisites");

		// Extract any prerequisites
		PrerequisiteSet prerequisiteSet = new PrerequisiteSet();
		if (prerequisitesString != null) {
			Logged<PrerequisiteSet> loggedPrerequisiteSet = factorySet.getPrerequisiteFactory().createPrerequisiteSet(prerequisitesString);
			
			Log log = loggedPrerequisiteSet.getLog();
			if (log.countErrors() > 0) {
				return ScriptFactoryPartData.fromError(log.getErrors().getFormattedString());
			}
			
			prerequisiteSet = loggedPrerequisiteSet.getResult();
		}
		
		// Extract command & argument
		String commandString = instructionMatcher.group("command").trim();
		String argumentString = instructionMatcher.group("arguments");
		argumentString = argumentString == null ? "" : argumentString.trim();
		
		for (Pattern regex : commandNodesMap.keySet()) {
			if (regex.matcher(commandString).matches()) {
				// Get the class to instantiate
				Class<? extends ScriptCommandNode> commandClass = commandNodesMap.get(regex);
				
				// Generate a new node with the arguments string
				try {
					Constructor<? extends ScriptCommandNode> constructor = commandClass.getDeclaredConstructor(String.class, PrerequisiteSet.class);
					ScriptCommandNode node = constructor.newInstance(argumentString, prerequisiteSet);
					return ScriptFactoryPartData.fromNode(node);
				} catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
					return ScriptFactoryPartData.fromError("Malformed constructor for command class for: " + commandString);
				}
			}
		}
		
		return ScriptFactoryPartData.fromError("Invalid command: " + commandString);
	}

}
