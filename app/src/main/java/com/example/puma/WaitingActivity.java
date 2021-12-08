package com.example.puma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class WaitingActivity extends AppCompatActivity {

    private Socket mSocket;
    private static final String TAG = WaitingActivity.class.getSimpleName();

    private String pin;
    private String nick;
    private String timeStart;
    private String timeStop;
    private String questionNo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        mSocket = SocketIoManager.getInstance().getSocket();
        Bundle bundle = getIntent().getExtras();

        configureBackButton();

        if (bundle != null) {
            pin = bundle.getString("pin");
            nick = bundle.getString("nick");
        }


        if (mSocket.connected()) {
            mSocket.on("LobbyEnd", new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                    startActivity(new Intent(WaitingActivity.this, MainActivity.class));
                }
            }).on("NewQuestion", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, data.toString());
                    System.out.println("Waiting");

                    try {
                        timeStart = data.getString("TimeStart");
                        timeStop = data.getString("TimeStop");
                        questionNo = data.getString("QuestionNo");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(WaitingActivity.this, AnswerActivity.class);
                    intent.putExtra("pin", pin);
                    intent.putExtra("nick", nick);
                    intent.putExtra("timeStart", timeStart);
                    intent.putExtra("timeStop", timeStop);
                    intent.putExtra("questionNo", questionNo);

                    startActivity(intent);
                }
            }).on("QuizEnd", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Intent intent = new Intent(WaitingActivity.this, LobbyActivity.class);
                    intent.putExtra("pin", pin);
                    intent.putExtra("nick", nick);

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
                startActivity(new Intent(WaitingActivity.this, MainActivity.class));
            }
        });
    }
}
