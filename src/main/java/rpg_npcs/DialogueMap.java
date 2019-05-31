package rpg_npcs;

import java.util.HashMap;
import java.util.Map;

import rpg_npcs.script.Script;
import rpg_npcs.script.ScriptMap;
import rpg_npcs.trigger.Trigger;

public class DialogueMap extends HashMap<String, WeightedSet<String>> {

	private static final long serialVersionUID = -5475727082399361636L;
	
	/**
	 * Combines this with a trigger map and a script map to map triggers on to script sets
	 * @param triggers the trigger map that this references
	 * @param scripts the scripts map that this references
	 * @return a mapping from triggers to a weighted script set
	 */
	public Map<Trigger, WeightedSet<Script>> zip(TriggerMap triggers, ScriptMap scripts) {
		Map<Trigger, WeightedSet<Script>> map = new HashMap<Trigger, WeightedSet<Script>>();
		
		for (String triggerName : this.keySet()) {
			Trigger trigger = triggers.get(triggerName);
			WeightedSet<Script> scriptSet = this.get(triggerName).zip(scripts);
			
			map.put(trigger, scriptSet);
		}
		
		return map;
	}
}
