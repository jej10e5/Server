package com.example.testclient04;

import android.os.Handler;
import android.os.Message;

import java.net.DatagramSocket;
import java.net.Socket;

public class ClientThread extends Thread {

    public String mServAddr;
    public static Handler mMainHandler;


    public ClientThread(String servAddr, Handler mainHandler){
        mServAddr = servAddr;
        mMainHandler = mainHandler;
    }

    @Override
    public void run(){
        Socket sock = null;
        try {
            sock = new Socket(mServAddr, 9000);
            DatagramSocket datagramSocket = new DatagramSocket(50001);
            SendThread thread = new SendThread(sock.getOutputStream());
            RecvThread recvThread = new RecvThread(sock.getInputStream());
            UDPRecvThread udpRecvThread = new UDPRecvThread(datagramSocket);
            thread.setPriority(9);
            thread.start();
            recvThread.start();
            udpRecvThread.start();
            thread.join();
            recvThread.join();
            udpRecvThread.join();
        }catch (Exception e){
            doPrintIn(e.getMessage());
        }finally {
            try {
                if(sock != null){
                    sock.close();
                }
                enableConnectButton();
            }catch (Exception e){
                doPrintIn(e.getMessage());
            }
        }
    }

    public static void doPrintIn(String str){
        Message msg = Message.obtain();
        msg.what = MainActivity.CMD_APPEND_TEXT;
        msg.obj = str +"\n";
        mMainHandler.sendMessage(msg);
    }

    private static void enableConnectButton(){
        Message msg = Message.obtain();
        msg.what = MainActivity.CMD_ENABLE_CONNECT_BUTTON;
        mMainHandler.sendMessage(msg);
    }
}