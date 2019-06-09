package rpg_npcs;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.persistence.DelegatePersistence;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import rpg_npcs.script.Script;

public class RpgTrait extends Trait {
	public static class DataMapPersister implements Persister<Map<String, String>> {
		// WARNING: Backslash hell, tread carefully
		
		@Override
		public Map<String, String> create(DataKey root) {
			Map<String, String> map = new HashMap<String, String>();
			
			String stateNameString = root.getString("StateNames");
			
			String[] nameStrings = stateNameString.split("[^\\\\];"); // Split on non-escaped semicolons
			
			for (String nameString : nameStrings) {
				nameString = nameString.replace("\\;", ";").replace("\\\\", "\\"); // Unescapes string, Replaces \; with ; and \\ with \
				map.put(nameString, root.getString(nameString));
			}
			
			return map;
		}
		
		@Override
		public void save(Map<String, String> map, DataKey root) {
			String[] nameStrings = map.keySet().toArray(new String[map.size()]);
			
			for (int i = 0; i < nameStrings.length; i++) {
				nameStrings[i] = nameStrings[i].replace("\\", "\\\\").replace(";", "\\;"); // Escapes string, Replaces ; with \; and \ with \\
			}
			
			String stateNameString = String.join(";", nameStrings);
			root.setString("StateNames", stateNameString);
			
			for (String key : map.keySet()) {
				root.setString(key, map.get(key));
			}
		}
	}
	
	protected static Random rng = new Random();
	
	protected RPGNPCsPlugin instancingPlugin;
	
	@Persist("stopRange")
	protected int storedStopRange = -1;
	
	@Persist("stateData")
	@DelegatePersistence(DataMapPersister.class)
	public Map<String, String> stateDataMap;
	
	// Used for speech bubbles
	protected SpeechBubble speechBubble;
	protected Conversation currentConversation;

	public RpgTrait() {
		super("Rpgnpc");
		
		instancingPlugin = (RPGNPCsPlugin) Bukkit.getPluginManager().getPlugin("RPGNPCs");
	}
	
	@Override
	public void onAttach() {
		super.onAttach();

		instancingPlugin.registerRPGNPC(this);
	}
	
	@Override
	public void onRemove() {
		super.onRemove();

		instancingPlugin.unregisterRPGNPC(this);
	}
	
	@Override
	public void onSpawn() {
		super.onSpawn();
		
		// Create speech bubble stuff
		speechBubble = new SpeechBubble(instancingPlugin, npc);
	}
	
	@Override
	public void onDespawn() {
		super.onDespawn();
		
		speechBubble.clearText();
	}
	
	public int getStopRange() {
		if (storedStopRange >= 0) {
			return storedStopRange;
		}
		
		return instancingPlugin.defaultConversationMaxRange;
	}
	
	private int tickIndex = 0;
	
	@Override
	public void run() {
		// Check if conversation should end
		int stopRange = getStopRange();
		if(tickIndex >= instancingPlugin.ticksPerRangeCheck && stopRange > 0 && isTalking()) {
			Location npcLocation = getNPC().getEntity().getLocation();
			Location playerLocation = currentConversation.getPlayer().getLocation();
			
			if (npcLocation.distanceSquared(playerLocation) >= stopRange * stopRange) {
				stopConversation();
			}
			
			tickIndex = 0;
		}
		
		tickIndex++;
	}
	
	protected void stopConversation() {
		if (currentConversation != null) {
			currentConversation.stopConversation();
		}
		
		// Clear the last conversation
		speechBubble.clearText();
	}
	
	public int getConversationPriority() {
		if (!isTalking()) {
			return Integer.MIN_VALUE;
		}
		
		return currentConversation.getPriority();
	}
	
	public boolean isTalking() {
		if (currentConversation == null) {
			return false;
		}
		
		return currentConversation.isRunning();
	}
	
	public void startConversation(Script script, Player player, int priority) {
		// Stop old conversation
		if (currentConversation != null && currentConversation.isRunning()) {
			currentConversation.stopConversation();
		}
		
		// Start new conversation
		currentConversation = new Conversation(instancingPlugin, speechBubble, player, this.getNPC(), priority);
		currentConversation.startConversation(script);
	}
}
