package rpg_npcs.script;

import java.util.Collection;
import java.util.Map;

import rpg_npcs.role.RolePropertyMap;
import rpg_npcs.script.ScriptFactoryPartData.HeldData;
import rpg_npcs.script.factoryPart.ScriptFactoryPart;
import rpg_npcs.script.node.ScriptLinearNode;
import rpg_npcs.script.node.ScriptTextNode;
import rpg_npcs.script.node.status.ScriptClearNode;

public class ScriptFactory {
	private final double defaultSpeed;
	private final int charactersPerWrap;
	private final String defaultLineStartString;
	private final Collection<ScriptFactoryPart> factoryParts;
	
	public ScriptFactory(Collection<ScriptFactoryPart> factoryParts, double defaultSpeed, int charactersPerWrap, String defaultLineStartString) {
		this.defaultSpeed = defaultSpeed;
		this.charactersPerWrap = charactersPerWrap;
		this.defaultLineStartString = defaultLineStartString;
		this.factoryParts = factoryParts;
	}
	
	public ScriptFactoryState createConversationTree(Map<String, String> instructions, RolePropertyMap<Script> parentScriptsMap) {
		/* These Instructions are outdated due to modular system but still give a good outline of the intended functionality of the system
		 * 
		 * Each item in the instructions is an id and a path. The first string is the id of that path
		 * The line of text is to be interpreted as spoken text with the following exceptions:
		 * A number surrounded by two ~ indicated a pause of ~x~ ticks
		 * A number inside of curly braces {x} indicates that the current text speed should be updated to x characters a tick
		 * Angled braces indicate a question and are used with the following format: <text1:x|text2:y|...|textn:z> where each x,y,z are ids of other paths
		 * Any characters inside of square braces are control characters and should not be displayed:
		 * 	1. [-] means clear the display
		 * 	2. [execute <string>] means execute the string as a command
		 * Any deviations of this format should print an error but should parse the error containing text as spoken text.
		 * Pauses should be automatically added in to punctuation
		 * The default line start string should be prepended to all new lines
		 * The lines of text should be broken up if they exceed the number of characters per wrap
		 */
		
		// Initialise state
		ScriptFactoryState state = new ScriptFactoryState(defaultSpeed, parentScriptsMap);
		
		// Populate state with skeleton data
		for (String lineID : instructions.keySet()) {
			state.addScript(new Script(lineID));
		}
		
		// Populate script stubs
		for (String lineID : instructions.keySet()) {
			String spokenTextString = instructions.get(lineID);
			Script baseNode = state.getScript(lineID);
			PopulateBranch(baseNode, spokenTextString, state);
			
			state.ResetBranchData();
		}
		
		return state;
	}
	
	private ScriptLinearNode AddSpeechNode(ScriptLinearNode workingNode, ScriptFactoryState state, String spokenTextString) {
		// Check if no text was given, if so don't create a node
		if (spokenTextString.length() == 0) {
			return workingNode;
		}
		
		ScriptLinearNode nextNode = new ScriptTextNode(spokenTextString, state.TextSpeed, charactersPerWrap, defaultLineStartString);
		workingNode.setNextNode(nextNode);
		return nextNode;
	}
	
	private void PopulateBranch(ScriptLinearNode baseNode, String instructionString, ScriptFactoryState state) {
		if (instructionString.length() == 0) {
			return;
		}
		
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
		
		for (int characterIndex = startingIndex; characterIndex < instructionString.length(); characterIndex++) {
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
						state.log.addError(data.errorText);
					} else if(data.heldData == HeldData.node) {
						workingNode.setNextNode(data.node);
						
						// Check if done
						if (!state.BranchDone && !(data.node instanceof ScriptLinearNode)) {
							state.log.addError("Returned conversation node was non-linear but the branch has not been marked as done. Expect bugs.");
						} else if (data.node instanceof ScriptLinearNode) {
							workingNode = (ScriptLinearNode)data.node;
						}
					}

					// Check if spaces need to be trimmed after
					if(currentPart.shouldTrimSpacesAfter()) {
						while (characterIndex + 1 < currentEatenString.length() &&
								Character.isWhitespace(currentEatenString.charAt(characterIndex + 1))) {
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
			
			// Check if any parts have marked the branch as done
			if (state.BranchDone) {
				// Check if it is the end of the instructions
				if (characterIndex != instructionString.length() - 1) {
					state.log.addError("Branch marked as done with a branching node, but there were more instructions: '" + instructionString.substring(characterIndex) + "'");
				}
				
				return;
			}
			
			// Stop escaping if were escaping
			escaping = false;
		}
		
		// Add the remaining string as a text node
		AddSpeechNode(workingNode, state, currentEatenString);
	}
}
