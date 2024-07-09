package com.example.mogu;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebSocketService extends Service {
    private static final String TAG = "WebSocketService";
    private WebSocketClient webSocketClient;

    @Override
    public void onCreate() {
        super.onCreate();
        connectWebSocket();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void connectWebSocket() {
        URI uri = URI.create("ws://10.0.2.2:8080/ws");
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake handshake) {
                Log.d(TAG, "Connected to server");
            }

            @Override
            public void onMessage(String message) {
                Log.d(TAG, "Response: " + message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d(TAG, "Connection closed: " + reason);
            }

            @Override
            public void onError(Exception ex) {
                Log.e(TAG, "Error: " + ex.getMessage());
            }
        };
        webSocketClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webSocketClient != null) {
            webSocketClient.close();
        }
    }
}
