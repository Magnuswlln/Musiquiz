package com.example.puma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class LobbyActivity extends AppCompatActivity {

    private Socket mSocket;
    private static final String TAG = LobbyActivity.class.getSimpleName();

    private String pin;
    private String nick;
    private String timeStart;
    private String timeStop;
    private String questionNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lobby);
        configureBackButton();
        mSocket = SocketIoManager.getInstance().getSocket();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            pin = bundle.getString("pin");
            nick = bundle.getString("nick");
        }

        if (mSocket.connected()) {
            mSocket.on("LobbyEnd", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    startActivity(new Intent(LobbyActivity.this, MainActivity.class));
                }
            }).on("NewQuestion", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, data.toString());
                    System.out.println("Lobby");

                    try {
                        timeStart = data.getString("TimeStart");
                        timeStop = data.getString("TimeStop");
                        questionNo = data.getString("QuestionNo");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(LobbyActivity.this, AnswerActivity.class);
                    intent.putExtra("pin", pin);
                    intent.putExtra("nick", nick);
                    intent.putExtra("timeStart", timeStart);
                    intent.putExtra("timeStop", timeStop);
                    intent.putExtra("questionNo", questionNo);

                    startActivity(intent);
                }
            });
        }
    }

    private void configureBackButton() {

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LobbyActivity.this, MainActivity.class));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSocket.off();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSocket.open();
    }
}