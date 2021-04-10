package com.example.exploreserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    /*블루투스*/
    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;

    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;
    VoiceSend mic;

    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;

    public static final int CMD_FORWARDBUTTON = 4;
    public static final int CMD_BACKBUTTON = 5;
    public static final int CMD_RIGHTBUTTON = 6;
    public static final int CMD_LEFTBUTTON = 7;
    public static final int CMD_STOP = 8;

    public static final int CMD_VOICESTART = 9;
    public static final int CMD_VOICEQUIT = 10;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final int CMD_APPEND_TEXT=0;

    private TextView mTextStatus;
    private CapturePreview mCapturePreview;
    private DeviceLocation mDeviceLocation;
    private ServerThread mServerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextStatus=(TextView)findViewById(R.id.textStatus);
        mCapturePreview=new CapturePreview(this,(SurfaceView)findViewById(R.id.surfPreview));
        mDeviceLocation=new DeviceLocation(this);

        if(mServerThread==null) {//서버시작
            mServerThread = new ServerThread(this,mMainHandler);
            mServerThread.start();
        }

        mBluetoothHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == BT_MESSAGE_READ) {
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDeviceLocation.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDeviceLocation.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void mOnClick(View v){
        switch (v.getId()){
            case R.id.btnQuit:
                finish();
                break;
        }
    }
    private Handler mMainHandler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case CMD_APPEND_TEXT://텍스트출력
                    mTextStatus.append((String)msg.obj);
                    break;
                 //버튼 조작시
                case CMD_FORWARDBUTTON:
                    mThreadConnectedBluetooth.write("F");
                    break;
                case CMD_BACKBUTTON:
                    mThreadConnectedBluetooth.write("B");
                    break;
                case CMD_RIGHTBUTTON:
                    mThreadConnectedBluetooth.write("R");
                    break;
                case CMD_LEFTBUTTON:
                    mThreadConnectedBluetooth.write("L");
                    break;
                case CMD_STOP:
                    mThreadConnectedBluetooth.write("S");
                    break;
                case CMD_VOICESTART:
                    if (mic != null) {
                        return;
                    }
                    mic = new VoiceSend(mServerThread.ip2);
                    mic.start();
                    break;
                case CMD_VOICEQUIT:
                    if (mic != null) {
                        mic.interrupt();
                        mic = null;
                    }
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.bluetooth_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        switch (item.getItemId()){
            case R.id.bluetooth_On:
                bluetoothOn();
                return true;
            case R.id.bluetooth_Off:
                bluetoothOff();
                return true;
            case R.id.bluetooth_Connect:
                listPairedDevices();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    void bluetoothOn() {
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어있습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }

    void bluetoothOff() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되었습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case BT_REQUEST_ENABLE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_SHORT).show();
                } else if (requestCode == RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();
            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("장치 선택");

                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                }

                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "블루투스 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    void connectSelectedDevice (String selectedDeviceName){
        for (BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())){
                mBluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            ;
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();

        } catch(IOException e) {
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    public class ConnectedBluetoothThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedBluetoothThread(BluetoothSocket socket){
            mmSocket=socket;
            InputStream tmpIn=null;
            OutputStream tmpOut=null;

            try{
                tmpIn=socket.getInputStream();
                tmpOut=socket.getOutputStream();
            }catch (IOException e){
                Toast.makeText(getApplicationContext(),"소켓 연결 중 오류가 발생했습니다.",Toast.LENGTH_SHORT).show();
            }
            mmInStream=tmpIn;
            mmOutStream=tmpOut;
        }
        public void run(){
            byte[] buffer=new byte[1024];
            int bytes;
            Message msg=Message.obtain();

            while (true){
                try{
                    bytes=mmInStream.available();
                    if(bytes!=0){
                        SystemClock.sleep(100);
                        bytes=mmInStream.available();
                        bytes=mmInStream.read(buffer,0,bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                }catch (IOException e){
                    break;
                }
            }
        }
        public void write(String str){
            byte[] bytes=str.getBytes();
            try{
                mmOutStream.write(bytes);
            }catch (IOException e){
                Toast.makeText(getApplicationContext(),"데이터 전송 중 오류가 발생했습니다.",Toast.LENGTH_SHORT).show();
            }
        }
        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException e){
                Toast.makeText(getApplicationContext(),"소켓 해제 중 오류가 발생했습니다.",Toast.LENGTH_SHORT).show();
            }
        }
    }
}