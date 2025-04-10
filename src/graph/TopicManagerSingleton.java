package graph;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe singleton manager for handling topics in the messaging system.
 * Uses the Bill Pugh singleton pattern for lazy initialization.
 */
public class TopicManagerSingleton {
    /**
     * Thread-safe manager for topics with concurrent access support.
     */
    public static class TopicManager {
        private final ConcurrentHashMap<String, Topic> topics = new ConcurrentHashMap<>();

        /**
         * Gets or creates a topic by name.
         * @param name The name of the topic
         * @return The topic instance
         */
        public Topic getTopic(String name) {
            return topics.computeIfAbsent(name, Topic::new);
        }

        /**
         * Returns all registered topics.
         * @return Collection of all topics
         */
        public Collection<Topic> getTopics() {
            return topics.values();
        }

        /**
         * Removes all topics from the manager.
         */
        public void clear() {
            topics.clear();
        }
    }

    /**
     * Holder class for lazy initialization of the singleton instance.
     */
    private static class Holder {
        private static final TopicManager INSTANCE = new TopicManager();
    }

    /**
     * Gets the singleton instance of the TopicManager.
     * @return The singleton TopicManager instance
     */
    public static TopicManager get() {
        return Holder.INSTANCE;
    }
}