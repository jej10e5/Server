package com.toure.objectdetection;

import android.os.Message;

import java.io.DataInputStream;
import java.io.InputStream;

public class RecvThread extends Thread {
    public static final int HEADER_CALL = 0x11111111;
    public static final int HEADER_BOTTON1 = 0x22222222;
    public static final int HEADER_BOTTON2 = 0x33333333;
    public static final int HEADER_BOTTON3 = 0x44444444;
    public static final int HEADER_BOTTON4 = 0x55555555;
    public static final int HEADER_BOTTON5 = 0x66666666;
    public static final int HEADER_BOTTON6 = 0x77777777;
    public static final int HEADER_BOTTON7 = 0x88888888;
    public static final int HEADER_BOTTON8 = 0x99999999;
    public static final int HEADER_ENDCALL = 0x12341234;
    public static final int HEADER_REJECTCALL = 0x12345678;
    public static final int HEADER_OPEN = 0x87654321;

    private DataInputStream mDataInputStream;

    public RecvThread(InputStream is){
        mDataInputStream = new DataInputStream(is);
    }

    @Override
    public void run(){
        int header, length;
        try{
            while(true){
                header = mDataInputStream.readInt();
                length = mDataInputStream.readInt();
                switch (header) {
                    case HEADER_CALL:
                        String message0 = mDataInputStream.readUTF();
                        Message msg0 = Message.obtain();
                        msg0.what = MainActivity.CMD_CALL;
                        msg0.obj = message0;
                        ServerThread.mMainHandler.sendMessage(msg0);
                        break;
                    case HEADER_ENDCALL:
                        String message1 = mDataInputStream.readUTF();
                        Message msg1 = Message.obtain();
                        msg1.what = MainActivity.CMD_ENDCALL;
                        msg1.obj = message1;
                        ServerThread.mMainHandler.sendMessage(msg1);
                        break;
                    case HEADER_REJECTCALL:
                        String message2 = mDataInputStream.readUTF();
                        Message msg2 = Message.obtain();
                        msg2.what = MainActivity.CMD_REJECTCALL;
                        msg2.obj = message2;
                        ServerThread.mMainHandler.sendMessage(msg2);
                        break;
                    case HEADER_BOTTON1:
                        String message3 = mDataInputStream.readUTF();
                        Message msg3 = Message.obtain();
                        msg3.what = MainActivity.CMD_BOTTON1;
                        msg3.obj = "w";
                        ServerThread.mMainHandler.sendMessage(msg3);
                        break;
                    case HEADER_BOTTON2:
                        String message4 = mDataInputStream.readUTF();
                        Message msg4 = Message.obtain();
                        msg4.what = MainActivity.CMD_BOTTON2;
                        msg4.obj = "a";
                        ServerThread.mMainHandler.sendMessage(msg4);
                        break;
                    case HEADER_BOTTON3:
                        String message5 = mDataInputStream.readUTF();
                        Message msg5 = Message.obtain();
                        msg5.what = MainActivity.CMD_BOTTON3;
                        msg5.obj = "s";
                        ServerThread.mMainHandler.sendMessage(msg5);
                        break;
                    case HEADER_BOTTON4:
                        String message6 = mDataInputStream.readUTF();
                        Message msg6 = Message.obtain();
                        msg6.what = MainActivity.CMD_BOTTON4;
                        msg6.obj = "d";
                        ServerThread.mMainHandler.sendMessage(msg6);
                        break;
                    case HEADER_BOTTON5:
                        String message7 = mDataInputStream.readUTF();
                        Message msg7 = Message.obtain();
                        msg7.what = MainActivity.CMD_BOTTON5;
                        msg7.obj = "t";
                        ServerThread.mMainHandler.sendMessage(msg7);
                        break;
                    case HEADER_BOTTON6:
                        String message8 = mDataInputStream.readUTF();
                        Message msg8 = Message.obtain();
                        msg8.what = MainActivity.CMD_BOTTON6;
                        msg8.obj = "f";
                        ServerThread.mMainHandler.sendMessage(msg8);
                        break;
                    case HEADER_BOTTON7:
                        String message9 = mDataInputStream.readUTF();
                        Message msg9 = Message.obtain();
                        msg9.what = MainActivity.CMD_BOTTON7;
                        msg9.obj = "g";
                        ServerThread.mMainHandler.sendMessage(msg9);
                        break;
                    case HEADER_BOTTON8:
                        String message10 = mDataInputStream.readUTF();
                        Message msg10 = Message.obtain();
                        msg10.what = MainActivity.CMD_BOTTON8;
                        msg10.obj = "h";
                        ServerThread.mMainHandler.sendMessage(msg10);
                        break;
                    case HEADER_OPEN:
                        String message11 = mDataInputStream.readUTF();
                        Message msg11 = Message.obtain();
                        msg11.what = MainActivity.CMD_OPEN;
                        msg11.obj = "o";
                        ServerThread.mMainHandler.sendMessage(msg11);
                        break;
            }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}