package com.example.server;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;

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
import java.util.concurrent.Semaphore;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2  {
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
    private CapturePreview mCapturePreview;//삭제
    private ServerThread mServerThread;

    /**/////////////////////////////////////////////////
    int facestack = 0;//OpenCV로 부터 받아온 데이터를 사용하기위한 변수
    private static final String TAG = "opencv";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat matInput;
    private Mat matResult;
    boolean autoDetectionOnOff = false;

    public native long loadCascade(String cascadeFileName);

    public native int detect(long cascadeClassifier_face,
                             long cascadeClassifier_eye, long matAddrInput, long matAddrResult);

    public native double detect4(long cascadeClassifier_face, long cascadeClassifier_eye, long matAddrInput, long matAddrResult);

    public native double detect5(long cascadeClassifier_face, long cascadeClassifier_eye, long matAddrInput, long matAddrResult);

    public native double faceWidth(long cascadeClassifier_face, long cascadeClassifier_eye, long matAddrInput, long matAddrResult);

    public native double faceHeight(long cascadeClassifier_face, long cascadeClassifier_eye, long matAddrInput, long matAddrResult);

    public long cascadeClassifier_face = 0;
    public long cascadeClassifier_eye = 0;

    private final Semaphore writeLock = new Semaphore(2); //세마포어로 동시 실행 가능한 Thread 제어

    public void getWriteLock() throws InterruptedException {
        writeLock.acquire();
    }

    public void releaseWriteLock() {
        writeLock.release();
    }

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    private void copyFile(String filename) {  //Harr cascade 트레이닝 데이터 읽어오기 위해서 안드로이드폰의 내부 저장소로 옮기는 작업
        String baseDir = Environment.getExternalStorageDirectory().getPath();
        String pathDir = baseDir + File.separator + filename;

        AssetManager assetManager = this.getAssets();

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Log.d(TAG, "copyFile :: 다음 경로로 파일복사 " + pathDir);
            inputStream = assetManager.open(filename);
            outputStream = new FileOutputStream(pathDir);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            Log.d(TAG, "copyFile :: 파일 복사 중 예외 발생 " + e.toString());
        }
    }

    //Harr cascade 트레이닝 데이터 읽어오기 위해서 안드로이드폰의 내부 저장소로 옮기는 작업
    private void read_cascade_file()
    {
        copyFile("haarcascade_frontalface_alt.xml");
        copyFile("haarcascade_eye_tree_eyeglasses.xml");

        Log.d(TAG, "read_cascade_file:");


        //loadCascade => 내부 저장소로부터 Harr cascade 트레이닝 데이터를 읽어와 CascadeClassifier 객체를 생성후 자바로 넘겨준다
        cascadeClassifier_face = loadCascade("haarcascade_frontalface_alt.xml");
        Log.d(TAG, "read_cascade_file:");

        cascadeClassifier_eye = loadCascade("haarcascade_eye_tree_eyeglasses.xml");
    }
    /**//////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//activity 화면 초기화

        mTextStatus=(TextView)findViewById(R.id.textStatus);
        //mCapturePreview=new CapturePreview(this,(SurfaceView)findViewById(R.id.surfPreview));

        if(mServerThread==null) {//서버시작
            mServerThread = new ServerThread(this,mMainHandler);
            mServerThread.start();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //퍼미션 상태 확인
            if (!hasPermissions(PERMISSIONS)) {

                //퍼미션 허가 안되어있다면 사용자에게 요청
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }else read_cascade_file(); //추가
        }else read_cascade_file(); //추가

        /**///////////////
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.surfPreview);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        /**//////////////////////////

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
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    /**////////////////////////////////////////////////
    //여기서부턴 퍼미션 관련 메소드
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    //String[] PERMISSIONS  = {"android.permission.CAMERA"};
    String[] PERMISSIONS = {"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    private boolean hasPermissions(String[] permissions) { //Permission 관련 코드
        int result;

        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions) {

            result = ContextCompat.checkSelfPermission(this, perms);

            if (result == PackageManager.PERMISSION_DENIED) {
                //허가 안된 퍼미션 발견
                return false;
            }
        }
        //모든 퍼미션이 허가되었음
        return true;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onCameraViewStarted(int width, int height) {//Override 해야하는 메서드

    }

    @Override
    public void onCameraViewStopped() {//Override 해야하는 메서드

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {//매 프레임마다 동작
        try {
            getWriteLock();

            matInput = inputFrame.rgba();

            if (matResult == null)

                matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());


            Core.flip(matInput, matInput, 1);   //카메라화면 180도 뒤집기(전면, 후면 카메라 변경시 확인)

            int ret = detect(cascadeClassifier_face, cascadeClassifier_eye, matInput.getNativeObjAddr(),  //검출된 얼굴 개수 대입
                    matResult.getNativeObjAddr());

            double real_facesize_x = detect4(cascadeClassifier_face, cascadeClassifier_eye, matInput.getNativeObjAddr(),
                    matResult.getNativeObjAddr());

            double real_facesize_y = detect5(cascadeClassifier_face, cascadeClassifier_eye, matInput.getNativeObjAddr(),
                    matResult.getNativeObjAddr());

            // 얼굴이 2개 이상 검출될 경우 큰 사이즈의 값을 가져와 주는 좌표 값
            double facewidth = faceWidth(cascadeClassifier_face, cascadeClassifier_eye, matInput.getNativeObjAddr(),
                    matResult.getNativeObjAddr());

            double faceheight = faceHeight(cascadeClassifier_face, cascadeClassifier_eye, matInput.getNativeObjAddr(),
                    matResult.getNativeObjAddr());

            // 얼굴이 2개 이상 검출될 경우 큰 사이즈로 검출된 얼굴을 인식 및 자동 추적
            if (ret >= 2 && autoDetectionOnOff) {
                Log.d("taehyung", "face " + ret + " found");
                Log.d("taehyung", "X좌표" + facewidth);
                Log.d("taehyung", "Y좌표" + faceheight);

                facestack++;
                if (facestack % 100 == 50) {
                    if (SendThread.mHandler != null) {  //SendThread에 핸들러 존재시 실행
                        Message msg = Message.obtain();
                        msg.what = SendThread.CMD_SEND_MESSAGE;
                        msg.obj = "얼굴이 검출되었습니다";
                        SendThread.mHandler.sendMessage(msg);  //작성한 메시지를 SendThread로 핸들러를 이용해 보냄
                    }
                }

                if (faceheight >= 320) {
                    Message msg = Message.obtain();
                    msg.what = MainActivity.CMD_BACKBUTTON;
                    mThreadConnectedBluetooth.write("B");
                    ServerThread.mMainHandler.sendMessage(msg);
                    Log.d("자동추적테스트", "아두이노 동작 확인 하");
                } else if (faceheight <= 160) {
                    Message msg = Message.obtain();
                    msg.what = MainActivity.CMD_FORWARDBUTTON;
                    mThreadConnectedBluetooth.write("F");
                    ServerThread.mMainHandler.sendMessage(msg);
                    Log.d("자동추적테스트", "아두이노 동작 확인 상");
                }else{
                    Message msg = Message.obtain();
                    msg.what = MainActivity.CMD_STOP;
                    mThreadConnectedBluetooth.write("S");
                    ServerThread.mMainHandler.sendMessage(msg);
                    Log.d("자동추적테스트", "아두이노 동작 확인 정지");
                }

                if (facewidth >= 430) {
                    Message msg = Message.obtain();
                    msg.what = MainActivity.CMD_RIGHTBUTTON;
                    mThreadConnectedBluetooth.write("R");
                    ServerThread.mMainHandler.sendMessage(msg);
                    Log.d("자동추적테스트", "아두이노 동작 확인 좌");
                } else if (facewidth <= 210) {
                    Message msg = Message.obtain();
                    msg.what = MainActivity.CMD_LEFTBUTTON;
                    mThreadConnectedBluetooth.write("L");
                    ServerThread.mMainHandler.sendMessage(msg);
                    Log.d("자동추적테스트", "아두이노 동작 확인 우");
                } else{
                    Message msg = Message.obtain();
                    msg.what = MainActivity.CMD_STOP;
                    mThreadConnectedBluetooth.write("S");
                    ServerThread.mMainHandler.sendMessage(msg);
                    Log.d("자동추적테스트", "아두이노 동작 확인 정지");
                }

            }
            else if (ret == 1 && autoDetectionOnOff) {   //얼굴 1개
                Log.d("TaeHyeong1", "face " + ret + " found");
                Log.d("Test2", "X좌표" + real_facesize_x);
                Log.d("Test2", "Y좌표" + real_facesize_y);

                facestack++;
                if (facestack % 100 == 50) {
                    if (SendThread.mHandler != null) {  //SendThread에 핸들러 존재시 실행
                        Message msg = Message.obtain();
                        msg.what = SendThread.CMD_SEND_MESSAGE;
                        msg.obj = "얼굴이 검출되었습니다";
                        SendThread.mHandler.sendMessage(msg);  //작성한 메시지를 SendThread로 핸들러를 이용해 보냄
                    }
                }

                if (real_facesize_y >= 320) {
                    Message msg = Message.obtain();
                    msg.what = MainActivity.CMD_BACKBUTTON;
                    mThreadConnectedBluetooth.write("B");
                    ServerThread.mMainHandler.sendMessage(msg);
                    Log.d("자동추적테스트", "아두이노 동작 확인 하");
                } else if (real_facesize_y <= 160) {
                    Message msg = Message.obtain();
                    msg.what = MainActivity.CMD_FORWARDBUTTON;
                    mThreadConnectedBluetooth.write("F");
                    ServerThread.mMainHandler.sendMessage(msg);
                    Log.d("자동추적테스트", "아두이노 동작 확인 상");
                }else{
                    Message msg = Message.obtain();
                    msg.what = MainActivity.CMD_STOP;
                    mThreadConnectedBluetooth.write("S");
                    ServerThread.mMainHandler.sendMessage(msg);
                    Log.d("자동추적테스트", "아두이노 동작 확인 정지");
                }

                if (real_facesize_x >= 430) {
                    Message msg = Message.obtain();
                    msg.what = MainActivity.CMD_RIGHTBUTTON;
                    mThreadConnectedBluetooth.write("R");
                    ServerThread.mMainHandler.sendMessage(msg);
                    Log.d("자동추적테스트", "아두이노 동작 확인 좌");
                } else if (real_facesize_x <= 210) {
                    Message msg = Message.obtain();
                    msg.what = MainActivity.CMD_LEFTBUTTON;
                    mThreadConnectedBluetooth.write("L");
                    ServerThread.mMainHandler.sendMessage(msg);
                    Log.d("자동추적테스트", "아두이노 동작 확인 우");
                } else{
                    Message msg = Message.obtain();
                    msg.what = MainActivity.CMD_STOP;
                    mThreadConnectedBluetooth.write("S");
                    ServerThread.mMainHandler.sendMessage(msg);
                    Log.d("자동추적테스트", "아두이노 동작 확인 정지");
                }

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        releaseWriteLock();

        Bitmap bitmap = Bitmap.createBitmap(matResult.cols(), matResult.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(matResult, bitmap);       // Mat을 Bitmap으로 변환

        //이부분 고쳐야함///////////////
        //sendBitmapThroughNetwork(bitmap);            // 네트워크로 전송 <- 기존 VideoServer에 있던 함수  핸들러로 처리함
        //sendBitmapToViewer(bitmap);                  // ImageView에 보여줌   핸들러로 처리함
        ///////////////////////
        return matResult; //최종 결과를 안드로이드폰의 화면에 보여지도록 결과 Mat 객체를 리턴
    }

    /**//////////////////////////////////////////////
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