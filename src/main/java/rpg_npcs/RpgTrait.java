package rpg_npcs;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import rpg_npcs.script.Script;

public class RpgTrait extends Trait {
	protected static Random rng = new Random();
	
	protected RPGNPCsPlugin instancingPlugin;
	
	@Persist("stopRange") protected int stopRange = 5;
	
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
	
	
	private int tickIndex = 0;
	
	@Override
	public void run() {
		// Check if conversation should end
		if(tickIndex >= 20 && stopRange > 0 && isTalking()) {
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
		currentConversation = new Conversation(speechBubble, player, this.getNPC(), priority);
		currentConversation.startConversation(script);
	}
}
