package com.example.testclient04;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int CMD_APPEND_TEXT = 0;
    public static final int CMD_ENABLE_CONNECT_BUTTON = 1;
    public static final int CMD_SHOW_BITMAP = 2;
    public static final int CMD_CALL = 3;
    public static final int CMD_ENDCALL = 4;
    public static final int CMD_BAT = 5;
    public static final int CMD_WARMING = 6;
    private ImageView mImageFrame;
    private EditText mEditIP;
    Button mBtnConnect;
    Button mBtnCall;
    Button mBtnEndCall;
    Button mBtnW;
    Button mBtnA;
    Button mBtnS;
    Button mBtnD;
    private ClientThread mClientThread;
    private UDPSendThread mUDPSendThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageFrame = (ImageView) findViewById(R.id.imageFrame);
        mEditIP = (EditText) findViewById(R.id.editIP);
        mBtnConnect = (Button) findViewById(R.id.btnConnect);
        mBtnW = (Button) findViewById(R.id.btnW);
        mBtnA = (Button) findViewById(R.id.btnA);
        mBtnS = (Button) findViewById(R.id.btnS);
        mBtnD = (Button) findViewById(R.id.btnD);
        mBtnCall = (Button)findViewById(R.id.btnCall);
        mBtnEndCall = (Button)findViewById(R.id.btnEndCall);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean auto_connect = prefs.getBoolean("pref_autoconnect", false);
        String default_ip = prefs.getString("pref_defaultip", "127.0.0.1");
        mEditIP.setText(default_ip);
        if (auto_connect)
            mOnClick(mBtnConnect);

        ((Button) findViewById(R.id.btnW)).setOnTouchListener(new RepeatListener(400, 50, new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_SEND_BOTTON1;
                    msg.obj = "w";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }));
        ((Button) findViewById(R.id.btnA)).setOnTouchListener(new RepeatListener(400, 50, new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_SEND_BOTTON2;
                    msg.obj = "a";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }));
        ((Button) findViewById(R.id.btnS)).setOnTouchListener(new RepeatListener(400, 50, new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_SEND_BOTTON3;
                    msg.obj = "s";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }));
        ((Button) findViewById(R.id.btnD)).setOnTouchListener(new RepeatListener(400, 50, new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_SEND_BOTTON4;
                    msg.obj = "d";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }));
        ((Button) findViewById(R.id.btnT)).setOnTouchListener(new RepeatListener(400, 50, new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_SEND_BOTTON5;
                    msg.obj = "t";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }));
        ((Button) findViewById(R.id.btnF)).setOnTouchListener(new RepeatListener(400, 50, new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_SEND_BOTTON6;
                    msg.obj = "f";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }));
        ((Button) findViewById(R.id.btnG)).setOnTouchListener(new RepeatListener(400, 50, new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_SEND_BOTTON7;
                    msg.obj = "g";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }));
        ((Button) findViewById(R.id.btnH)).setOnTouchListener(new RepeatListener(400, 50, new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_SEND_BOTTON8;
                    msg.obj = "h";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }));
    }
    private void createNotification(String a) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("배터리 부족!");
        builder.setContentText("배터리를 충전하세요!"+a+"%");

        builder.setColor(Color.RED);
        // 사용자가 탭을 클릭하면 자동 제거
        builder.setAutoCancel(true);

        // 알림 표시
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        // id값은
        // 정의해야하는 각 알림의 고유한 int값
        notificationManager.notify(1, builder.build());
    }
    private void WarmingNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("침입자 발생!");
        builder.setContentText("경고!경고!경고!");

        builder.setColor(Color.RED);
        // 사용자가 탭을 클릭하면 자동 제거
        builder.setAutoCancel(true);

        // 알림 표시
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        // id값은
        // 정의해야하는 각 알림의 고유한 int값
        notificationManager.notify(1, builder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_setting:
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
            case R.id.btnOpen:
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_SEND_OPEN;
                    msg.obj = "o";
                    SendThread.mHandler.sendMessage(msg);
                }
                break;
            case R.id.btnConnect:
                if (mClientThread != null) return;
                String addr = mEditIP.getText().toString();
                if (addr.length() == 0) return;
                mClientThread = new ClientThread(addr, mMainHandler);
                mClientThread.start();
                mBtnConnect.setEnabled(false);
                break;
            case R.id.btnCall:
                if (mUDPSendThread != null) {
                    break;
                }else {
                    String addr2 = mEditIP.getText().toString();
                    mUDPSendThread = new UDPSendThread(addr2);
                    mUDPSendThread.start();
                    if (SendThread.mHandler != null) {
                        Message msg = Message.obtain();
                        msg.what = SendThread.CMD_SEND_CALL;
                        msg.obj = addr2;
                        SendThread.mHandler.sendMessage(msg);
                    }
                    mBtnCall.setEnabled(false);
                }
                break;
            case R.id.btnEndCall:
                if(mUDPSendThread != null) {
                    mUDPSendThread.interrupt();
                    mUDPSendThread = null;
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_SEND_ENDCALL;
                    msg.obj = "end";
                    SendThread.mHandler.sendMessage(msg);
                }
                mBtnCall.setEnabled(true);
                break;
            case R.id.btnQuit:
                finish();
                break;
        }
    }

    private Handler mMainHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CMD_APPEND_TEXT:
                    break;
                case CMD_ENABLE_CONNECT_BUTTON:
                    mClientThread = null;
                    mBtnConnect.setEnabled(true);
                    break;
                case CMD_SHOW_BITMAP:
                    Bitmap bitmap = (Bitmap) msg.obj;
                    mImageFrame.setImageBitmap(bitmap);
                    break;
                case CMD_CALL:
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("통화 연결");
                    builder.setMessage("통화 연결 요청이 발생했습니다.");
                    builder.setPositiveButton("수락", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (mUDPSendThread != null) return;
                            String addr2 = mEditIP.getText().toString();
                            mUDPSendThread = new UDPSendThread(addr2);
                            if (SendThread.mHandler != null) {
                                Message msg = Message.obtain();
                                msg.what = SendThread.CMD_SEND_CALL;
                                msg.obj = addr2;
                                SendThread.mHandler.sendMessage(msg);
                            }
                            mUDPSendThread.start();
                            mBtnCall.setEnabled(false);
                        }
                    });
                    builder.setNegativeButton("거절", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (SendThread.mHandler != null) {
                                Message msg = Message.obtain();
                                msg.what = SendThread.CMD_SEND_REJECTCALL;
                                msg.obj = "reject";
                                SendThread.mHandler.sendMessage(msg);
                            }
                            Toast.makeText(getBaseContext(),"통화를 거절합니다.",Toast.LENGTH_LONG).show();
                        }
                    });
                    builder.create().show();
                    break;
                case CMD_ENDCALL:
                    if (mUDPSendThread != null){
                        mUDPSendThread.interrupt();
                        mUDPSendThread = null;
                        mBtnCall.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "통화가 종료되었습니다.", Toast.LENGTH_LONG).show();
                    }
                    break;
                case CMD_BAT:
                    String x =(String)msg.obj;
                    createNotification(x);
                    break;
                case CMD_WARMING:
                    WarmingNotification();
                    break;
            }
        }
    };
}