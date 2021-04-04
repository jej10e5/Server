package com.example.client;


import android.os.Handler;
import android.os.Message;

import java.net.DatagramSocket;
import java.net.Socket;

public class ClientThread extends Thread {

    private String mServAddr; //소켓 생성을 위한 변수 선언
    public static Handler mMainHandler;

    public ClientThread(String servAddr, Handler mainHandler){
        mServAddr = servAddr;
        mMainHandler = mainHandler;
    }

    @Override
    public void run(){
        Socket sock = null;
        try {
            sock = new Socket(mServAddr, 9000); //서버에 연결하기 위한 소켓 설정, 포트 번호 설정
            DatagramSocket datagramSocket=new DatagramSocket(50006); //서버와 음성 통화를 위한 UDP소켓 설정
            doPrintIn(">> 서버(사용자)와 연결 성공하였습니다.");
            SendThread sendThread = new SendThread(sock.getOutputStream());
            RecvThread recvThread = new RecvThread(sock.getInputStream());
            ChatRecv chatRecv=new ChatRecv(datagramSocket);

            recvThread.start();
            sendThread.start();
            chatRecv.start();

            recvThread.join();
            sendThread.join();
            chatRecv.join();

        }catch (Exception e){
            doPrintIn(e.getMessage());
        }finally {
            try {
                if(sock != null){
                    sock.close();
                    doPrintIn(" >> 서버(사용자)와 연결 종료되었습니다.");
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