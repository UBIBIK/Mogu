package com.example.mogu;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketHelper {

    private static final String TAG = "WebSocketHelper";
    private WebSocketClient webSocketClient;
    private static WebSocketHelper instance;

    private WebSocketHelper() {
    }

    public static synchronized WebSocketHelper getInstance() {
        if (instance == null) {
            instance = new WebSocketHelper();
        }
        return instance;
    }

    public void connect(String serverUri) {
        URI uri;
        try {
            uri = new URI(serverUri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                Log.d(TAG, "WebSocket Opened");
            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, "WebSocket Message: " + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d(TAG, "WebSocket Closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                Log.d(TAG, "WebSocket Error: " + ex.getMessage());
            }
        };
        webSocketClient.connect();
    }

    public void sendMessage(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
        } else {
            Log.d(TAG, "WebSocket is not open");
        }
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
}
