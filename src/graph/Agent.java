package graph;

/**
 * Interface representing an agent in the messaging system.
 * Agents can subscribe to topics and receive messages through callbacks.
 */
public interface Agent {
    /**
     * Gets the unique name of this agent.
     * @return The agent's name
     */
    String getName();

    /**
     * Resets the agent's state to its initial condition.
     */
    void reset();

    /**
     * Callback method invoked when a message is published to a subscribed topic.
     * @param topic The topic that received the message
     * @param msg The message that was published
     */
    void callback(String topic, Message msg);

    /**
     * Performs cleanup and releases any resources held by the agent.
     */
    void close();
}
