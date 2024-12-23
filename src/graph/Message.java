package graph;
import java.util.Date;

public final class Message {
    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    public Message(String str) { //constructor
        if (str == null) {
            throw new IllegalArgumentException("String cannot be null");
        }
        this.data = str.getBytes();
        this.asText = str;
        this.asDouble = parseDoubleSafely(str);
        this.date = new Date();
    }

    public Message(byte[] bytes) {
        this(new String(bytes));
    }

    public Message(double d) {
        this(Double.toString(d));
    }

    private static double parseDoubleSafely(String str) {
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}