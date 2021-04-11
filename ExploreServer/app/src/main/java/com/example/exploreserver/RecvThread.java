package com.example.exploreserver;

import android.os.Handler;
import android.os.Message;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RecvThread extends Thread {

    public static final int HEADER_FORWARD = 0x11111111; //앞으로 가는 키의 주소
    public static final int HEADER_BACK = 0X22222222; //뒤로 가는 키의 주소
    public static final int HEADER_RIGHT = 0x33333333; //오른쪽으로 가는 키의 주소
    public static final int HEADER_LEFT = 0x44444444; //왼쪽으로 가는 키의 주소
    public static final int HEADER_STOP = 0x55555555; //멈추는 키의 주소
    public static final int HEADER_VOICESTART=0x66666666; //음성전달시작 버튼 키의 주소
    public static final int HEADER_VOICEQUIT=0x77777777; //음성전달종료 버튼 키의 주소

    private DataInputStream mDataInputStream;

    public RecvThread(InputStream inputStream) {
        mDataInputStream = new DataInputStream(inputStream);
    }

    @Override
    public void run() {
        int header, length;
        byte[] byteArray;

        try {
            while (true) {
                header = mDataInputStream.readInt();
                length = mDataInputStream.readInt();
                switch (header) {
                    case HEADER_FORWARD:
                        String forward = mDataInputStream.readUTF();
                        Message msg1 = Message.obtain();
                        msg1.what = MainActivity.CMD_FORWARDBUTTON;
                        msg1.obj = "F";
                        ServerThread.mMainHandler.sendMessage(msg1);
                        break;
                    case HEADER_BACK:
                        String back = mDataInputStream.readUTF();
                        Message msg2 = Message.obtain();
                        msg2.what = MainActivity.CMD_BACKBUTTON;
                        msg2.obj = "B";
                        ServerThread.mMainHandler.sendMessage(msg2);
                        break;
                    case HEADER_RIGHT:
                        String right = mDataInputStream.readUTF();
                        Message msg3 = Message.obtain();
                        msg3.what = MainActivity.CMD_RIGHTBUTTON;
                        msg3.obj = "R";
                        ServerThread.mMainHandler.sendMessage(msg3);
                        break;
                    case HEADER_LEFT:
                        String left = mDataInputStream.readUTF();
                        Message msg4 = Message.obtain();
                        msg4.what = MainActivity.CMD_LEFTBUTTON;
                        msg4.obj = "L";
                        ServerThread.mMainHandler.sendMessage(msg4);
                        break;
                    case HEADER_STOP:
                        String stop = mDataInputStream.readUTF();
                        Message msg5 = Message.obtain();
                        msg5.what = MainActivity.CMD_STOP;
                        msg5.obj = "S";
                        ServerThread.mMainHandler.sendMessage(msg5);
                        break;
                    case HEADER_VOICESTART:
                        String voicestart=mDataInputStream.readUTF();
                        Message msg8 = Message.obtain();
                        msg8.what = MainActivity.CMD_VOICESTART;
                        msg8.obj = "start";
                        ServerThread.mMainHandler.sendMessage(msg8);
                        break;
                    case HEADER_VOICEQUIT:
                        String voicequit=mDataInputStream.readUTF();
                        Message msg9 = Message.obtain();
                        msg9.what = MainActivity.CMD_VOICEQUIT;
                        msg9.obj = "end";
                        ServerThread.mMainHandler.sendMessage(msg9);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
