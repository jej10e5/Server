package com.example.client;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.UUID;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int CMD_APPEND_TEXT = 0;
    public static final int CMD_ENABLE_CONNECT_BUTTON =1;
    public static final int CMD_SHOW_BITMAP = 2;
    public static final int CMD_SHOW_MAP=3;
    public static final int CMD_BAT =4;

    private ImageView mImageFrame;
    private TextView mTextBattery;
    private TextView mTextStatus;
    private EditText mEditIP;
    private Button mBtnConnect;
    private ClientThread mClientThread;

    private GoogleMap mMap;
    private Marker mMarker;


    Button f;
    Button r;
    Button l;
    Button b;
    Button s;

    Button u;
    Button d;

    Button start;
    Button stop;

    private ChatSend mic;

    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageFrame = (ImageView) findViewById(R.id.imageFrame);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mEditIP = (EditText) findViewById(R.id.editIP);
        mBtnConnect = (Button) findViewById(R.id.btnConnect);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean auto_connect = prefs.getBoolean("pref_autoconnect", false);
        String default_ip=prefs.getString("pref_defaultip","");
        mEditIP.setText(default_ip);
        if(auto_connect)
            mOnClick(mBtnConnect);

        f = (Button) findViewById(R.id.btnF);
        r = (Button) findViewById(R.id.btnR);
        l = (Button) findViewById(R.id.btnL);
        b = (Button) findViewById(R.id.btnB);

        u = (Button) findViewById(R.id.btnU);
        d = (Button) findViewById(R.id.btnD);

        start=(Button) findViewById(R.id.startChat);
        stop=(Button) findViewById(R.id.stopChat);

        ((Button)findViewById(R.id.btnF)).setOnTouchListener(new RepeatListener(3000,1, new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_GOBUTTON;
                    msg.obj = "F";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }));

        ((Button)findViewById(R.id.btnB)).setOnTouchListener(new RepeatListener(3000,1, new View.OnClickListener(){
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
        ((Button)findViewById(R.id.btnR)).setOnTouchListener(new RepeatListener(3000,1, new View.OnClickListener(){
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
        ((Button)findViewById(R.id.btnL)).setOnTouchListener(new RepeatListener(3000,1, new View.OnClickListener(){
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

        u.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_UPBUTTON;
                    msg.obj = "U";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }) ;

        d.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SendThread.mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = SendThread.CMD_DOWNBUTTON;
                    msg.obj = "D";
                    SendThread.mHandler.sendMessage(msg);
                }
            }
        }) ;


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.item_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void mOnClick(View v){
        switch (v.getId()){
            case R.id.btnConnect:
                if(mClientThread != null) return;
                String addr = mEditIP.getText().toString();
                if(addr.length() == 0) return;
                mClientThread = new ClientThread(addr, mMainHandler);
                mClientThread.start();
                mBtnConnect.setEnabled(false);
                break;
            case R.id.btnQuit:
                finish();
                break;
            case R.id.startChat:
                if(mic != null)
                    break;
                else{
                    String address2=mEditIP.getText().toString();
                    mic=new ChatSend(address2);
                    mic.start();
                    if(SendThread.mHandler!=null){
                        Message msg=Message.obtain();
                        msg.what=SendThread.CMD_STARTCHAT;
                        msg.obj=address2;
                        SendThread.mHandler.sendMessage(msg);
                    }
                }
                break;
            case R.id.stopChat:
                if(mic != null){
                    mic.interrupt();
                    mic=null;
                    Message msg=Message.obtain();
                    msg.what=SendThread.CMD_STOPCHAT;
                    msg.obj="end";
                    SendThread.mHandler.sendMessage(msg);
                }
                break;
        }
    }

    private Handler mMainHandler = new Handler(){

        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case CMD_APPEND_TEXT:
                    mTextStatus.append((String)msg.obj);
                    break;
                case CMD_ENABLE_CONNECT_BUTTON:
                    mClientThread = null;
                    mBtnConnect.setEnabled(true);
                    break;
                case CMD_SHOW_BITMAP:
                    Bitmap bitmap =(Bitmap) msg.obj;
                    mImageFrame.setImageBitmap(bitmap);
                    break;
                case CMD_SHOW_MAP:
                    if(mMap == null) return;
                    LatLng latlng = (LatLng)msg.obj;
                    if(mMarker != null)mMarker.remove();
                    mMarker = mMap.addMarker(new MarkerOptions().position(latlng));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                    break;
                case CMD_BAT:
                    String x =(String)msg.obj;
                    createNotification(x);
                    break;

            }
        }
    };

    private void createNotification(String a) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default");

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("서버의 배터리가 부족합니다! 배터리를 충전하세요!");
        builder.setContentText("서버의 배터리 잔량 : "+a+"%");

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
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }



}