package rpg_npcs.script;

import rpg_npcs.role.RolePropertyMap;

public class ScriptFactoryState {
	public double TextSpeed;
	private final double defaultTextSpeed;
	private boolean containsText;
	
	private RolePropertyMap<Script> allScripts;
	private RolePropertyMap<Script> newScripts;
	
	public ScriptFactoryState(double textSpeed, RolePropertyMap<Script> parentScripts) {
		defaultTextSpeed = textSpeed;
		TextSpeed = textSpeed;
		
		allScripts = parentScripts;
		newScripts = new RolePropertyMap<Script>();
	}
	
	public RolePropertyMap<Script> getNewScripts() {
		return newScripts;
	}
	
	public void addScript(Script script) {
		allScripts.put(script);
		newScripts.put(script);
	}
	
	public boolean doesScriptExist(String script) {
		return allScripts.containsKey(script);
	}
	
	public Script getScript(String script) {
		return allScripts.get(script);
	}

	public RolePropertyMap<Script> getAllScripts() {
		return allScripts;
	}

	public boolean getContainsText() {
		return containsText;
	}

	public void resetLine() {
		TextSpeed = defaultTextSpeed;
		containsText = false;
	}

	public void containsText() {
		containsText = true;
	}
	
}
