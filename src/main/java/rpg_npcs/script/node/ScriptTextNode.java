package rpg_npcs.script.node;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.clip.placeholderapi.PlaceholderAPI;
import rpg_npcs.Conversation;
import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.SpeechBubble;
import rpg_npcs.role.RolePropertyMap;
import rpg_npcs.state.State;

public class ScriptTextNode extends ScriptLinearNode {
	protected static final int TICKS_PER_CYCLE = 1;

	private final String templateTextString;
	private final double textSpeed;
	
	private final String SHORTPAUSE_CHARACTERS = ",:;";
	private final int SHORTPAUSE = 4;
	private final String LONGPAUSE_CHARACTERS = ".?!";
	private final int LONGPAUSE = 7;

	private final int charactersPerWrap;
	private final String defaultLineStartString;
	
	private Map<Conversation, BukkitTask> addTextTasks = new HashMap<Conversation, BukkitTask>();
	
	public ScriptTextNode(String text, double speed, int charactersPerWrap, String defaultLineStartString) {
		super();
		
		templateTextString = text;
		textSpeed = speed;
		this.charactersPerWrap = charactersPerWrap;
		this.defaultLineStartString = defaultLineStartString;
	}

	@Override
	protected void startThis(Conversation conversation) {
		ScriptTextNode thisConversationNode = this;
		SpeechBubble bubble = conversation.getSpeechBubble();
		
		String textString = templateTextString;

		// Check if PlaceholdersAPI is present and format text
		if (RPGNPCsPlugin.getPlaceholderAPI() != null) {
			textString = PlaceholderAPI.setPlaceholders(conversation.getPlayer(), textString);
		}
		
		// Add variable values
		RolePropertyMap<State<?>> statesMap = conversation.getNpc().getRole().getAllVisibleStates();
		Matcher textStringMatcher = Pattern.compile("%([^%]*)%").matcher(textString);
		while(textStringMatcher.find()) {
			String variableNameString = textStringMatcher.group(1);
			
			State<?> state = statesMap.get(variableNameString);
			if (state!= null) {
				String valueString = state.getValue(conversation.getNpc(), conversation.getPlayer()).toString();
				textString = textString.replace("%" + variableNameString + "%", valueString);
			}
		}
		
		final String finalTextString = textString;
		
		// Create a new task to add text
		BukkitTask newTask = new BukkitRunnable() {
			private double charsToAdd = 0;
			protected int nextCharIndex = 0;
			
			private String currentColourString = null;
			
			public void run() {
				// Add char multiplier
				charsToAdd += textSpeed * TICKS_PER_CYCLE;
				
				// Add any chars that need added
				while (charsToAdd > 1) {
					// Check if finished
					if (nextCharIndex >= finalTextString.length()) {
						// Cancel this task
						thisConversationNode.stopNode(conversation);
						
						// Exe next operation
						thisConversationNode.finished(conversation);
						
						return;
					}
					
					boolean hasAddedChar = false; // Eats characters until a normal character is hit
					
					while (!hasAddedChar && nextCharIndex < finalTextString.length()) {
						char nextChar = finalTextString.charAt(nextCharIndex);
						
						switch (nextChar) {
						// On a newline a new line should be added to the hologram
						case '\n':
							// Add a new line
							bubble.addNewLine();
							if (currentColourString == null) {
								bubble.setLastLineText(defaultLineStartString);
							} else {
								bubble.setLastLineText(defaultLineStartString + currentColourString);
							}
							
							nextCharIndex += 1;
							break;
						// On a formatting char add the formatting code but set to add another char too
						case '§':
							if (nextCharIndex + 1 < finalTextString.length()) {
								String colourCodeString = "§" + finalTextString.charAt(nextCharIndex + 1);
								bubble.setLastLineText(bubble.getLastLineString() + colourCodeString);
								currentColourString = colourCodeString;
							}
							nextCharIndex += 2;
							break;
						// On a space, check to wrap line
						case ' ':
							int lastLineLength = bubble.getLastLineLength();
							
							if (lastLineLength >= charactersPerWrap) {
								// Add a new line
								bubble.addNewLine();
								bubble.setLastLineText(defaultLineStartString);
							} else {
								bubble.setLastLineText(bubble.getLastLineString() + ' ');
							}
							
							nextCharIndex += 1;
							
							break;
						// Eat a default character and set has added char
						default:
							bubble.setLastLineText(bubble.getLastLineString() + nextChar);
							
							hasAddedChar = true;
							nextCharIndex += 1;
							
							// Check for delay
							if (SHORTPAUSE_CHARACTERS.contains("" + nextChar)) {
								charsToAdd -= SHORTPAUSE * TICKS_PER_CYCLE;
							}
							if (LONGPAUSE_CHARACTERS.contains("" + nextChar)) {
								charsToAdd -= LONGPAUSE * TICKS_PER_CYCLE;
							}
							
							break;
						}
					}
					
					charsToAdd -= 1;
				}
			}
		}.runTaskTimer(conversation.instancingPlugin, 1, TICKS_PER_CYCLE);
		
		addTextTasks.put(conversation, newTask);
	}

	@Override
	public void stopNode(Conversation conversation) {
		if (addTextTasks.containsKey(conversation)) {
			addTextTasks.get(conversation).cancel();
			addTextTasks.remove(conversation);
		}
	}
}
