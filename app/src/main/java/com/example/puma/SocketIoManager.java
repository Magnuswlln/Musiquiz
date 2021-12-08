package com.example.puma;
import android.content.Context;
import android.util.Log;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.engineio.client.transports.WebSocket;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class SocketIoManager {

    private static volatile SocketIoManager instance;
    private static final String SERVER_ADDRESS = "https://55t.se:8040";
    private Socket mSocket;
    private static final String TAG = SocketIoManager.class.getSimpleName();
    private JSONObject jsonObject;
    private int i = 0;

    private SocketIoManager() {
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static synchronized SocketIoManager getInstance() {
        if (instance == null) { //Check for the first time

            synchronized (SocketIoManager.class) {   //Check for the second time.
                //if there is no instance available... create new one
                if (instance == null) {
                    instance = new SocketIoManager();
                }
            }
        }
        return instance;
    }

    public Socket connectSocket() {
        try {
            if (mSocket == null) {
                IO.Options opts = new IO.Options();
                opts.forceNew = true;
                opts.reconnection = true;
                opts.reconnectionAttempts = 5;
                opts.secure = true;
                opts.upgrade = false;
                opts.transports = new String[] {WebSocket.NAME};
                mSocket = IO.socket(SERVER_ADDRESS, opts);

                mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.i(TAG, "socket connected");
                    }
                }).on(Socket.EVENT_RECONNECTING, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.e(TAG, "Socket reconnecting");
                    }
                }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.e(TAG, "Socket disconnect event");
                    }

                }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Log.e(TAG, "Socket connect error");
                        //mSocket.disconnect();
                    }
                }).on(Socket.EVENT_ERROR, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        JsonObject error = (JsonObject) args[0];
                        Log.e(TAG + " error EVENT_ERROR ", error.toString());
                    }

                });

                mSocket.connect();
                return mSocket;
            } else if (!mSocket.connected()) {
                mSocket.connect();
                return mSocket;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void receiveJoinResponse(final CallBack callBack) {

        mSocket.on("JoinResponse", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

                jsonObject = (JSONObject) args[0];
                Log.d(TAG, jsonObject.toString());
                String response = "";
                int flag = 0;
                try {
                    response = jsonObject.getString("response");
                    flag = jsonObject.getInt("flag");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callBack.onJoinResponse(response, flag);
            }
        });
    }

    public Socket getSocket() {
        return this.mSocket;
    }

    public void setSocket(Socket socket) {
        this.mSocket = socket;
    }

    public void destroy() {
        if (mSocket != null) {
            mSocket.off();
            mSocket.disconnect();
            mSocket.close();
            mSocket = null;
        }
    }

    public JSONObject getJsonObject() {
        return this.jsonObject;
    }

}
