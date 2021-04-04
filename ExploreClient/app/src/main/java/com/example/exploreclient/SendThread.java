package com.example.exploreclient;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SendThread extends Thread {

    public static final int CMD_GOBUTTON = 4;
    public static final int CMD_BACKBUTTON = 5;
    public static final int CMD_RIGHTBUTTON = 6;
    public static final int CMD_LEFTBUTTON = 7;
    public static final int CMD_STOP = 8;

    public static final int HEADER_GO = 0x11111111;
    public static final int HEADER_BACK = 0X22222222;
    public static final int HEADER_RIGHT = 0x33333333;
    public static final int HEADER_LEFT = 0X44444444;
    public static final int HEADER_STOP = 0x55555555;

    public static Handler mHandler;
    private DataOutputStream mDataOutputStream;

    public SendThread(OutputStream outputStream) {
        mDataOutputStream = new DataOutputStream(outputStream);
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                byte[] byteArray;
                try {
                    switch (msg.what) {
                        case CMD_GOBUTTON:
                            String go = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_GO);
                            mDataOutputStream.writeInt(go.length());
                            mDataOutputStream.writeUTF(go);
                            mDataOutputStream.flush();
                            break;
                        case CMD_BACKBUTTON:
                            String back = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_BACK);
                            mDataOutputStream.writeInt(back.length());
                            mDataOutputStream.writeUTF(back);
                            mDataOutputStream.flush();
                            break;
                        case CMD_RIGHTBUTTON:
                            String right = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_RIGHT);
                            mDataOutputStream.writeInt(right.length());
                            mDataOutputStream.writeUTF(right);
                            mDataOutputStream.flush();
                            break;
                        case CMD_LEFTBUTTON:
                            String left = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_LEFT);
                            mDataOutputStream.writeInt(left.length());
                            mDataOutputStream.writeUTF(left);
                            mDataOutputStream.flush();
                            break;

                        case CMD_STOP:
                            String stop = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_STOP);
                            mDataOutputStream.writeInt(stop.length());
                            mDataOutputStream.writeUTF(stop);
                            mDataOutputStream.flush();
                            break;
                    }
                } catch (IOException e) {
                    getLooper().quit();
                }
            }
        };
            Looper.loop();
    }
}