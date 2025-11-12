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

    private Set<WebSocket> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private Gson gson = new Gson();

    public BikeWebSocketServer(InetSocketAddress address) {
        super(address);
        System.out.println("WebSocket server started on port " + address.getPort());
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        System.out.println("New WebSocket connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
        System.out.println("Closed WebSocket connection: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
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

    public void broadcast(String jsonMessage) {
        for (WebSocket conn : connections) {
            conn.send(jsonMessage);
        }
        System.out.println("Broadcasted update to " + connections.size() + " clients.");
    }
}