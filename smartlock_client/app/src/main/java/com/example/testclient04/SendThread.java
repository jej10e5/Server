package com.example.testclient04;

import android.os.Looper;
import android.os.Message;
import android.os.Handler;
import java.io.DataOutputStream;
import java.io.OutputStream;

public class SendThread extends Thread {
    public static final int CMD_SEND_CALL = 1;
    public static final int CMD_SEND_BOTTON1 = 2;
    public static final int CMD_SEND_BOTTON2 = 3;
    public static final int CMD_SEND_BOTTON3 = 4;
    public static final int CMD_SEND_BOTTON4 = 5;
    public static final int CMD_SEND_BOTTON5 = 6;
    public static final int CMD_SEND_BOTTON6 = 7;
    public static final int CMD_SEND_BOTTON7 = 8;
    public static final int CMD_SEND_BOTTON8 = 9;
    public static final int CMD_SEND_ENDCALL = 10;
    public static final int CMD_SEND_REJECTCALL = 11;
    public static final int CMD_SEND_OPEN = 12;

    private static final int HEADER_CALL = 0x11111111;
    private static final int HEADER_BOTTON1 = 0x22222222;
    private static final int HEADER_BOTTON2 = 0x33333333;
    private static final int HEADER_BOTTON3 = 0x44444444;
    private static final int HEADER_BOTTON4 = 0x55555555;
    private static final int HEADER_BOTTON5 = 0x66666666;
    private static final int HEADER_BOTTON6 = 0x77777777;
    private static final int HEADER_BOTTON7 = 0x88888888;
    private static final int HEADER_BOTTON8 = 0x99999999;
    private static final int HEADER_ENDCALL = 0x12341234;
    private static final int HEADER_REJECTCALL = 0x12345678;
    private static final int HEADER_OPEN = 0x87654321;

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
                try {
                    switch (msg.what){
                        case CMD_SEND_OPEN:
                            String s0 = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_OPEN);
                            mDataOutputStream.writeInt(s0.length());
                            mDataOutputStream.writeUTF(s0);
                            mDataOutputStream.flush();
                            break;
                        case CMD_SEND_CALL:
                            String s1 = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_CALL);
                            mDataOutputStream.writeInt(s1.length());
                            mDataOutputStream.writeUTF(s1);
                            mDataOutputStream.flush();
                            break;
                        case CMD_SEND_ENDCALL:
                            String s2 = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_ENDCALL);
                            mDataOutputStream.writeInt(s2.length());
                            mDataOutputStream.writeUTF(s2);
                            mDataOutputStream.flush();
                            break;
                        case CMD_SEND_REJECTCALL:
                            String s3 = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_REJECTCALL);
                            mDataOutputStream.writeInt(s3.length());
                            mDataOutputStream.writeUTF(s3);
                            mDataOutputStream.flush();
                            break;
                        case CMD_SEND_BOTTON1:
                            String s4 = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_BOTTON1);
                            mDataOutputStream.writeInt(s4.length());
                            mDataOutputStream.writeUTF(s4);
                            mDataOutputStream.flush();
                            break;
                        case CMD_SEND_BOTTON2:
                            String s5 = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_BOTTON2);
                            mDataOutputStream.writeInt(s5.length());
                            mDataOutputStream.writeUTF(s5);
                            mDataOutputStream.flush();
                            break;
                        case CMD_SEND_BOTTON3:
                            String s6 = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_BOTTON3);
                            mDataOutputStream.writeInt(s6.length());
                            mDataOutputStream.writeUTF(s6);
                            mDataOutputStream.flush();
                            break;
                        case CMD_SEND_BOTTON4:
                            String s7 = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_BOTTON4);
                            mDataOutputStream.writeInt(s7.length());
                            mDataOutputStream.writeUTF(s7);
                            mDataOutputStream.flush();
                            break;
                        case CMD_SEND_BOTTON5:
                            String s8 = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_BOTTON5);
                            mDataOutputStream.writeInt(s8.length());
                            mDataOutputStream.writeUTF(s8);
                            mDataOutputStream.flush();
                            break;
                        case CMD_SEND_BOTTON6:
                            String s9 = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_BOTTON6);
                            mDataOutputStream.writeInt(s9.length());
                            mDataOutputStream.writeUTF(s9);
                            mDataOutputStream.flush();
                            break;
                        case CMD_SEND_BOTTON7:
                            String s10 = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_BOTTON7);
                            mDataOutputStream.writeInt(s10.length());
                            mDataOutputStream.writeUTF(s10);
                            mDataOutputStream.flush();
                            break;
                        case CMD_SEND_BOTTON8:
                            String s11 = (String) msg.obj;
                            mDataOutputStream.writeInt(HEADER_BOTTON8);
                            mDataOutputStream.writeInt(s11.length());
                            mDataOutputStream.writeUTF(s11);
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
