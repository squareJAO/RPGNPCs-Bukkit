package rpg_npcs.script;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.ChatColor;

import rpg_npcs.logging.Log;
import rpg_npcs.logging.Logged;
import rpg_npcs.role.RolePropertyMap;
import rpg_npcs.script.ScriptFactoryPartData.HeldData;
import rpg_npcs.script.factoryPart.ScriptFactoryPart;
import rpg_npcs.script.node.ScriptLinearNode;
import rpg_npcs.script.node.ScriptPauseNode;
import rpg_npcs.script.node.ScriptTextNode;
import rpg_npcs.script.node.status.ScriptClearNode;

public class ScriptFactory {
	private final double defaultSpeed;
	private final int charactersPerWrap;
	private final String defaultLineStartString;
	private final Collection<ScriptFactoryPart> factoryParts;
	
	public ScriptFactory(double defaultSpeed, int charactersPerWrap, String defaultLineStartString) {
		this.defaultSpeed = defaultSpeed;
		this.charactersPerWrap = charactersPerWrap;
		this.defaultLineStartString = defaultLineStartString;
		this.factoryParts = new HashSet<ScriptFactoryPart>();
	}
	
	public void addPart(ScriptFactoryPart part) {
		factoryParts.add(part);
	}
	
	public Logged<RolePropertyMap<Script>> createConversationTree(Map<String, String> instructions, RolePropertyMap<Script> parentScriptsMap) {
		/* These Instructions are outdated due to modular system but still give a good outline of the intended functionality of the system
		 * 
		 * Each item in the instructions is an id and a path. The first string is the id of that path
		 * The line of text is to be interpreted as spoken text with the following exceptions:
		 * A number surrounded by two ~ indicated a pause of ~x~ ticks
		 * A number inside of curly braces {x} indicates that the current text speed should be updated to x characters a tick
		 * Angled braces indicate a question and are used with the following format: <text1:x|text2:y|...|textn:z> where each x,y,z are ids of other paths
		 * Any characters inside of square braces are control characters and should not be displayed
		 * || means clear the display
		 * [execute <string>] means execute the string as a command
		 * Any deviations of this format should print an error but should parse the error containing text as spoken text.
		 * Pauses should be automatically added in to punctuation
		 * The default line start string should be prepended to all new lines
		 * The lines of text should be broken up if they exceed the number of characters per wrap
		 */
		
		Log log = new Log();
		
		// Initialise state
		ScriptFactoryState state = new ScriptFactoryState(defaultSpeed, parentScriptsMap);
		
		// Populate state with skeleton data
		for (String lineID : instructions.keySet()) {
			state.addScript(new Script(lineID));
		}
		
		// Populate script stubs
		for (String lineID : instructions.keySet()) {
			String spokenTextString = ChatColor.translateAlternateColorCodes('&', instructions.get(lineID));
			spokenTextString = spokenTextString.trim();
			Script baseNode = state.getScript(lineID);
			Log branchLog = PopulateBranch(baseNode, spokenTextString, state);
			log.addNamedEntry(lineID, branchLog);
		}
		
		return new Logged<RolePropertyMap<Script>>(state.getNewScripts(), log);
	}
	
	private ScriptLinearNode AddSpeechNode(ScriptLinearNode workingNode, ScriptFactoryState state, String spokenTextString) {
		// Check if no text was given, if so don't create a node
		if (spokenTextString.length() == 0) {
			return workingNode;
		}
		
		state.containsText();
		
		ScriptLinearNode nextNode = new ScriptTextNode(spokenTextString, state.TextSpeed, charactersPerWrap, defaultLineStartString);
		workingNode.setNextNode(nextNode);
		return nextNode;
	}
	
	private Log PopulateBranch(ScriptLinearNode baseNode, String instructionString, ScriptFactoryState state) {
		Log log = new Log();
		
		state.resetLine();
		
		if (instructionString.length() == 0) {
			return log;
		}
		
		log.addInfo(instructionString);
		
		ScriptLinearNode workingNode = baseNode;
		
		// Scroll and eat characters to put a tree together
		String currentEatenString = defaultLineStartString;
		ScriptFactoryPart currentPart = null; // The current factory for the current text being eaten. If null then eating speech
		boolean escaping = false;
		
		// Check if script should be a continuation
		int startingIndex = 0;
		if (instructionString.charAt(0) != '+') {
			ScriptClearNode clearNode = new ScriptClearNode();
			workingNode.setNextNode(clearNode);
			workingNode = clearNode;
		} else {
			startingIndex = 1;
		}
		
		int characterIndex;
		for (characterIndex = startingIndex; characterIndex < instructionString.length(); characterIndex++) {
			char currentCharacter = instructionString.charAt(characterIndex);

			
			// Check if next character is being escaped
			if (currentCharacter == '\\') {
				escaping = true;
				continue;
			}
			
			// If eating speech
			if (currentPart == null) {
				// Check if any of the other factory parts want to take over
				for (ScriptFactoryPart alternatePart : factoryParts) {
					if (!escaping && alternatePart.StartChar == currentCharacter) {
						// If an alternate part wants to take over then swap it out
						currentPart = alternatePart;
						
						// Check if spaces need to be trimmed after
						if(alternatePart.shouldTrimSpacesBefore()) {
							int i = currentEatenString.length() - 1;
							while (i > 0 && Character.isWhitespace(currentEatenString.charAt(i))) {
								i--;
							}
							currentEatenString = currentEatenString.substring(0, i + 1);
						}
						
						// Create a text node for before the new factory
						workingNode = AddSpeechNode(workingNode, state, currentEatenString);

						// Reset eaten string
						currentEatenString = "";
						
						break;
					}
				}
				
				// Eat a character
				currentEatenString += currentCharacter;
			} else {
				// Check if the current part is done
				if(!escaping && currentCharacter == currentPart.EndChar) {
					ScriptFactoryPartData data = currentPart.extractNode(state);
					
					// Do what needs to be done with the returned data
					if(data.heldData == HeldData.error) {
						log.addError(data.errorText);
					} else if(data.heldData == HeldData.node) {
						// Add a pause if needed
						int pauseBefore = data.pauseBefore;
						if (pauseBefore > 0) {
							ScriptPauseNode pauseNode = new ScriptPauseNode(pauseBefore);
							workingNode.setNextNode(pauseNode);
							workingNode = pauseNode;
						}
						
						workingNode.setNextNode(data.node);
						
						// Check if done
						if (!(data.node instanceof ScriptLinearNode)) {
							// Check if more given that wasn't used
							if (characterIndex < instructionString.length() - 1) {
								log.addError("Branch marked as done with a branching node, but there were more instructions: '" + instructionString.substring(characterIndex) + "'");
							}
							return log;
						}
						
						workingNode = (ScriptLinearNode)data.node;
					}

					// Check if spaces need to be trimmed after
					if(currentPart.shouldTrimSpacesAfter()) {
						while (characterIndex + 1 < instructionString.length() &&
								Character.isWhitespace(instructionString.charAt(characterIndex + 1))) {
							characterIndex++;
						}
					}
					
					// Go back to eating text
					currentPart = null;
					currentEatenString = "";
				} else {
					// Have the current factory part eat the current character
					currentPart.consumeCharacter(currentCharacter);
				}
			}
			
			// Stop escaping if were escaping
			escaping = false;
		}
		
		// Add the remaining string as a text node
		workingNode = AddSpeechNode(workingNode, state, currentEatenString);
		
		// Add a pause
		if (!(workingNode instanceof ScriptPauseNode) && state.getContainsText()) {
			ScriptPauseNode node = new ScriptPauseNode(100);
			workingNode.setNextNode(node);
			workingNode = node;
		}
		
		return log;
	}
}
