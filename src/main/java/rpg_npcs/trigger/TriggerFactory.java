package rpg_npcs.trigger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import rpg_npcs.ParseLog;
import rpg_npcs.prerequisite.PrerequisiteSet;

public class TriggerFactory {
	private final Map<Pattern, Class<? extends Trigger>> triggersMap = new HashMap<Pattern, Class<? extends Trigger>>();
	
	public static class TriggerFactoryReturnData {
		public ParseLog log = new ParseLog();
		public Trigger trigger = null;
	}
	
	public void addTriggerClass(Pattern pattern, Class<? extends Trigger> triggerClass) {
		triggersMap.put(pattern, triggerClass);
	}
	
	public TriggerFactoryReturnData createTrigger(String type, String name, PrerequisiteSet prerequisites, int priority) {
		TriggerFactoryReturnData returnData = new TriggerFactoryReturnData();
		
		for (Pattern regex : triggersMap.keySet()) {
			if (regex.matcher(type.toLowerCase()).matches()) {
				// Get the class to instantiate
				Class<? extends Trigger> triggerClass = triggersMap.get(regex);
				
				// Generate a new trigger
				try {
					Constructor<? extends Trigger> constructor = triggerClass.getDeclaredConstructor(String.class, PrerequisiteSet.class, Integer.class);
					returnData.trigger = constructor.newInstance(name, prerequisites, priority);
				} catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
					e.printStackTrace();
					returnData.log.addError("Malformed constructor for trigger class for: " + type);
				}
				
				return returnData;
			}
		}
		
		returnData.log.addError("Unrecognised trigger type: '" + type + "', name '" + name + "'");
		
		return returnData;
	}
}
