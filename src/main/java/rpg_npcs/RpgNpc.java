package rpg_npcs;

import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import rpg_npcs.role.Role;
import rpg_npcs.script.Script;

public interface RpgNpc {
	public void setRole(Role newRole);
	
	public Role getRole();
	
	public int getStopRange();
	
	public void stopConversation();
	
	public int getConversationPriority();
	
	public boolean isTalking();
	
	public void startConversation(Script script, Player player, int priority);
	
	public Map<String, String> getStateMap();
	
	public void setStateValue(String key, String value);
	
	public boolean isSpawned();
	
	public Entity getEntity();

	public String getName();
	
	public void lookClose(boolean enabled);
}
