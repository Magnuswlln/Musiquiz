package com.example.puma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class AnswerActivity extends AppCompatActivity {

    private Socket mSocket;
    private static final String TAG = AnswerActivity.class.getSimpleName();

    CountDownTimer cTimer = null;

    TextView timer = null;

    private String pin;
    private String nick;
    private String timeStart;
    private String timeStop;
    private String questionNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);

        timer = findViewById(R.id.timer);

        mSocket = SocketIoManager.getInstance().getSocket();
        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            pin = bundle.getString("pin");
            nick = bundle.getString("nick");
            timeStart = bundle.getString("timeStart");
            timeStop = bundle.getString("timeStop");
            questionNo = bundle.getString("questionNo");
        }


        configureButtons();

        startTimer(Long.parseLong(timeStop) - Long.parseLong(timeStart));

        if (mSocket.connected()) {
            mSocket.on("LobbyEnd", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    startActivity(new Intent(AnswerActivity.this, MainActivity.class));
                }
            }).on("AnswerResponse", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject data = (JSONObject) args[0];
                    Log.d(TAG, data.toString());

                    Intent intent = new Intent(AnswerActivity.this, WaitingActivity.class);
                    intent.putExtra("pin", pin);
                    intent.putExtra("nick", nick);

                    startActivity(intent);
                }
            }).on("QuizEnd", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Intent intent = new Intent(AnswerActivity.this, LobbyActivity.class);
                    intent.putExtra("pin", pin);
                    intent.putExtra("nick", nick);

                    startActivity(intent);
                }
            });
        }
    }

    private void configureButtons(){
        Button firstButton = findViewById(R.id.rectangle);
        Button secondButton = findViewById(R.id.rectangle2);
        Button thirdButton = findViewById(R.id.rectangle3);
        Button fourthButton = findViewById(R.id.rectangle4);
        ImageButton backButton = findViewById(R.id.backButton);


        firstButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAnswer("1");
            }
        });

        secondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAnswer("2");
            }
        });

        thirdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAnswer("3");
            }
        });

        fourthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAnswer("4");
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AnswerActivity.this, MainActivity.class));
            }
        });
    }

    //start timer function
    void startTimer(long timeCount) {
        cTimer = new CountDownTimer(timeCount, 1000) {
            public void onTick(long millisUntilFinished) {

                if (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)) >= 10) {
                    timer.setText("" + String.format("%d:%d",
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                } else {
                    timer.setText("" + String.format("%d:0%d",
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                }
            }
            public void onFinish() {
                sendAnswer("0");
            }
        };
        cTimer.start();
    }

    //cancel timer
    void cancelTimer() {
        if(cTimer!=null)
            cTimer.cancel();
    }

    public void sendAnswer(String answer) {
        JSONObject message = new JSONObject();

        // JOIN MESSAGE
        try {
            message.put("PIN", pin);
            message.put("Username", nick);
            message.put("QuestionNo", questionNo);
            message.put("Answer", answer);

        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        mSocket.emit("Answer", message);
        System.out.println("Sending Answer to Server.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelTimer();
    }
}
