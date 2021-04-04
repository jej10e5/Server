package org.tensorflow.lite.examples.detection;


import android.os.Message;

import java.io.DataInputStream;
import java.io.InputStream;

public class RecvThread extends Thread {
    public static final int HEADER_GO = 0x11111111; //앞으로 가는 키의 주소
    public static final int HEADER_BACK = 0X22222222; //뒤로 가는 키의 주소
    public static final int HEADER_RIGHT=0x33333333; //오른쪽으로 가는 키의 주소
    public static final int HEADER_LEFT=0x44444444; //왼쪽으로 가는 키의 주소
    public static final int HEADER_STOP=0x55555555; //멈추는 키의 주소
    public static final int SERVO_UP=0x66666666; //서보모터 동작 상방향 키의 주소
    public static final int SERVO_DOWN=0x77777777; //서보모터 동작 하방향 키의 주소
    public static final int HEADER_CHATSTART=0x88888888;
    public static final int HEADER_CHATSTOP=0x99999999;

    private DataInputStream mDataInputStream;

    public RecvThread(InputStream is){
        mDataInputStream = new DataInputStream(is);
    }

    @Override
    public void run(){
        int header, length;
        byte[] byteArray;

        try{
            while(true){
                header = mDataInputStream.readInt();
                length = mDataInputStream.readInt();
                switch (header) {
                    case HEADER_GO:
                        String go=mDataInputStream.readUTF();
                        Message msg1 = Message.obtain();
                        msg1.what = CameraActivity.CMD_GOBUTTON;
                        msg1.obj = "F";
                        ServerThread.mMainHandler.sendMessage(msg1);
                        break;
                    case HEADER_BACK:
                        String back=mDataInputStream.readUTF();
                        Message msg2 = Message.obtain();
                        msg2.what = CameraActivity.CMD_BACKBUTTON;
                        msg2.obj = "B";
                        ServerThread.mMainHandler.sendMessage(msg2);
                        break;
                    case HEADER_RIGHT:
                        String right=mDataInputStream.readUTF();
                        Message msg3 = Message.obtain();
                        msg3.what = CameraActivity.CMD_RIGHTBUTTON;
                        msg3.obj = "R";
                        ServerThread.mMainHandler.sendMessage(msg3);
                        break;
                    case HEADER_LEFT:
                        String left=mDataInputStream.readUTF();
                        Message msg4 = Message.obtain();
                        msg4.what = CameraActivity.CMD_LEFTBUTTON;
                        msg4.obj = "L";
                        ServerThread.mMainHandler.sendMessage(msg4);
                        break;
                    case HEADER_STOP:
                        String stop=mDataInputStream.readUTF();
                        Message msg5 = Message.obtain();
                        msg5.what = CameraActivity.CMD_STOP;
                        msg5.obj = "S";
                        ServerThread.mMainHandler.sendMessage(msg5);
                        break;
                    case SERVO_UP:
                        String up=mDataInputStream.readUTF();
                        Message msg6 = Message.obtain();
                        msg6.what = CameraActivity.CMD_UPBUTTON;
                        msg6.obj = "U";
                        ServerThread.mMainHandler.sendMessage(msg6);
                        break;
                    case SERVO_DOWN:
                        String down=mDataInputStream.readUTF();
                        Message msg7 = Message.obtain();
                        msg7.what = CameraActivity.CMD_DOWNBUTTON;
                        msg7.obj = "D";
                        ServerThread.mMainHandler.sendMessage(msg7);
                        break;
                    case HEADER_CHATSTART:
                        String chatstart=mDataInputStream.readUTF();
                        Message msg8 = Message.obtain();
                        msg8.what = CameraActivity.CMD_CHATSTART;
                        msg8.obj = "start";
                        ServerThread.mMainHandler.sendMessage(msg8);
                        break;
                    case HEADER_CHATSTOP:
                        String chatstop=mDataInputStream.readUTF();
                        Message msg9 = Message.obtain();
                        msg9.what = CameraActivity.CMD_CHATSTOP;
                        msg9.obj = "end";
                        ServerThread.mMainHandler.sendMessage(msg9);
                        break;

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
