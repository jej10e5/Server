package com.example.exploreclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    public static final int CMD_APPEND_TEXT = 0;
    public static final int CMD_ENABLE_CONNECT_BUTTON = 1;
    public static final int CMD_SHOW_BITMAP = 2;
    public static final int CMD_SHOW_MAP = 3;

    private ImageView mImageFrame;
    private TextView mTextStatus;
    private EditText mEditIP;
    private Button mBtnConnect;
    private ClientThread mClientThread;
    private SendThread mSendThread;

    Button mBtnRight;
    Button mBtnLeft;
    Button mBtnForward;
    Button mBtnBackward;
    Button mBtnStop;

    Button mBtnVoiceStart;
    Button mBtnVoiceQuit;
    private VoiceSend mic;

    Button mBtnSpeech;
    final int PERMISSION = 1;
    Intent intent;
    SpeechRecognizer mRecognizer;
    TextView textView1;
    TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageFrame = (ImageView) findViewById(R.id.imageFrame);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mEditIP = (EditText) findViewById(R.id.editIP);
        mBtnConnect = (Button) findViewById(R.id.btnConnect);

        mBtnForward = (Button) findViewById(R.id.btnForward);
        mBtnRight = (Button) findViewById(R.id.btnRight);
        mBtnBackward = (Button) findViewById(R.id.btnBackward);
        mBtnLeft = (Button) findViewById(R.id.btnLeft);
        mBtnStop = (Button) findViewById(R.id.btnStop);

        mBtnVoiceStart = (Button) findViewById(R.id.btnVoiceStart);
        mBtnVoiceQuit = (Button) findViewById(R.id.btnVoiceQuit);

        mBtnSpeech = (Button) findViewById(R.id.btnSpeech);
        textView1 = (TextView) findViewById(R.id.SpeechTextView);
        textView2 = (TextView) findViewById(R.id.SpeechTextView2);

        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        mBtnSpeech.setOnClickListener(v -> {
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            mRecognizer.setRecognitionListener(listener);
            mRecognizer.startListening(intent);
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean auto_connect = prefs.getBoolean("pref_autoconnect", false);
        String default_ip = prefs.getString("pref_defaultip", "127.0.0.1");
        mEditIP.setText(default_ip);
        if (auto_connect)
            mOnClick(mBtnConnect);

        //??? ???????????? ?????? ???????????? ???????????? RepeatListener??? ??????
        //initialinterval??? ?????? ????????? ??? ????????? ????????????????????? ??????????????? ?????????
        //normalinterval??? ?????? ????????? ??????
        (mBtnForward).setOnTouchListener(new RepeatListener(3000, 1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_FOWARDBUTTON;
                    msg.obj = "F";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }));

        (mBtnBackward).setOnTouchListener(new RepeatListener(3000, 1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_BACKBUTTON;
                    msg.obj = "B";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }));
        (mBtnRight).setOnTouchListener(new RepeatListener(3000, 1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_RIGHTBUTTON;
                    msg.obj = "R";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }));
        (mBtnLeft).setOnTouchListener(new RepeatListener(3000, 1, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_LEFTBUTTON;
                    msg.obj = "L";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }));

        mBtnStop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_STOP;
                    msg.obj = "S";
                    SendThread.mHandler.sendMessage(msg);

                }
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void mOnClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnect:
                if (mClientThread != null) return;
                String addr = mEditIP.getText().toString();
                if (addr.length() == 0) return;
                mClientThread = new ClientThread(addr, mMainHandler);
                mClientThread.start();
                mBtnConnect.setEnabled(false);
                break;
            case R.id.btnQuit:
                finish();
                break;
            case R.id.btnVoiceStart:
                if (mic != null)
                    break;
                else {
                    String address2 = mEditIP.getText().toString();
                    mic = new VoiceSend(address2);
                    mic.start();
                    if (SendThread.mHandler != null) {
                        Message msg = Message.obtain();
                        msg.what = SendThread.CMD_VOICESTART;
                        msg.obj = address2;
                        SendThread.mHandler.sendMessage(msg);
                    }
                    mBtnVoiceStart.setEnabled(false);
                }
                break;
            case R.id.btnVoiceQuit:
                if (mic != null) {
                    mic.interrupt();
                    mic = null;
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_VOICEQUIT;
                    msg.obj = "end";
                    SendThread.mHandler.sendMessage(msg);
                }
                mBtnVoiceStart.setEnabled(true);
                break;
        }
    }

    //???????????? ??????form
    public RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "??????????????? ???????????????.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {
            String message;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "????????? ??????";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "??????????????? ??????";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "????????? ??????";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "???????????? ??????";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "????????? ????????????";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "?????? ??? ??????";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER??? ??????";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "????????? ?????????";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "????????? ????????????";
                    break;
                default:
                    message = "??? ??? ?????? ?????????";
                    break;
            }

            Toast.makeText(getApplicationContext(), "????????? ?????????????????????. : " + message, Toast.LENGTH_SHORT).show();
        }

        //???????????? ??????
        @Override
        public void onResults(Bundle results) { // ?????? ?????? ArrayList??? ????????? ?????? textView??? ????????? ???????????????.
            String R = "?????????";
            String L = "??????";
            String TURN = "??????";
            String GO = "???";
            String BACK = "???";
            String STOP = "??????";
            String DANCE = "???";
            String speechtxt = new String();
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < matches.size(); i++) {
                textView1.setText(matches.get(i));
            }
            //???????????? ????????? ????????? ???????????? ???????????? ?????? ArrayList??? ????????? matches??? String????????? ?????????
            speechtxt = matches.toString();

            if (speechtxt.contains(TURN)) {//?????? ??????
                if (speechtxt.contains(R)) {//String?????? ????????? ????????? ????????????????????? ???????????? ?????? contain?????? ??????
                    textView2.setText("Right Turn");
                    if (SendThread.mHandler != null) {
                        Message msg = Message.obtain();
                        msg.what = SendThread.CMD_RIGHTBUTTON;
                        msg.obj = "R";
                        SendThread.mHandler.sendMessage(msg);
                    }
                } else if (speechtxt.contains(L)) {
                    textView2.setText("Left Turn");
                    if (SendThread.mHandler != null) {
                        Message msg = Message.obtain();
                        msg.what = SendThread.CMD_LEFTBUTTON;
                        msg.obj = "L";
                        SendThread.mHandler.sendMessage(msg);
                    }
                }
            } else if (speechtxt.contains(GO)) {//?????????
                textView2.setText("Go");
                if (SendThread.mHandler != null) {
                    for (int cnt = 0; cnt < 30; cnt++) { //????????? ??????????????? ??????
                        Message msg = Message.obtain();
                        msg.what = SendThread.CMD_FOWARDBUTTON;
                        msg.obj = "F";
                        SendThread.mHandler.sendMessage(msg);
                    }
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_STOP;
                    msg.obj = "S";
                    SendThread.mHandler.sendMessage(msg);
                }
            } else if (speechtxt.contains(BACK)) {//??????
                textView2.setText("Back");
                if (SendThread.mHandler != null) {
                    for (int cnt = 0; cnt < 30; cnt++) { //????????? ??????????????? ??????
                        Message msg = Message.obtain();
                        msg.what = SendThread.CMD_BACKBUTTON;
                        msg.obj = "B";
                        SendThread.mHandler.sendMessage(msg);
                    }
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_STOP;
                    msg.obj = "S";
                    SendThread.mHandler.sendMessage(msg);
                }
            } else if (speechtxt.contains(STOP)) {
                textView2.setText("STOP");
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_STOP;
                    msg.obj = "S";
                    SendThread.mHandler.sendMessage(msg);
                }
            } else if (speechtxt.contains(R)) {//????????? ????????? ?????? ????????? ??????
                textView2.setText("Right");
                if (SendThread.mHandler != null) {
                    for (int cnt = 0; cnt < 45; cnt++) { //????????? ?????? ????????? ???????????? ?????? for????????? delay????????? ?????????
                        Message msg = Message.obtain();
                        msg.what = SendThread.CMD_RIGHTBUTTON;
                        msg.obj = "R";
                        SendThread.mHandler.sendMessage(msg);
                    }
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_STOP;
                    msg.obj = "S";
                    SendThread.mHandler.sendMessage(msg);
                }
            } else if (speechtxt.contains(L)) {//????????? ????????? ?????? ????????? ??????
                textView2.setText("Left");
                if (SendThread.mHandler != null) {
                    for (int cnt = 0; cnt < 45; cnt++) {
                        Message msg = Message.obtain();
                        msg.what = SendThread.CMD_LEFTBUTTON;
                        msg.obj = "L";
                        SendThread.mHandler.sendMessage(msg);
                    }
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_STOP;
                    msg.obj = "S";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }


        @Override
        public void onPartialResults(Bundle partialResults) {}
        @Override
        public void onEvent(int eventType, Bundle params) {}
        };


    private Handler mMainHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CMD_APPEND_TEXT: // ????????? ??????
                    mTextStatus.append((String) msg.obj);
                    break;
                case CMD_ENABLE_CONNECT_BUTTON: // ?????? ?????? ?????????
                    mClientThread = null;
                    mBtnConnect.setEnabled(true);
                    break;
                case CMD_SHOW_BITMAP: // ????????? ??????
                    Bitmap bitmap = (Bitmap) msg.obj;
                    mImageFrame.setImageBitmap(bitmap);
                    break;
            }
        }
    };

}