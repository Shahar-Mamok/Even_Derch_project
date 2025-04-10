package graph;
import java.util.Date;

/**
 * Immutable message class that can represent data in multiple formats (text, bytes, double).
 * Each message is timestamped with its creation date.
 */
public final class Message {
    /** Raw byte representation of the message */
    public final byte[] data;
    
    /** Text representation of the message */
    public final String asText;
    
    /** Numeric representation of the message (NaN if not a valid number) */
    public final double asDouble;
    
    /** Timestamp when the message was created */
    public final Date date;

    /**
     * Creates a message from a string.
     * @param str The string content
     * @throws IllegalArgumentException if str is null
     */
    public Message(String str) {
        if (str == null) {
            throw new IllegalArgumentException("String cannot be null");
        }
        this.data = str.getBytes();
        this.asText = str;
        this.asDouble = parseDoubleSafely(str);
        this.date = new Date();
    }

    /**
     * Creates a message from a byte array.
     * @param bytes The byte array content
     */
    public Message(byte[] bytes) {
        this(new String(bytes));
    }

    /**
     * Creates a message from a double value.
     * @param value The numeric value
     */
    public Message(double value) {
        this(Double.toString(value));
    }

    /**
     * Safely parses a string to double, returning NaN if parsing fails.
     * @param str The string to parse
     * @return The parsed double value or NaN if parsing fails
     */
    private static double parseDoubleSafely(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}