package com.example.puma;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    private EditText nickName;
    private EditText pinCode;
    private Socket mSocket;
    private static final String TAG = MainActivity.class.getSimpleName();

    private String pin;
    private String nick;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        joinGame();

        pinCode = findViewById(R.id.pin);
        nickName = findViewById(R.id.nickname);
        mSocket = SocketIoManager.getInstance().connectSocket();

    }

    private void joinGame() {

        Button nextButton = findViewById(R.id.button_next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                send(new CallBack() {

                    @Override
                    public void onJoinResponse(final String response, final int flag) {
                        System.out.println("Response: " + response);
                        System.out.println("Flag: " + flag);

                        Thread thread = new Thread() {
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        final Toast toast = Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.TOP, 0, 600);
                                        if (flag == 0) {
                                            toast.show();
                                        } else if (flag == 1) {
                                            toast.show();
                                        } else if (flag == 2) {
                                            Intent intent = new Intent(MainActivity.this, LobbyActivity.class);
                                            intent.putExtra("pin", pin);
                                            intent.putExtra("nick", nick);
                                            startActivity(intent);
                                        }
                                    }
                                });
                            }
                        };
                        thread.start();
                    }
                });
            }
        });
    }

    public void send(final CallBack callBack) {
        JSONObject message = new JSONObject();
        pin = pinCode.getText().toString();
        nick = nickName.getText().toString();

        // JOIN MESSAGE
        try {
            message.put("PIN", pinCode.getText().toString());
            message.put("Username", nickName.getText().toString());

        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        if(mSocket != null){
            mSocket.emit("Join", message);
            System.out.println("Skickar Join till server");
        }

        else{
            Log.e(TAG, "Socket is null");
        }


        SocketIoManager.getInstance().receiveJoinResponse(callBack);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketIoManager.getInstance().destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSocket = SocketIoManager.getInstance().getSocket();
        if(mSocket == null){
            mSocket = SocketIoManager.getInstance().connectSocket();
        }

        joinGame();
        System.out.println("nu anropades on resumse");
    }
}

