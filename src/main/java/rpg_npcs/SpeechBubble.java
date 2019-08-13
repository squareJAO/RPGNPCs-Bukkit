package rpg_npcs;

import java.util.List;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;

import net.citizensnpcs.api.npc.NPC;

public class SpeechBubble {
	protected JavaPlugin instancingPlugin;
	
	protected NPC linkedNPC;
	protected Hologram hologram;
	protected TextLine lastLine;
	protected List<Color> currentColors; 

	protected double _xOffset = 0;
	protected double _yOffset = 0.8;
	protected double _zOffset = 0;
	
	public SpeechBubble(JavaPlugin instancingPlugin, NPC linkedNPC) {
		// Pass values
		this.instancingPlugin = instancingPlugin;
		this.linkedNPC = linkedNPC;
		
		Entity linkedEntity = linkedNPC.getEntity();
		
		// Create hologram
		hologram = HologramsAPI.createHologram(instancingPlugin, linkedEntity.getLocation());
		
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
					thisSpeechBubble.teleport(linkedEntity.getLocation().add(_xOffset, _yOffset + linkedEntity.getHeight() + hologram.getHeight(), _zOffset));
				}
			}
		}.runTaskTimer(instancingPlugin, 1, 1);
	}
	
	public TextLine getLastTextLine() {
		return lastLine;
	}
	
	public int getLastLineLength() {
		if (lastLine == null) {
			return 0;
		}
		
		String lastLineString = lastLine.getText();
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
		if (lastLine == null) {
			return "";
		}
		
		return lastLine.getText();
	}

	public void setLastLineText(String string) {
		// Check there is a line to add to
		if (lastLine == null) {
			addNewLine();
		}
		
		lastLine.setText(string);
	}
	
	public void clearText() {
		hologram.clearLines();
		lastLine = null;
	}
	
	public void addNewLine() {
		lastLine = hologram.appendTextLine("");
	}
	
	public NPC getNpc() {
		return linkedNPC;
	}
	
	protected void teleport (Location loc) {
		hologram.teleport(loc);
	}
}
