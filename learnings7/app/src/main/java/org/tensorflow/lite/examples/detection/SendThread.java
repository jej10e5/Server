package org.tensorflow.lite.examples.detection;

import android.graphics.Bitmap;
import android.os.Looper;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import android.os.Handler;

public class SendThread extends Thread {

    public static final int CMD_SEND_BITMAP = 1;
    public static final int CMD_SEND_LOCATION = 2;
    public static final int CMD_SEND_BAT = 3;

    private static final int HEADER_BITMAP = 0X11111111;
    private static final int HEADER_LOCATION = 0X22222222;
    private static final int HEADER_BAT = 0x33333333;


    private DataOutputStream mDataOutputStream;
    public static Handler mHandler;

    public SendThread(OutputStream os) {
        mDataOutputStream = new DataOutputStream(os);
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                byte[] byteArray;
                try {
                    switch (msg.what) {
                        case CMD_SEND_BITMAP:
                            Bitmap bitmap = (Bitmap) msg.obj;
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream);
                            byteArray = stream.toByteArray();

                            mDataOutputStream.writeInt(HEADER_BITMAP);
                            mDataOutputStream.writeInt(byteArray.length);
                            mDataOutputStream.write(byteArray);
                            mDataOutputStream.flush();
                            break;
                        case CMD_SEND_LOCATION:
                            //DeviceLocation loc = (DeviceLocation) msg.obj;
                            mDataOutputStream.writeInt(HEADER_LOCATION);
                            mDataOutputStream.writeInt(8 * 2);
                            //mDataOutputStream.writeInt((int) loc.mLatitude);
                            //mDataOutputStream.writeInt((int) loc.mLongitude);
                            mDataOutputStream.flush();
                            break;

                        case CMD_SEND_BAT:
                            String s = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_BAT);
                            mDataOutputStream.writeInt(s.length());
                            mDataOutputStream.writeUTF(s);
                            mDataOutputStream.flush();
                            break;

                    }
                } catch (Exception e) {
                    getLooper().quit();
                }
            }
        };
        Looper.loop();
    }
}