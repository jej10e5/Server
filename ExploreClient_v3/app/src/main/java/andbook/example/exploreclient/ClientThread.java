package andbook.example.exploreclient;

import android.os.Handler;
import android.os.Message;

import java.net.Socket;

public class ClientThread extends Thread {

    private String mServAddr;
    public static Handler mMainHandler;

    public ClientThread(String servAddr, Handler mainHandler) {
        mServAddr = servAddr;
        mMainHandler = mainHandler;
    }

    @Override
    public void run() {
        Socket sock = null;
        try {
            sock = new Socket(mServAddr, 9000);
            doPrintln(">> 서버와 연결 성공!");
            RecvThread recvThread = new RecvThread(sock.getInputStream());
            recvThread.start();
            recvThread.join();
        } catch (Exception e) {
            doPrintln(e.getMessage());
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                    doPrintln(">> 서버와 연결 종료!");
                }
                enableConnectButton();
            } catch (Exception e) {
                doPrintln(e.getMessage());
            }
        }
    }

    public static void doPrintln(String str) {
        Message msg = Message.obtain();
        msg.what = MainActivity.CMD_APPEND_TEXT;
        msg.obj = str + "\n";
        mMainHandler.sendMessage(msg);
    }

    private static void enableConnectButton() {
        Message msg = Message.obtain();
        msg.what = MainActivity.CMD_ENABLE_CONNECT_BUTTON;
        mMainHandler.sendMessage(msg);
    }
}
