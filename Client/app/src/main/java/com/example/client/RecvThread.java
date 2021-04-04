package com.example.client;


import android.graphics.BitmapFactory;
import android.os.Message;

import java.io.DataInputStream;
import java.io.InputStream;

public class RecvThread extends Thread {
    public static final int HEADER_BITMAP = 0x11111111;
    public static final int HEADER_LOCATION = 0X22222222;
    public static final int HEADER_BAT = 0x33333333;

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
                    case HEADER_BITMAP:
                        byteArray = new byte[length];
                        mDataInputStream.readFully(byteArray);

                        Message msg1 = Message.obtain();
                        msg1.what = MainActivity.CMD_SHOW_BITMAP;
                        msg1.obj = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        ClientThread.mMainHandler.sendMessage(msg1);
                        break;


                    case HEADER_BAT:
                        String message2 = mDataInputStream.readUTF();
                        Message msg2 = Message.obtain();
                        msg2.what = MainActivity.CMD_BAT;
                        msg2.obj = message2;
                        ClientThread.mMainHandler.sendMessage(msg2);
                        break;

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}