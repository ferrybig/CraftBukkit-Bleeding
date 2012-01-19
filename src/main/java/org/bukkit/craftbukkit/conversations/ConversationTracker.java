package org.bukkit.craftbukkit.conversations;

import org.bukkit.conversations.Conversation;

import java.util.Deque;
import java.util.LinkedList;

/**
 */
public class ConversationTracker {

    private Deque<Conversation> conversationQueue = new LinkedList<Conversation>();

    public synchronized void beginConversation(Conversation conversation) {
        if (!conversationQueue.contains(conversation)) {
            conversationQueue.addLast(conversation);
            if (conversationQueue.getFirst() == conversation) {
                conversation.begin();
            }
        }
    }

    public synchronized void abandonConversation(Conversation conversation) {
        if (conversationQueue.size() != 0) {
            if (conversationQueue.getFirst() == conversation) {
                conversation.abandon();
            }
            if (conversationQueue.contains(conversation)) {
                conversationQueue.remove(conversation);
            }
        }
    }

    public synchronized void abandonAllConversations() {

        Deque<Conversation> oldQueue = conversationQueue;
        conversationQueue = new LinkedList<Conversation>();
        for(Conversation conversation : oldQueue) {
            conversation.abandon();
        }
    }

    public synchronized void acceptConversationInput(String input) {
        if (isConversing()) {
            conversationQueue.getFirst().acceptInput(input);
        }
    }

    public synchronized boolean isConversing() {
        return !conversationQueue.isEmpty();
    }

    public synchronized boolean isConversingModaly() {
        return isConversing() && conversationQueue.getFirst().isModal();
    }
}
