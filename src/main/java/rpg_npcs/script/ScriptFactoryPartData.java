package rpg_npcs.script;

import rpg_npcs.script.node.ScriptNode;

public class ScriptFactoryPartData {
	// Types of held data
	public enum HeldData {
		error,
		node,
		nothing
	}
	
	public final HeldData heldData;
	public final String errorText;
	public final ScriptNode node;
	
	private ScriptFactoryPartData(HeldData heldData, String errorText, ScriptNode node) {
		this.heldData = heldData;
		this.errorText = errorText;
		this.node = node;
	}
	
	public static ScriptFactoryPartData fromError(String errorText) {
		return new ScriptFactoryPartData(HeldData.error, errorText, null);
	}
	
	public static ScriptFactoryPartData fromNode(ScriptNode node) {
		return new ScriptFactoryPartData(HeldData.node, "", node);
	}
	
	public static ScriptFactoryPartData fromNothing() {
		return new ScriptFactoryPartData(HeldData.nothing, "", null);
	}
}
