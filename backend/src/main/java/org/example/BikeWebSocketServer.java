// New File: BikeWebSocketServer.java
package org.example;

import com.google.gson.Gson;
import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.handshake.ClientHandshake;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BikeWebSocketServer extends WebSocketServer {

    // A thread-safe set to store all connected clients
    private Set<WebSocket> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Gson gson = new Gson();

    public BikeWebSocketServer(InetSocketAddress address) {
        super(address);
        System.out.println("WebSocket server started on port " + address.getPort());
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn); // Add new connection
        System.out.println("New WebSocket connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn); // Remove connection on close
        System.out.println("Closed WebSocket connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // We don't need clients to send messages for this feature,
        // but you could add features here later.
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        // This will print the full, detailed error
        System.err.println("--- WebSocket Error ---");
        ex.printStackTrace();
        System.err.println("-------------------------");

        if (conn != null) {
            connections.remove(conn);
        }
    }

    @Override
    public void onStart() {
        // not needed
    }

    /**
     * This is the key method. It sends a message (JSON string)
     * to EVERY connected client.
     */
    public void broadcast(String jsonMessage) {
        for (WebSocket conn : connections) {
            conn.send(jsonMessage);
        }
        System.out.println("Broadcasted update to " + connections.size() + " clients.");
    }
}