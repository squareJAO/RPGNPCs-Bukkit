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

	private final String _templateTextString;
	private final double _textSpeed;
	
	private final String SHORTPAUSE_CHARACTERS = ",:;";
	private final int SHORTPAUSE = 3;
	private final String LONGPAUSE_CHARACTERS = ".?!";
	private final int LONGPAUSE = 5;

	private final int _charactersPerWrap;
	private final String _defaultLineStartString;
	
	private Map<Conversation, BukkitTask> _addTextTasks = new HashMap<Conversation, BukkitTask>();
	
	public ScriptTextNode(RPGNPCsPlugin plugin, String text, double speed, int charactersPerWrap, String defaultLineStartString) {
		super(plugin);
		
		_templateTextString = text;
		_textSpeed = speed;
		_charactersPerWrap = charactersPerWrap;
		_defaultLineStartString = defaultLineStartString;
	}

	@Override
	protected void startThis(Conversation conversation) {
		ScriptTextNode thisConversationNode = this;
		SpeechBubble bubble = conversation.getSpeechBubble();
		
		String _textString;

		// Check if PlaceholdersAPI is present and format text
		if (instancingPlugin.hasPlaceholderAPI()) {
			_textString = PlaceholderAPI.setPlaceholders(conversation.getPlayer(), _templateTextString);
		} else {
			_textString = _templateTextString;
		}
		
		// Create a new task to add text
		BukkitTask newTask = new BukkitRunnable() {
			private double _charsToAdd = 0;
			protected int _nextCharIndex = 0;
			
			public void run() {
				// Add char multiplier
				_charsToAdd += _textSpeed * TICKS_PER_CYCLE;
				
				// Add any chars that need added
				while (_charsToAdd > 1) {
					// Check if finished
					if (_nextCharIndex >= _textString.length()) {
						// Cancel this task
						thisConversationNode.stopNode(conversation);
						
						// Exe next operation
						thisConversationNode.onFinished(conversation);
						
						return;
					}
					
					boolean hasAddedChar = false; // Eats characters until a normal character is hit
					
					while (!hasAddedChar && _nextCharIndex < _textString.length()) {
						char nextChar = _textString.charAt(_nextCharIndex);
						
						switch (nextChar) {
						// On a newline a new line should be added to the hologram
						case '\n':
							// Add a new line
							bubble.addNewLine();
							bubble.setLastLineText(_defaultLineStartString);
							
							_nextCharIndex += 1;
							break;
						// On a formatting char add the formatting code but set to add another char too
						case '§':
							if (_nextCharIndex + 1 < _textString.length()) {
								bubble.setLastLineText(bubble.getLastLineString() + '§' + _textString.charAt(_nextCharIndex + 1));
							}
							_nextCharIndex += 2;
							break;
						// On a space, check to wrap line
						case ' ':
							int lastLineLength = bubble.getLastLineLength();
							
							if (lastLineLength >= _charactersPerWrap) {
								// Add a new line
								bubble.addNewLine();
								bubble.setLastLineText(_defaultLineStartString);
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
		}.runTaskTimer(instancingPlugin, 1, TICKS_PER_CYCLE);
		
		_addTextTasks.put(conversation, newTask);
	}

	@Override
	public void stopNode(Conversation conversation) {
		if (_addTextTasks.containsKey(conversation)) {
			_addTextTasks.get(conversation).cancel();
			_addTextTasks.remove(conversation);
		}
	}

	@Override
	protected String getNodeRepresentation() {
		return "<say '" + _templateTextString.replace("\n", "\\n").replace("\t", "\\t") + "' with a speed of " + _textSpeed + ">";
	}
}
