package server;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.*;

/**
 * A robust HTTP server implementation that handles concurrent requests using a thread pool.
 * Supports GET, POST, and DELETE operations with URI-based routing to servlets.
 * 
 * @author Your Name
 * @version 1.0
 */
public class MyHTTPServer extends Thread implements HTTPServer {
    // Servlet mappings for different HTTP methods
    private final ConcurrentHashMap<String, Servlet> getServlets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Servlet> postServlets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Servlet> deleteServlets = new ConcurrentHashMap<>();

    private final ExecutorService requestHandlerPool;
    private ServerSocket serverSocket;
    private volatile boolean isServerStopped = false;
    private final int port;
    private final int threadCount;
    private static final int SOCKET_TIMEOUT = 1000;
    private static final int REQUEST_DELAY = 125;

    /**
     * Creates a new HTTP server instance.
     *
     * @param port The port to listen on
     * @param threadCount Number of threads in the request handling pool
     */
    public MyHTTPServer(int port, int threadCount) {
        this.port = port;
        this.threadCount = threadCount;
        this.requestHandlerPool = Executors.newFixedThreadPool(threadCount);
    }

    /**
     * Registers a servlet for a specific HTTP method and URI pattern.
     *
     * @param httpCommand HTTP method (GET, POST, DELETE)
     * @param uri URI pattern to match
     * @param servlet Servlet instance to handle matching requests
     */
    public void addServlet(String httpCommand, String uri, Servlet servlet) {
        if (uri == null || servlet == null) return;

        switch (httpCommand.toUpperCase()) {
            case "GET" -> getServlets.put(uri, servlet);
            case "POST" -> postServlets.put(uri, servlet);
            case "DELETE" -> deleteServlets.put(uri, servlet);
        }
    }

    /**
     * Removes a servlet registration.
     *
     * @param httpCommand HTTP method
     * @param uri URI pattern
     */
    public void removeServlet(String httpCommand, String uri) {
        if (uri == null) return;

        switch (httpCommand.toUpperCase()) {
            case "GET" -> getServlets.remove(uri);
            case "POST" -> postServlets.remove(uri);
            case "DELETE" -> deleteServlets.remove(uri);
        }
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            this.serverSocket = serverSocket;
            serverSocket.setSoTimeout(SOCKET_TIMEOUT);

            while (!isServerStopped) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    handleClientRequest(clientSocket);
                } catch (IOException e) {
                    if (isServerStopped) break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles an incoming client request asynchronously.
     */
    private void handleClientRequest(Socket clientSocket) {
        requestHandlerPool.submit(() -> {
            try {
                Thread.sleep(REQUEST_DELAY);
                processRequest(clientSocket);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeClientSocket(clientSocket);
            }
        });
    }

    /**
     * Processes the HTTP request and routes it to the appropriate servlet.
     */
    private void processRequest(Socket clientSocket) throws IOException {
        BufferedReader requestReader = createBufferedReader(clientSocket);
        RequestParser.RequestInfo requestInfo = RequestParser.parseRequest(requestReader);

        if (requestInfo != null) {
            Servlet matchingServlet = findMatchingServlet(requestInfo);
            if (matchingServlet != null) {
                matchingServlet.handle(requestInfo, clientSocket.getOutputStream());
            }
        }
        requestReader.close();
    }

    /**
     * Finds the best matching servlet for the request based on URI pattern.
     */
    private Servlet findMatchingServlet(RequestParser.RequestInfo requestInfo) {
        ConcurrentHashMap<String, Servlet> servletMap = switch (requestInfo.getHttpCommand()) {
            case "GET" -> getServlets;
            case "POST" -> postServlets;
            case "DELETE" -> deleteServlets;
            default -> throw new IllegalArgumentException("Unsupported HTTP command: " + requestInfo.getHttpCommand());
        };

        return servletMap.entrySet().stream()
                .filter(entry -> requestInfo.getUri().startsWith(entry.getKey()))
                .max(Map.Entry.comparingByKey((a, b) -> a.length() - b.length()))
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    private static BufferedReader createBufferedReader(Socket clientSocket) throws IOException {
        InputStream inputStream = clientSocket.getInputStream();
        byte[] buffer = new byte[inputStream.available()];
        int bytesRead = inputStream.read(buffer);
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buffer, 0, bytesRead)));
    }

    private void closeClientSocket(Socket clientSocket) {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        isServerStopped = false;
        super.start();
    }

    public void close() {
        isServerStopped = true;
        requestHandlerPool.shutdownNow();
    }

    public ExecutorService getThreadPool() {
        return requestHandlerPool;
    }
}