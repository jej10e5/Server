package com.example.client;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.DataOutputStream;
import java.io.OutputStream;

public class SendThread extends Thread {

    public static final int CMD_GOBUTTON = 4;
    public static final int CMD_BACKBUTTON =5;
    public static final int CMD_RIGHTBUTTON =6;
    public static final int CMD_LEFTBUTTON=7;
    public static final int CMD_STOP=8;
    public static final int CMD_UPBUTTON=9;
    public static final int CMD_DOWNBUTTON=10;
    public static final int CMD_STARTCHAT=11;
    public static final int CMD_STOPCHAT=12;

    public static final int HEADER_GO = 0x11111111;
    public static final int HEADER_BACK = 0X22222222;
    public static final int HEADER_RIGHT = 0x33333333;
    public static final int HEADER_LEFT = 0X44444444;
    public static  final int HEADER_STOP = 0x55555555;
    public static  final int SERVO_UP = 0x66666666;
    public static  final int SERVO_DOWN = 0x77777777;
    public static final int HEADER_STARTCHAT = 0x88888888;
    public static final int HEADER_STOPCHAT=0x99999999;

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
                        case CMD_UPBUTTON:
                            String up = (String) msg.obj;
                            mDataOutputStream.writeInt(SERVO_UP);
                            mDataOutputStream.writeInt(up.length());
                            mDataOutputStream.writeUTF(up);
                            mDataOutputStream.flush();
                            break;
                        case CMD_DOWNBUTTON:
                            String down = (String) msg.obj;
                            mDataOutputStream.writeInt(SERVO_DOWN);
                            mDataOutputStream.writeInt(down.length());
                            mDataOutputStream.writeUTF(down);
                            mDataOutputStream.flush();
                            break;
                        case CMD_STOP:
                            String stop = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_STOP);
                            mDataOutputStream.writeInt(stop.length());
                            mDataOutputStream.writeUTF(stop);
                            mDataOutputStream.flush();
                            break;
                        case CMD_STARTCHAT:
                            String startchat=(String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_STARTCHAT);
                            mDataOutputStream.writeInt(startchat.length());
                            mDataOutputStream.writeUTF(startchat);
                            mDataOutputStream.flush();
                            break;
                        case CMD_STOPCHAT:
                            String stopchat=(String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_STOPCHAT);
                            mDataOutputStream.writeInt(stopchat.length());
                            mDataOutputStream.writeUTF(stopchat);
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
