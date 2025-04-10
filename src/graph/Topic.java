package graph;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a topic in the messaging system that can have multiple publishers and subscribers.
 * Thread-safe implementation using CopyOnWriteArrayList for concurrent access.
 */
public class Topic {
    /** The unique name of the topic */
    public final String name;
    
    /** Thread-safe list of subscribers */
    private final List<Agent> subscribers = new CopyOnWriteArrayList<>();
    
    /** Thread-safe list of publishers */
    private final List<Agent> publishers = new CopyOnWriteArrayList<>();
    
    /** The most recently published message */
    private Message lastMessage;

    /**
     * Creates a new topic with the specified name.
     * @param name The name of the topic
     * @throws IllegalArgumentException if name is null or empty
     */
    public Topic(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Topic name cannot be null or empty");
        }
        this.name = name;
    }

    /**
     * Adds a subscriber to this topic if not already subscribed.
     * @param agent The agent to subscribe
     */
    public void subscribe(Agent agent) {
        if (!subscribers.contains(agent)) {
            subscribers.add(agent);
        }
    }

    /**
     * Removes a subscriber from this topic.
     * @param agent The agent to unsubscribe
     */
    public void unsubscribe(Agent agent) {
        subscribers.remove(agent);
    }

    /**
     * Publishes a message to all subscribers.
     * @param message The message to publish
     */
    public void publish(Message message) {
        lastMessage = message;
        subscribers.forEach(agent -> agent.callback(this.name, message));
    }

    /**
     * Adds a publisher to this topic if not already added.
     * @param agent The agent to add as publisher
     */
    public void addPublisher(Agent agent) {
        if (!publishers.contains(agent)) {
            publishers.add(agent);
        }
    }

    /**
     * Removes a publisher from this topic.
     * @param agent The agent to remove as publisher
     */
    public void removePublisher(Agent agent) {
        publishers.remove(agent);
    }

    /**
     * Gets the most recently published message.
     * @return The last message, or null if no message has been published
     */
    public Message getLastMessage() {
        return lastMessage;
    }

    /**
     * Gets the list of current subscribers.
     * @return List of subscriber agents
     */
    public List<Agent> getSubscribers() {
        return subscribers;
    }

    /**
     * Gets the list of current publishers.
     * @return List of publisher agents
     */
    public List<Agent> getPublishers() {
        return publishers;
    }
}