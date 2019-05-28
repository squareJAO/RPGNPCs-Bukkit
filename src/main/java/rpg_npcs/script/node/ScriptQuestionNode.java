package rpg_npcs.script.node;

import java.util.List;

import org.bukkit.entity.Player;
import com.gmail.filoghost.holographicdisplays.api.handler.TouchHandler;

import rpg_npcs.Conversation;
import rpg_npcs.SpeechBubble;
import rpg_npcs.script.Script;

public class ScriptQuestionNode extends ScriptNode {
	private final List<QuestionOption> _questions;

	public ScriptQuestionNode(List<QuestionOption> questions) {
		super();
		
		_questions = questions;
	}

	@Override
	protected void startThis(Conversation conversation) {
		for (QuestionOption question : _questions) {
			SpeechBubble bubble = conversation.getSpeechBubble();
			
			// Add the answer as a new line
			bubble.addNewLine();
			bubble.setLastLineText(question.textString);
			
			// Add a method to change the flow of conversation based on the answer
			Script answerScript = question.script;
			bubble.getLastTextLine().setTouchHandler(new TouchHandler() {
				@Override
				public void onTouch(Player player) {
					if (conversation.isPlayer(player)) {
						// Queue up next node
						finished(answerScript.initialNode, conversation);
					}
				}
			});
		}
	}

	@Override
	public void stopNode(Conversation conversation) {
		// Stub
	}

	@Override
	protected String getNodeRepresentation() {
		return "<Ask question>";
	}

}
