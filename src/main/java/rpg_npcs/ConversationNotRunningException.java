package rpg_npcs;

public class ConversationNotRunningException extends RuntimeException {
	private static final long serialVersionUID = 2535076859526230797L;
	
	private final Conversation conversation;
	
	public ConversationNotRunningException(Conversation conversation) {
		super("Conversation was attempted to be accessed in some way while it was not running");
		this.conversation = conversation;
	}

	/**
	 * @return the conversation
	 */
	public Conversation getConversation() {
		return conversation;
	}
}
