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

        mBtnVoiceStart=(Button)findViewById(R.id.btnVoiceStart);
        mBtnVoiceQuit=(Button)findViewById(R.id.btnVoiceQuit);

        mBtnSpeech=(Button)findViewById(R.id.btnSpeech);
        textView1=(TextView)findViewById(R.id.SpeechTextView);
        textView2=(TextView)findViewById(R.id.SpeechTextView2);

        if ( Build.VERSION.SDK_INT >= 23 ){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO},PERMISSION);
        }

        intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
        mBtnSpeech.setOnClickListener(v -> {
            mRecognizer= SpeechRecognizer.createSpeechRecognizer(this);
            mRecognizer.setRecognitionListener(listener);
            mRecognizer.startListening(intent);
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean auto_connect = prefs.getBoolean("pref_autoconnect", false);
        String default_ip = prefs.getString("pref_defaultip", "127.0.0.1");
        mEditIP.setText(default_ip);
        if (auto_connect)
            mOnClick(mBtnConnect);

        (mBtnForward).setOnTouchListener(new RepeatListener(3000,1, new View.OnClickListener(){
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

        (mBtnBackward).setOnTouchListener(new RepeatListener(3000,1, new View.OnClickListener(){
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
        (mBtnRight).setOnTouchListener(new RepeatListener(3000,1, new View.OnClickListener(){
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
        (mBtnLeft).setOnTouchListener(new RepeatListener(3000,1, new View.OnClickListener(){
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
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_settings:
                Intent intent=new Intent(this,SettingsActivity.class);
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

    public void mOnClick(View v){
        switch (v.getId()){
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
                if(mic != null)
                    break;
                else{
                    String address2=mEditIP.getText().toString();
                    mic=new VoiceSend(address2);
                    mic.start();
                    if(SendThread.mHandler!=null){
                        Message msg=Message.obtain();
                        msg.what=SendThread.CMD_VOICESTART;
                        msg.obj=address2;
                        SendThread.mHandler.sendMessage(msg);
                    }
                    mBtnVoiceStart.setEnabled(false);
                }
                break;
            case R.id.btnVoiceQuit:
                if(mic != null){
                    mic.interrupt();
                    mic=null;
                    Message msg=Message.obtain();
                    msg.what=SendThread.CMD_VOICEQUIT;
                    msg.obj="end";
                    SendThread.mHandler.sendMessage(msg);
                }
                mBtnVoiceStart.setEnabled(true);
                break;
        }
    }

    public RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
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
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }

            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) { // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
            String R="오른쪽";
            String L="왼쪽";
            String TURN="돌아";
            String GO="가";
            String STOP="멈춰";
            String speechtxt = new String();
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for(int i = 0; i < matches.size() ; i++){
                textView1.setText(matches.get(i));
            }
            speechtxt=matches.toString();
            if(speechtxt.contains(R)){
                    textView2.setText("Right");
                if (SendThread.mHandler != null) {
                    for(int cnt=0;cnt<45;cnt++) {
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
            }
            else if(speechtxt.contains(L)){
                textView2.setText("Left");
                if (SendThread.mHandler != null) {
                    for(int cnt=0;cnt<45;cnt++) {
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
            else if(speechtxt.contains(STOP)){
                textView2.setText("STOP");
                if (SendThread.mHandler != null) {
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
                case CMD_APPEND_TEXT: // 텍스트 출력
                    mTextStatus.append((String) msg.obj);
                    break;
                case CMD_ENABLE_CONNECT_BUTTON: // 연결 버튼 활성화
                    mClientThread = null;
                    mBtnConnect.setEnabled(true);
                    break;
                case CMD_SHOW_BITMAP: // 비트맵 출력
                    Bitmap bitmap = (Bitmap) msg.obj;
                    mImageFrame.setImageBitmap(bitmap);
                    break;
            }
        }
    };

}