package rpg_npcs.script.node;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.npc.NPC;
import rpg_npcs.SpeechBubble;

public abstract class ScriptActionNode extends ScriptLinearNode {

	public ScriptActionNode() {
		super();
	}
	
	protected NPC getNPC(SpeechBubble bubble) {
		return bubble.getNpc();
	}
	
	protected Player getPlayer(SpeechBubble bubble) {
		Entity entity = getNPC(bubble).getEntity();
		
		if (entity instanceof Player) {
			return (Player) entity;
		}
		
		return null;
	}
}
