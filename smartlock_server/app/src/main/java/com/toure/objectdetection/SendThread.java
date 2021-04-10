package com.toure.objectdetection;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;


public class SendThread extends Thread {

    public static final int CMD_SEND_BITMAP = 1;
    public static final int CMD_CALL = 2;
    public static final int CMD_ENDCALL = 3;
    public static final int CMD_SEND_BAT = 4;
    public static final int CMD_SEND_WARMING = 5;

    private static final int HEADER_BITMAP = 0x11111111;
    private static final int HEADER_CALL = 0x22222222;
    private static final int HEADER_ENDCALL = 0x33333333;
    private static final int HEADER_BAT = 0x44444444;
    private static final int HEADER_WARMING = 0x55555555;

    private DataOutputStream mDataOutputStream;
    private String ip;
    public static Handler mHandler;

    public SendThread(OutputStream os, String s) {
        mDataOutputStream = new DataOutputStream(os);
        ip = s;
    }

    @Override
    public void run() {
        Looper.prepare();

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                byte[] byteArray;
                try {
                    switch (msg.what){
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
                        case CMD_CALL:
                            String s = ip;
                            mDataOutputStream.writeInt(HEADER_CALL);
                            mDataOutputStream.writeInt(s.length());
                            mDataOutputStream.writeUTF(s);
                            mDataOutputStream.flush();
                            break;
                        case CMD_ENDCALL:
                            String s0 = ip;
                            mDataOutputStream.writeInt(HEADER_ENDCALL);
                            mDataOutputStream.writeInt(s0.length());
                            mDataOutputStream.writeUTF(s0);
                            mDataOutputStream.flush();
                            break;
                        case CMD_SEND_BAT:
                            String s3 = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_BAT);
                            mDataOutputStream.writeInt(s3.length());
                            mDataOutputStream.writeUTF(s3);
                            mDataOutputStream.flush();
                            break;
                        case CMD_SEND_WARMING:
                            String s4 = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_WARMING);
                            mDataOutputStream.writeInt(s4.length());
                            mDataOutputStream.writeUTF(s4);
                            mDataOutputStream.flush();
                            break;
                    }
                } catch (Exception e){
                    getLooper().quit();
                }
            }
        };
        Looper.loop();
    }
}