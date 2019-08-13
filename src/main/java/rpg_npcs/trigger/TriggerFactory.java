package rpg_npcs.trigger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import rpg_npcs.logging.Log;
import rpg_npcs.logging.Logged;
import rpg_npcs.prerequisite.PrerequisiteSet;

public class TriggerFactory {
	private final Map<Pattern, Class<? extends Trigger>> triggersMap = new HashMap<Pattern, Class<? extends Trigger>>();
	
	public void addTriggerClass(Pattern pattern, Class<? extends Trigger> triggerClass) {
		triggersMap.put(pattern, triggerClass);
	}
	
	public Logged<Trigger> createTrigger(String type, String name, PrerequisiteSet prerequisites, int priority) {
		Log log = new Log();
		Trigger trigger = null;
		
		for (Pattern regex : triggersMap.keySet()) {
			if (regex.matcher(type.toLowerCase()).matches()) {
				// Get the class to instantiate
				Class<? extends Trigger> triggerClass = triggersMap.get(regex);
				
				// Generate a new trigger
				try {
					Constructor<? extends Trigger> constructor = triggerClass.getDeclaredConstructor(String.class, PrerequisiteSet.class, Integer.class);
					trigger = constructor.newInstance(name, prerequisites, priority);
				} catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
					log.addError("Malformed constructor for trigger class for: " + type);
				}
				
				return new Logged<Trigger>(trigger, log);
			}
		}
		
		log.addError("Unrecognised trigger type: '" + type + "', name '" + name + "'");

		return new Logged<Trigger>(null, log);
	}
}
