package rpg_npcs;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;

import net.citizensnpcs.api.npc.NPC;

public class SpeechBubble {
	protected JavaPlugin _instancingPlugin;
	
	protected NPC _linkedNPC;
	protected Hologram _hologram;
	protected TextLine _lastLine;

	protected double _xOffset = 0;
	protected double _yOffset = 0.8;
	protected double _zOffset = 0;
	
	public SpeechBubble(JavaPlugin instancingPlugin, NPC linkedNPC) {
		// Pass values
		_instancingPlugin = instancingPlugin;
		_linkedNPC = linkedNPC;
		
		Entity linkedEntity = _linkedNPC.getEntity();
		
		// Create hologram
		_hologram = HologramsAPI.createHologram(instancingPlugin, linkedEntity.getLocation());
		
		// Create move loop
		final SpeechBubble thisSpeechBubble = this;
		new BukkitRunnable() {
			public void run() {
				// If the entity is dead then it isn't talking
				if (linkedEntity.isDead()) {
					thisSpeechBubble.teleport(linkedEntity.getLocation().add(0, -500, 0));
				}
				
				// Else move it on top of the entity
				else {
					thisSpeechBubble.teleport(linkedEntity.getLocation().add(_xOffset, _yOffset + linkedEntity.getHeight() + _hologram.getHeight(), _zOffset));
				}
			}
		}.runTaskTimer(instancingPlugin, 1, 1);
	}
	
	public TextLine getLastTextLine() {
		return _lastLine;
	}
	
	public int getLastLineLength() {
		String lastLineString = getLastTextLine().getText();
		int length = lastLineString.length();
		
		// Remove formatting characters
		for (int i = 0; i < lastLineString.length() - 1; i++) {
			if (lastLineString.charAt(i) == '§') {
				length -= 2;
				i++;
			}
		}
		
		return length;
	}
	
	public String getLastLineString() {
		if (_lastLine == null) {
			return "";
		}
		
		return _lastLine.getText();
	}

	public void setLastLineText(String string) {
		// Check there is a line to add to
		if (_lastLine == null) {
			addNewLine();
		}
		
		_lastLine.setText(string);
	}
	
	public void clearText() {
		_hologram.clearLines();
		_lastLine = null;
	}
	
	public void addNewLine() {
		_lastLine = _hologram.appendTextLine("");
	}
	
	public NPC getNpc() {
		return _linkedNPC;
	}
	
	protected void teleport (Location loc) {
		_hologram.teleport(loc);
	}
}
