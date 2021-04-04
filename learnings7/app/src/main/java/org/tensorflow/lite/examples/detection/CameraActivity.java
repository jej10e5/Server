/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.detection;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.nio.ByteBuffer;
import org.tensorflow.lite.examples.detection.env.ImageUtils;
import org.tensorflow.lite.examples.detection.env.Logger;

//main
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public abstract class CameraActivity extends AppCompatActivity
    implements OnImageAvailableListener,
        Camera.PreviewCallback,
        CompoundButton.OnCheckedChangeListener,
        View.OnClickListener {
  private static final Logger LOGGER = new Logger();
  private static final int PERMISSIONS_REQUEST = 1;
  private static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
  protected int previewWidth = 0;
  protected int previewHeight = 0;
  private boolean debug = false;
  private Handler handler;
  private HandlerThread handlerThread;
  private boolean useCamera2API;
  private boolean isProcessingFrame = false;
  private byte[][] yuvBytes = new byte[3][];
  private int[] rgbBytes = null;
  private int yRowStride;
  private Runnable postInferenceCallback;
  private Runnable imageConverter;
  private LinearLayout bottomSheetLayout;
  private LinearLayout gestureLayout;
  private BottomSheetBehavior<LinearLayout> sheetBehavior;
  protected TextView frameValueTextView, cropValueTextView, inferenceTimeTextView;
  protected ImageView bottomSheetArrowImageView;
  private ImageView plusImageView, minusImageView;
  private SwitchCompat apiSwitchCompat;
  private TextView threadsTextView;

  //main
  public static final int CMD_APPEND_TEXT = 0;
  private TextView mTextStatus;
  private CapturePreview mCapturePreview;
  private DeviceLocation mDeviceLocation;
  private ServerThread mServerThread;

  Button mBtnbt;
  Button mBtnBluetoothOn;
  Button mBtnBluetoothOff;
  Button mBtnconnect;
  Button mBtngo;
  BluetoothAdapter mBluetoothAdapter;
  Set<BluetoothDevice> mPairedDevices;
  List<String> mListPairedDevices;
  Handler mBluetoothHandler;
  ConnectedBluetoothThread mThreadConnectedBluetooth;
  BluetoothDevice mBluetoothDevice;
  BluetoothSocket mBluetoothSocket;
  TextView mTvReceiveData;
  ChatSend mic;
  Thread thread;
  public boolean isthread = false;


  final static int BT_REQUEST_ENABLE = 1;
  final static int BT_MESSAGE_READ = 2;
  final static int BT_CONNECTING_STATUS = 3;
  public static final int CMD_GOBUTTON = 4;
  public static final int CMD_BACKBUTTON = 5;
  public static final int CMD_RIGHTBUTTON = 6;
  public static final int CMD_LEFTBUTTON = 7;
  public static final int CMD_STOP = 8;
  public static final int CMD_UPBUTTON = 9;
  public static final int CMD_DOWNBUTTON = 10;
  public static final int CMD_CHATSTART = 11;
  public static final int CMD_CHATSTOP = 12;
  final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
  private Object CapturePreview;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    LOGGER.d("onCreate " + this);
    super.onCreate(null);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    setContentView(R.layout.tfe_od_activity_camera);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    if (hasPermission()) {
      setFragment();
    } else {
      requestPermission();
    }

    threadsTextView = findViewById(R.id.threads);
    plusImageView = findViewById(R.id.plus);
    minusImageView = findViewById(R.id.minus);
    apiSwitchCompat = findViewById(R.id.api_info_switch);
    bottomSheetLayout = findViewById(R.id.bottom_sheet_layout);
    gestureLayout = findViewById(R.id.gesture_layout);
    sheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
    bottomSheetArrowImageView = findViewById(R.id.bottom_sheet_arrow);

    ViewTreeObserver vto = gestureLayout.getViewTreeObserver();
    vto.addOnGlobalLayoutListener(
            new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override
              public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                  gestureLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                  gestureLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                //                int width = bottomSheetLayout.getMeasuredWidth();
                int height = gestureLayout.getMeasuredHeight();

                sheetBehavior.setPeekHeight(height);
              }
            });
    sheetBehavior.setHideable(false);

    sheetBehavior.setBottomSheetCallback(
            new BottomSheetBehavior.BottomSheetCallback() {
              @Override
              public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                  case BottomSheetBehavior.STATE_HIDDEN:
                    break;
                  case BottomSheetBehavior.STATE_EXPANDED: {
                    bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_down);
                  }
                  break;
                  case BottomSheetBehavior.STATE_COLLAPSED: {
                    bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                  }
                  break;
                  case BottomSheetBehavior.STATE_DRAGGING:
                    break;
                  case BottomSheetBehavior.STATE_SETTLING:
                    bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up);
                    break;
                }
              }

              @Override
              public void onSlide(@NonNull View bottomSheet, float slideOffset) {
              }
            });

    frameValueTextView = findViewById(R.id.frame_info);
    cropValueTextView = findViewById(R.id.crop_info);
    inferenceTimeTextView = findViewById(R.id.inference_info);

    apiSwitchCompat.setOnCheckedChangeListener(this);

    plusImageView.setOnClickListener(this);
    minusImageView.setOnClickListener(this);

    //main
    mTextStatus = (TextView) findViewById(R.id.textStatus);
    /*mCapturePreview = new CapturePreview(this,
            (SurfaceView) findViewById(R.id.surfPreview),
            (ImageView) findViewById(R.id.imageFrame));*/
    mDeviceLocation = new DeviceLocation(this);

    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    mBtnBluetoothOn = (Button) findViewById(R.id.btnBluetoothOn);
    mBtnBluetoothOff = (Button) findViewById(R.id.btnBluetoothOff);
    mBtnconnect = (Button) findViewById(R.id.btnconnect);
    mBtngo = (Button) findViewById(R.id.btngo);
    mBtnbt = (Button) findViewById(R.id.btnBT);

    if (mServerThread == null) { // 서버 시작
      mServerThread = new ServerThread(this, mMainHandler);
      mServerThread.start();
    }




    mBtnBluetoothOn.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View view) {
        bluetoothOn();

        Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float) scale;
        String value = String.valueOf((int) (batteryPct * 100));

        if (SendThread.mHandler != null) {
          Message msg = Message.obtain();
          msg.what = SendThread.CMD_SEND_BAT;
          msg.obj = value;
          SendThread.mHandler.sendMessage(msg);
        }
      }


    });

    mBtnBluetoothOff.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View view) {
        bluetoothOff();
      }
    });

    mBtnconnect.setOnClickListener(new Button.OnClickListener() {
      @Override
      public void onClick(View view) {
        listPairedDevices();
      }
    });


    mBluetoothHandler = new Handler() {
      public void handleMessage(android.os.Message msg) {
        if (msg.what == BT_MESSAGE_READ) {
          String readMessage = null;
          try {
            readMessage = new String((byte[]) msg.obj, "UTF-8");
          } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
          }
          mTvReceiveData.setText(readMessage);
        }
      }
    };

    thread = new Thread() {
      public void run() {
        while (!isthread) {
          try {
            sleep(300000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          BThandler.sendEmptyMessage(0);
        }
      }

    };
    thread.start();

  }

  protected int[] getRgbBytes() {
    imageConverter.run();
    return rgbBytes;
  }

  protected int getLuminanceStride() {
    return yRowStride;
  }

  protected byte[] getLuminance() {
    return yuvBytes[0];
  }

  /**
   * Callback for android.hardware.Camera API
   */
  @Override
  public void onPreviewFrame(final byte[] bytes, final Camera camera) {
    if (isProcessingFrame) {
      LOGGER.w("Dropping frame!");
      return;
    }

    try {
      // Initialize the storage bitmaps once when the resolution is known.
      if (rgbBytes == null) {
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        previewHeight = previewSize.height;
        previewWidth = previewSize.width;
        rgbBytes = new int[previewWidth * previewHeight];
        onPreviewSizeChosen(new Size(previewSize.width, previewSize.height), 90);
      }
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      return;
    }

    isProcessingFrame = true;
    yuvBytes[0] = bytes;
    yRowStride = previewWidth;

    imageConverter =
            new Runnable() {
              @Override
              public void run() {
                ImageUtils.convertYUV420SPToARGB8888(bytes, previewWidth, previewHeight, rgbBytes);
              }
            };

    postInferenceCallback =
            new Runnable() {
              @Override
              public void run() {
                camera.addCallbackBuffer(bytes);
                isProcessingFrame = false;
              }
            };
    processImage();
  }

  /**
   * Callback for Camera2 API
   */
  @Override
  public void onImageAvailable(final ImageReader reader) {
    // We need wait until we have some size from onPreviewSizeChosen
    if (previewWidth == 0 || previewHeight == 0) {
      return;
    }
    if (rgbBytes == null) {
      rgbBytes = new int[previewWidth * previewHeight];
    }
    try {
      final Image image = reader.acquireLatestImage();

      if (image == null) {
        return;
      }

      if (isProcessingFrame) {
        image.close();
        return;
      }
      isProcessingFrame = true;
      Trace.beginSection("imageAvailable");
      final Plane[] planes = image.getPlanes();
      fillBytes(planes, yuvBytes);
      yRowStride = planes[0].getRowStride();
      final int uvRowStride = planes[1].getRowStride();
      final int uvPixelStride = planes[1].getPixelStride();

      imageConverter =
              new Runnable() {
                @Override
                public void run() {
                  ImageUtils.convertYUV420ToARGB8888(
                          yuvBytes[0],
                          yuvBytes[1],
                          yuvBytes[2],
                          previewWidth,
                          previewHeight,
                          yRowStride,
                          uvRowStride,
                          uvPixelStride,
                          rgbBytes);
                }
              };

      postInferenceCallback =
              new Runnable() {
                @Override
                public void run() {
                  image.close();
                  isProcessingFrame = false;
                }
              };

      processImage();
    } catch (final Exception e) {
      LOGGER.e(e, "Exception!");
      Trace.endSection();
      return;
    }
    Trace.endSection();
  }

  @Override
  public synchronized void onStart() {
    LOGGER.d("onStart " + this);
    super.onStart();
  }

  @Override
  public synchronized void onResume() {
    LOGGER.d("onResume " + this);
    super.onResume();

    handlerThread = new HandlerThread("inference");
    handlerThread.start();
    handler = new Handler(handlerThread.getLooper());
  }

  @Override
  public synchronized void onPause() {
    LOGGER.d("onPause " + this);

    handlerThread.quitSafely();
    try {
      handlerThread.join();
      handlerThread = null;
      handler = null;
    } catch (final InterruptedException e) {
      LOGGER.e(e, "Exception!");
    }

    super.onPause();
  }

  @Override
  public synchronized void onStop() {
    LOGGER.d("onStop " + this);
    super.onStop();
  }

  @Override
  public synchronized void onDestroy() {
    LOGGER.d("onDestroy " + this);
    super.onDestroy();
  }

  protected synchronized void runInBackground(final Runnable r) {
    if (handler != null) {
      handler.post(r);
    }
  }

  @Override
  public void onRequestPermissionsResult(
          final int requestCode, final String[] permissions, final int[] grantResults) {
    if (requestCode == PERMISSIONS_REQUEST) {
      if (allPermissionsGranted(grantResults)) {
        setFragment();
      } else {
        requestPermission();
      }
    }
  }

  private static boolean allPermissionsGranted(final int[] grantResults) {
    for (int result : grantResults) {
      if (result != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  private boolean hasPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return checkSelfPermission(PERMISSION_CAMERA) == PackageManager.PERMISSION_GRANTED;
    } else {
      return true;
    }
  }

  private void requestPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (shouldShowRequestPermissionRationale(PERMISSION_CAMERA)) {
        Toast.makeText(
                CameraActivity.this,
                "Camera permission is required for this demo",
                Toast.LENGTH_LONG)
                .show();
      }
      requestPermissions(new String[]{PERMISSION_CAMERA}, PERMISSIONS_REQUEST);
    }
  }

  // Returns true if the device supports the required hardware level, or better.
  private boolean isHardwareLevelSupported(
          CameraCharacteristics characteristics, int requiredLevel) {
    int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
    if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
      return requiredLevel == deviceLevel;
    }
    // deviceLevel is not LEGACY, can use numerical sort
    return requiredLevel <= deviceLevel;
  }

  private String chooseCamera() {
    final CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    try {
      for (final String cameraId : manager.getCameraIdList()) {
        final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        // We don't use a front facing camera in this sample.
        final Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
          continue;
        }

        final StreamConfigurationMap map =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

        if (map == null) {
          continue;
        }

        // Fallback to camera1 API for internal cameras that don't have full support.
        // This should help with legacy situations where using the camera2 API causes
        // distorted or otherwise broken previews.
        useCamera2API =
                (facing == CameraCharacteristics.LENS_FACING_EXTERNAL)
                        || isHardwareLevelSupported(
                        characteristics, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL);
        LOGGER.i("Camera API lv2?: %s", useCamera2API);
        return cameraId;
      }
    } catch (CameraAccessException e) {
      LOGGER.e(e, "Not allowed to access camera");
    }

    return null;
  }

  protected void setFragment() {
    String cameraId = chooseCamera();

    Fragment fragment;
    if (useCamera2API) {
      CameraConnectionFragment camera2Fragment =
              CameraConnectionFragment.newInstance(
                      new CameraConnectionFragment.ConnectionCallback() {
                        @Override
                        public void onPreviewSizeChosen(final Size size, final int rotation) {
                          previewHeight = size.getHeight();
                          previewWidth = size.getWidth();
                          CameraActivity.this.onPreviewSizeChosen(size, rotation);
                        }
                      },
                      this,
                      getLayoutId(),
                      getDesiredPreviewFrameSize());

      camera2Fragment.setCamera(cameraId);
      fragment = camera2Fragment;
    } else {
      fragment =
              new LegacyCameraConnectionFragment(this, getLayoutId(), getDesiredPreviewFrameSize());
    }

    getFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
  }


  protected void fillBytes(final Plane[] planes, final byte[][] yuvBytes) {
    // Because of the variable row stride it's not possible to know in
    // advance the actual necessary dimensions of the yuv planes.
    for (int i = 0; i < planes.length; ++i) {
      final ByteBuffer buffer = planes[i].getBuffer();
      if (yuvBytes[i] == null) {
        LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity());
        yuvBytes[i] = new byte[buffer.capacity()];
      }
      buffer.get(yuvBytes[i]);
    }
  }

  public boolean isDebug() {
    return debug;
  }

  protected void readyForNextImage() {
    if (postInferenceCallback != null) {
      postInferenceCallback.run();
    }
  }

  protected int getScreenOrientation() {
    switch (getWindowManager().getDefaultDisplay().getRotation()) {
      case Surface.ROTATION_270:
        return 270;
      case Surface.ROTATION_180:
        return 180;
      case Surface.ROTATION_90:
        return 90;
      default:
        return 0;
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    setUseNNAPI(isChecked);
    if (isChecked) apiSwitchCompat.setText("NNAPI");
    else apiSwitchCompat.setText("TFLITE");
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.plus) {
      String threads = threadsTextView.getText().toString().trim();
      int numThreads = Integer.parseInt(threads);
      if (numThreads >= 9) return;
      numThreads++;
      threadsTextView.setText(String.valueOf(numThreads));
      setNumThreads(numThreads);
    } else if (v.getId() == R.id.minus) {
      String threads = threadsTextView.getText().toString().trim();
      int numThreads = Integer.parseInt(threads);
      if (numThreads == 1) {
        return;
      }
      numThreads--;
      threadsTextView.setText(String.valueOf(numThreads));
      setNumThreads(numThreads);
    }
  }

  protected void showFrameInfo(String frameInfo) {
    frameValueTextView.setText(frameInfo);
  }

  protected void showCropInfo(String cropInfo) {
    cropValueTextView.setText(cropInfo);
  }

  protected void showInference(String inferenceTime) {
    inferenceTimeTextView.setText(inferenceTime);
  }

  protected abstract void processImage();

  protected abstract void onPreviewSizeChosen(final Size size, final int rotation);

  protected abstract int getLayoutId();

  protected abstract Size getDesiredPreviewFrameSize();

  protected abstract void setNumThreads(int numThreads);

  protected abstract void setUseNNAPI(boolean isChecked);

  //main
  public void mOnClick(View v) {
    switch (v.getId()) {
      case R.id.btnQuit:
        finish();
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
        case CMD_GOBUTTON:
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
        case CMD_UPBUTTON:
          mThreadConnectedBluetooth.write("U");
          break;
        case CMD_DOWNBUTTON:
          mThreadConnectedBluetooth.write("D");
          break;
        case CMD_CHATSTART:
          if (mic != null) {
            return;
          }
          mic = new ChatSend(mServerThread.ip2);
          mic.start();
          break;
        case CMD_CHATSTOP:
          if (mic != null) {
            mic.interrupt();
            mic = null;
          }
          break;


      }
    }
  };

  void bluetoothOn() {
    if (mBluetoothAdapter == null) {
      Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
    } else {
      if (mBluetoothAdapter.isEnabled()) {
        Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show();
        //mTvBluetoothStatus.setText("활성화");
      } else {
        //Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_LONG).show();
        Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
      }
    }
  }

  void bluetoothOff() {
    if (mBluetoothAdapter == null) {
      Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
    } else {
      if (mBluetoothAdapter.isEnabled()) {
        mBluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되었습니다.", Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
      }
    }

  }

  void chargerGo() {
    mThreadConnectedBluetooth.write("F");
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        mThreadConnectedBluetooth.write("S");
      }
    }, 1500);
  }









  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case BT_REQUEST_ENABLE:
        if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
          Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_LONG).show();

        } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
          Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();

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
        Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
      }
    } else {
      Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
    }
  }

  void connectSelectedDevice(String selectedDeviceName) {
    for (BluetoothDevice tempDevice : mPairedDevices) {
      if (selectedDeviceName.equals(tempDevice.getName())) {
        mBluetoothDevice = tempDevice;
        break;
      }
    }
    try {
      mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
      mBluetoothSocket.connect();
      mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
      mThreadConnectedBluetooth.start();
      mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
    } catch (IOException e) {
      Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
    }
  }

  private class ConnectedBluetoothThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public ConnectedBluetoothThread(BluetoothSocket socket) {
      mmSocket = socket;
      InputStream tmpIn = null;
      OutputStream tmpOut = null;

      try {
        tmpIn = socket.getInputStream();
        tmpOut = socket.getOutputStream();
      } catch (IOException e) {
        Toast.makeText(getApplicationContext(), "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
      }

      mmInStream = tmpIn;
      mmOutStream = tmpOut;
    }

    public void run() {
      byte[] buffer = new byte[1024];
      int bytes;

      while (true) {
        try {
          bytes = mmInStream.available();
          if (bytes != 0) {
            SystemClock.sleep(100);
            bytes = mmInStream.available();
            bytes = mmInStream.read(buffer, 0, bytes);
            mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
          }
        } catch (IOException e) {
          break;
        }
      }
    }

    public void write(String str) {
      byte[] bytes = str.getBytes();
      try {
        mmOutStream.write(bytes);
      } catch (IOException e) {
        Toast.makeText(getApplicationContext(), "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
      }
    }

    public void cancel() {
      try {
        mmSocket.close();
      } catch (IOException e) {
        Toast.makeText(getApplicationContext(), "소켓 해제 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
      }
    }
  }

  private Handler BThandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      Intent batteryStatus = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

      int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
      int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

      float batteryPct = level / (float)scale;
      String value = String.valueOf((int)(batteryPct*100));
      int values = (int)(batteryPct*100);

      if (SendThread.mHandler != null ) {

        if (values <= 30) {

            Message msg1 = Message.obtain();
            msg1.what = SendThread.CMD_SEND_BAT;
            msg1.obj = value;
            SendThread.mHandler.sendMessage(msg1);

            mThreadConnectedBluetooth.write("R");
            new Handler().postDelayed(new Runnable() {
              @Override
              public void run() {
                mThreadConnectedBluetooth.write("S");

              }
            }, 1500);

          }



          // isthread=true;

        }

      }




  };

}
