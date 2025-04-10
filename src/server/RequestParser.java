package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * HTTP request parser that extracts command, URI, parameters, and content from incoming requests.
 * Supports multipart form data and query parameters.
 */
public class RequestParser {
    private static final String CONTENT_LENGTH_HEADER = "Content-Length";
    private static final String FILENAME_HEADER = "filename";
    private static final String CONTENT_TYPE_HEADER = "Content-Type:";
    private static final String BOUNDARY_MARKER = "----";

    /**
     * Parses an HTTP request and extracts all relevant information.
     */
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        RequestLine requestLine = parseRequestLine(reader);
        if (requestLine == null) return null;

        Map<String, String> parameters = new HashMap<>();
        byte[] content = parseContent(reader, parameters);

        return new RequestInfo(
            requestLine.httpCommand,
            requestLine.uri,
            requestLine.uriSegments,
            parameters,
            content
        );
    }

    private static class RequestLine {
        final String httpCommand;
        final String uri;
        final String[] uriSegments;

        RequestLine(String httpCommand, String uri, String[] uriSegments) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
        }
    }

    private static RequestLine parseRequestLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) return null;

        String[] parts = line.split(" ");
        if (parts.length < 2) return null;

        String httpCommand = parts[0];
        String uri = parts[1];
        String[] uriSegments = parseUriSegments(uri);

        return new RequestLine(httpCommand, uri, uriSegments);
    }

    private static String[] parseUriSegments(String uri) {
        String cleanUri = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;
        return Arrays.stream(cleanUri.split("/"))
                .filter(segment -> !segment.isEmpty())
                .toArray(String[]::new);
    }

    private static byte[] parseContent(BufferedReader reader, Map<String, String> parameters) throws IOException {
        StringBuilder contentBuilder = new StringBuilder();
        int contentLength = 0;
        boolean inContent = false;

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                inContent = true;
                continue;
            }

            if (inContent) {
                contentBuilder.append(line).append("\n");
            } else {
                parseHeader(line, parameters);
                if (line.startsWith(CONTENT_LENGTH_HEADER)) {
                    contentLength = Integer.parseInt(line.split(": ")[1]);
                }
            }
        }

        String contentString = contentBuilder.toString();
        return processContent(contentString, parameters, contentLength);
    }

    private static void parseHeader(String line, Map<String, String> parameters) {
        String[] parts = line.split(": ");
        if (parts.length == 2 && parts[0].equalsIgnoreCase(FILENAME_HEADER)) {
            parameters.put(FILENAME_HEADER, parts[1]);
        }
    }

    private static byte[] processContent(String contentString, Map<String, String> parameters, int contentLength) {
        if (contentLength <= 0 || contentString.isEmpty()) {
            return contentString.getBytes();
        }

        int filenameIndex = contentString.indexOf("filename=\"");
        if (filenameIndex == -1) {
            return contentString.getBytes();
        }

        return processMultipartContent(contentString, parameters, filenameIndex);
    }

    private static byte[] processMultipartContent(String contentString, Map<String, String> parameters, int filenameIndex) {
        int start = filenameIndex + "filename=\"".length();
        int end = contentString.indexOf("\"", start);
        if (end == -1) return contentString.getBytes();

        String filename = "\"" + contentString.substring(start, end) + "\"";
        parameters.put("filename", filename);

        int contentTypeIndex = contentString.indexOf(CONTENT_TYPE_HEADER);
        if (contentTypeIndex != -1) {
            return processContentWithType(contentString, contentTypeIndex);
        }

        return processContentWithoutType(contentString, end);
    }

    private static byte[] processContentWithType(String contentString, int contentTypeIndex) {
        int start = contentString.indexOf("\n", contentTypeIndex);
        start = contentString.indexOf("\n", start);
        String tmp = contentString.substring(start);
        int end = tmp.indexOf(BOUNDARY_MARKER);
        
        if (end != -1) {
            return tmp.substring(0, end).trim().getBytes();
        }
        return contentString.getBytes();
    }

    private static byte[] processContentWithoutType(String contentString, int end) {
        int contentStartIndex = contentString.indexOf("\n", end) + 1;
        contentStartIndex = contentString.indexOf("\n") + 1;
        return contentString.substring(contentStartIndex).getBytes();
    }

    /**
     * Immutable class holding parsed HTTP request information.
     */
    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        public RequestInfo(String httpCommand, String uri, String[] uriSegments, 
                         Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = Collections.unmodifiableMap(parameters);
            this.content = content;
        }

        public String getHttpCommand() { return httpCommand; }
        public String getUri() { return uri; }
        public String[] getUriSegments() { return uriSegments; }
        public Map<String, String> getParameters() { return parameters; }
        public byte[] getContent() { return content; }

        public void print() {
            System.out.println("HTTP Command: " + httpCommand);
            System.out.println("URI: " + uri);
            System.out.println("URI Segments: ");
            Arrays.stream(uriSegments).forEach(segment -> System.out.println("  - " + segment));
            System.out.println("Parameters: ");
            parameters.forEach((key, value) -> System.out.println("  " + key + " = " + value));
            if (content != null) {
                System.out.println("Content: " + new String(content));
            }
        }
    }
}