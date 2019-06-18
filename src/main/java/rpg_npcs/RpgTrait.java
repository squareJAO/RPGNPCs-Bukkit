package rpg_npcs;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.LookClose;
import rpg_npcs.role.Role;
import rpg_npcs.script.Script;

public class RpgTrait extends Trait implements RpgNpc {
	protected static Random rng = new Random();
	
	protected RPGNPCsPlugin instancingPlugin;
	
	protected Role role;
	
	@Persist("stopRange")
	protected int storedStopRange = -1;
	
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
	
	public void setRole(Role newRole) {
		if (role != null) {
			role.unregisterNpc(this);
		}
		
		role = newRole;
		
		if (newRole != null) {
			newRole.registerNpc(this);
		}
	}
	
	public Role getRole() {
		return role;
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
	
	public void stopConversation() {
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
		currentConversation = new Conversation(instancingPlugin, speechBubble, player, this, priority);
		currentConversation.startConversation(script);
	}

	@Override
	public boolean isSpawned() {
		return this.getNPC().isSpawned();
	}

	@Override
	public Entity getEntity() {
		return this.getNPC().getEntity();
	}

	@Override
	public void lookClose(boolean enabled) {
		npc.getTrait(LookClose.class).lookClose(enabled);
	}

	@Override
	public String getUUIDString() {
		return "Citizens2NPC_" + this.npc.getId();
	}
	
	@Override
	public String getNPCName() {
		return this.npc.getFullName();
	}
}
