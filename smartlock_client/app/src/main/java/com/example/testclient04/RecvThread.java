package com.example.testclient04;

import android.graphics.BitmapFactory;
import android.os.Message;
import android.util.Log;
import java.io.DataInputStream;
import java.io.InputStream;

public class RecvThread extends Thread {
    public static final int HEADER_BITMAP = 0x11111111;
    public static final int HEADER_CALL = 0x22222222;
    public static final int HEADER_ENDCALL = 0x33333333;
    public static final int HEADER_BAT = 0x44444444;
    public static final int HEADER_WARMING = 0x55555555;

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
                Log.e(this.getClass().getName(),"왜안될까2");
                header = mDataInputStream.readInt();
                length = mDataInputStream.readInt();
                switch (header) {
                    case HEADER_BITMAP:
                        byteArray = new byte[length];
                        mDataInputStream.readFully(byteArray);
                        Message msg1 = Message.obtain();
                        msg1.what = MainActivity.CMD_SHOW_BITMAP;
                        msg1.obj = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        ClientThread.mMainHandler.sendMessage(msg1);
                        break;
                    case HEADER_CALL:
                        String message = mDataInputStream.readUTF();
                        Message msg2 = Message.obtain();
                        msg2.what = MainActivity.CMD_CALL;
                        ClientThread.mMainHandler.sendMessage(msg2);
                        break;
                    case HEADER_ENDCALL:
                        Message msg3 = Message.obtain();
                        msg3.what = MainActivity.CMD_ENDCALL;
                        ClientThread.mMainHandler.sendMessage(msg3);
                        break;
                    case HEADER_BAT:
                        String message4 = mDataInputStream.readUTF();
                        Message msg4 = Message.obtain();
                        msg4.what = MainActivity.CMD_BAT;
                        msg4.obj = message4;
                        ClientThread.mMainHandler.sendMessage(msg4);
                        break;
                    case HEADER_WARMING:
                        String message5= mDataInputStream.readUTF();
                        Message msg5 = Message.obtain();
                        msg5.what = MainActivity.CMD_WARMING;
                        msg5.obj = message5;
                        ClientThread.mMainHandler.sendMessage(msg5);
                        break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}