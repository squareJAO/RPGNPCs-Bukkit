package rpg_npcs.script;

import rpg_npcs.ParseLog;
import rpg_npcs.role.RolePropertyMap;

public class ScriptFactoryState {
	public double TextSpeed;
	public boolean BranchDone;
	
	private RolePropertyMap<Script> allScripts;
	private RolePropertyMap<Script> newScripts;
	
	public ParseLog log;
	
	public ScriptFactoryState(double textSpeed, RolePropertyMap<Script> parentScripts) {
		TextSpeed = textSpeed;
		
		allScripts = parentScripts;
		newScripts = new RolePropertyMap<Script>();
		log = new ParseLog();
		
		ResetBranchData();
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
	
	public void ResetBranchData() {
		BranchDone = false;
	}

	public RolePropertyMap<Script> getAllScripts() {
		return allScripts;
	}
	
}
