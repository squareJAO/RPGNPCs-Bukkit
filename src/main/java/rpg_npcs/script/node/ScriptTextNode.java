package rpg_npcs.script.node;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.clip.placeholderapi.PlaceholderAPI;
import rpg_npcs.Conversation;
import rpg_npcs.RPGNPCsPlugin;
import rpg_npcs.SpeechBubble;

public class ScriptTextNode extends ScriptLinearNode {
	protected static final int TICKS_PER_CYCLE = 1;

	private final String templateTextString;
	private final double textSpeed;
	
	private final String SHORTPAUSE_CHARACTERS = ",:;";
	private final int SHORTPAUSE = 3;
	private final String LONGPAUSE_CHARACTERS = ".?!";
	private final int LONGPAUSE = 5;

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
		
		String textString;

		// Check if PlaceholdersAPI is present and format text
		if (RPGNPCsPlugin.hasPlaceholderAPI()) {
			textString = PlaceholderAPI.setPlaceholders(conversation.getPlayer(), templateTextString);
		} else {
			textString = templateTextString;
		}
		
		// Create a new task to add text
		BukkitTask newTask = new BukkitRunnable() {
			private double _charsToAdd = 0;
			protected int _nextCharIndex = 0;
			
			public void run() {
				// Add char multiplier
				_charsToAdd += textSpeed * TICKS_PER_CYCLE;
				
				// Add any chars that need added
				while (_charsToAdd > 1) {
					// Check if finished
					if (_nextCharIndex >= textString.length()) {
						// Cancel this task
						thisConversationNode.stopNode(conversation);
						
						// Exe next operation
						thisConversationNode.finished(conversation);
						
						return;
					}
					
					boolean hasAddedChar = false; // Eats characters until a normal character is hit
					
					while (!hasAddedChar && _nextCharIndex < textString.length()) {
						char nextChar = textString.charAt(_nextCharIndex);
						
						switch (nextChar) {
						// On a newline a new line should be added to the hologram
						case '\n':
							// Add a new line
							bubble.addNewLine();
							bubble.setLastLineText(defaultLineStartString);
							
							_nextCharIndex += 1;
							break;
						// On a formatting char add the formatting code but set to add another char too
						case '§':
							if (_nextCharIndex + 1 < textString.length()) {
								bubble.setLastLineText(bubble.getLastLineString() + '§' + textString.charAt(_nextCharIndex + 1));
							}
							_nextCharIndex += 2;
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
							
							_nextCharIndex += 1;
							
							break;
						// Eat a default character and set has added char
						default:
							bubble.setLastLineText(bubble.getLastLineString() + nextChar);
							
							hasAddedChar = true;
							_nextCharIndex += 1;
							
							// Check for delay
							if (SHORTPAUSE_CHARACTERS.contains("" + nextChar)) {
								_charsToAdd -= SHORTPAUSE * TICKS_PER_CYCLE;
							}
							if (LONGPAUSE_CHARACTERS.contains("" + nextChar)) {
								_charsToAdd -= LONGPAUSE * TICKS_PER_CYCLE;
							}
							
							break;
						}
					}
					
					_charsToAdd -= 1;
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

	@Override
	protected String getNodeRepresentation() {
		return "<say '" + templateTextString.replace("\n", "\\n").replace("\t", "\\t") + "' with a speed of " + textSpeed + ">";
	}
}
