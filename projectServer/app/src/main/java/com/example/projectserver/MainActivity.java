package com.example.projectserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final int CMD_APPEND_TEXT=0;
    /*블루투스*/
    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;

    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;

    private TextView mTextStatus;
    private CapturePreview mCapturePreview;
    private ServerThread mServerThread;

    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static int BT_FORWARD=4;
    final static int BT_RIGHT=5;
    final static int BT_BACKWARD=6;
    final static int BT_LEFT=7;
    final static int BT_STOP=8;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mCapturePreview = new CapturePreview(this,
                (SurfaceView) findViewById(R.id.surfPreview));

        if (mServerThread == null) { // 서버 시작
            mServerThread = new ServerThread(this, mMainHandler);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
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
    private class ConnectedBluetoothThread extends Thread{
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CMD_APPEND_TEXT: // 텍스트 출력
                    mTextStatus.append((String) msg.obj);
                    break;
            }
        }
    };

}