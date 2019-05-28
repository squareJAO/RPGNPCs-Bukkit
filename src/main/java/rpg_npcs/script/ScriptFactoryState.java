package rpg_npcs.script;

import rpg_npcs.ParseLog;

public class ScriptFactoryState {
	public double TextSpeed;
	public boolean BranchDone;
	
	private ScriptMap allScripts;
	private ScriptMap newScripts;
	
	public ParseLog log;
	
	public ScriptFactoryState(double textSpeed, ScriptMap parentScripts) {
		TextSpeed = textSpeed;
		
		allScripts = parentScripts;
		newScripts = new ScriptMap();
		log = new ParseLog();
		
		ResetBranchData();
	}
	
	public ScriptMap getNewScripts() {
		return newScripts;
	}
	
	public void addScript(Script script) {
		allScripts.addScript(script);
		newScripts.addScript(script);
	}
	
	public boolean doesScriptExist(String script) {
		return allScripts.doesScriptExist(script);
	}
	
	public Script getScript(String script) {
		return allScripts.getScript(script);
	}
	
	public void ResetBranchData() {
		BranchDone = false;
	}

	public ScriptMap getAllScripts() {
		return allScripts;
	}
	
}
