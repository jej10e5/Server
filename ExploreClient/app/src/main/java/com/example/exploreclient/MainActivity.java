package com.example.exploreclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


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
                break;
        }
    }





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