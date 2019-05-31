package rpg_npcs;

import java.util.HashMap;

import rpg_npcs.trigger.Trigger;

public class TriggerMap extends HashMap<String, Trigger> {

	private static final long serialVersionUID = -4499446758264135228L;

	public TriggerMap copy() {
		TriggerMap copyMap = new TriggerMap();
		copyMap.putAll(this);
		return copyMap;
	}
}
