package rpg_npcs.script;

import java.util.HashMap;

public class ScriptMap extends HashMap<String, Script> {
	private static final long serialVersionUID = 7821134684380740371L;

	public void addScript(Script script) {
		this.put(script.nameString, script);
	}
	
	public boolean doesScriptExist(String name) {
		return this.containsKey(name);
	}
	
	public Script getScript(String name) {
		return this.get(name);
	}

	public void putAll(ScriptMap newScripts, String prefixString) {
		for (String newScriptName : newScripts.keySet()) {
			Script newScript = newScripts.get(newScriptName);
			this.put(prefixString + newScriptName, newScript);
		}
	}
}
