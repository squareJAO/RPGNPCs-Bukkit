package rpg_npcs.script.factoryPart;

import rpg_npcs.script.ScriptFactoryPartData;
import rpg_npcs.script.ScriptFactoryState;

public abstract class ScriptFactoryPart {
	private String _consumed = "";
	public final char StartChar;
	public final char EndChar;
	
	/**
	 * @param StartChar The character to trigger this factory consuming data
	 * @param EndChar The character marking the end of the factory consuming data
	 */
	public ScriptFactoryPart(char StartChar, char EndChar) {
		this.StartChar = StartChar;
		this.EndChar = EndChar;
	}
	
	/**
	 * @param state The state of the script factory when the node contained within this factory should be produced & extracted
	 * @return The script node that is produced by this factory part
	 */
	public final ScriptFactoryPartData extractNode(ScriptFactoryState state) {
		ScriptFactoryPartData node = generateNode(state, _consumed);
		
		// Clear consumed characters
		_consumed = "";
		
		return node;
	}
	
	/**
	 * Should be overridden by all script factory parts
	 * @param state The state of the script factory when the node is requested
	 * @param instruction The text contained for this factory to handle
	 * @return A node generated from the instruction string
	 */
	protected abstract ScriptFactoryPartData generateNode(ScriptFactoryState state, String instruction);
	
	/**
	 * Consumes a single character into the factory's consumed variable
	 * @param character The character to be consumed into the internal string
	 */
	public final void consumeCharacter(char character) {
		// Otherwise eat the character and return
		_consumed += character;
	}
	
	/**
	 * @return A boolean indicating whether the factory should trim spaces before this factory
	 */
	public boolean shouldTrimSpacesBefore() {
		return false;
	}
	
	/**
	 * @return A boolean indicating whether the factory should trim spaces after this factory
	 */
	public boolean shouldTrimSpacesAfter() {
		return false;
	}
}
