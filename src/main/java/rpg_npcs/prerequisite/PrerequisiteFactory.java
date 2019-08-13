package rpg_npcs.prerequisite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import rpg_npcs.logging.Log;
import rpg_npcs.logging.LogEntry.MessageType;
import rpg_npcs.logging.Logged;

public class PrerequisiteFactory {
	private final Map<Pattern, Class<? extends Prerequisite>> supportedPrerequisiteRecords = new HashMap<Pattern, Class<? extends Prerequisite>>();
	
	public PrerequisiteFactory() {
	}

	public void addSupportedType(String prerequisiteKeyword, Class<? extends Prerequisite> prerequisite) {
		supportedPrerequisiteRecords.put(Pattern.compile(prerequisiteKeyword), prerequisite);
	}
	
	public Logged<Prerequisite> createPrerequisite(String key, String value) {
		for (Pattern prerequisitePattern : supportedPrerequisiteRecords.keySet()) {
			if (prerequisitePattern.matcher(key.toLowerCase()).matches()) {
				try {
					Method method = supportedPrerequisiteRecords.get(prerequisitePattern).getMethod("makePrerequisite", String.class);
					@SuppressWarnings("unchecked")
					Logged<Prerequisite> loggedPrerequisite = (Logged<Prerequisite>) method.invoke(null, value);
					return loggedPrerequisite;
				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
					return new Logged<Prerequisite>(null, Log.fromString(e.getLocalizedMessage(), MessageType.ERROR));
				}
			}
		}
		
		return new Logged<Prerequisite>(null, Log.fromString("No matching prerequisite found for " + key, MessageType.ERROR));
	}
	
	public Logged<PrerequisiteSet> createPrerequisiteSet(String prerequisitesString) {
		String[] prerequisites = prerequisitesString.split(";");
		
		Map<String, String> prerequisiteDataMap = new HashMap<String, String>();
		
		for (String string : prerequisites) {
			String[] prerequisitePartStrings = string.split(":");
			
			String prerequisiteKeyString = prerequisitePartStrings[0].trim();
			String prerequisiteValueString = string.substring(prerequisiteKeyString.length() + 1).trim();
			
			prerequisiteDataMap.put(prerequisiteKeyString, prerequisiteValueString);
		}
		
		return createPrerequisiteSet(prerequisiteDataMap);
	}

	public Logged<PrerequisiteSet> createPrerequisiteSet(Map<String, String> prerequisiteDataMap) {
		Log log = new Log();
		
		PrerequisiteSet prerequisites = new PrerequisiteSet();
		
		// Create prerequisites
		for (Entry<String, String> entry : prerequisiteDataMap.entrySet()) {
			// Convert to prerequisite
			Logged<Prerequisite> returnPrerequisiteData = createPrerequisite(entry.getKey(), entry.getValue());
			
			if (returnPrerequisiteData.getResult() != null) {
				prerequisites.add(returnPrerequisiteData.getResult());
			}
			
			log.addNamedEntry(entry.getKey(), returnPrerequisiteData.getLog());
		}
		
		return new Logged<PrerequisiteSet>(prerequisites, log);
	}
}
