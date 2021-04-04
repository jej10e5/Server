package org.tensorflow.lite.examples.detection;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Formatter;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

@SuppressWarnings("deprecation")
public class ServerThread extends Thread {
    private Context mContext;
    static Handler mMainHandler;

    public String ip2; //UDP 소켓와 연결 하기 위한 ip주소 선언

    public ServerThread(Context context, Handler mainHandler) {
        mContext = context;
        mMainHandler = mainHandler;
    }

    @Override
    public void run() {
        ServerSocket servSock = null;
        try {
            servSock = new ServerSocket(9000);
            doPrintIn(">>서버(사용자) 접속 되었습니다 : " + getDeviceIp() + "/" + servSock.getLocalPort());
            while (true) {
                Socket sock = servSock.accept();
                String ip = sock.getInetAddress().getHostAddress();
                int port = sock.getPort();
                ip2=sock.getInetAddress().getHostAddress();
                doPrintIn(">> 클라이언트 접속 : " + ip + "/" + port);
                try {
                    DatagramSocket datagramSocket=new DatagramSocket(50005); //UDP 포트 번호 설정
                    SendThread thread = new SendThread(sock.getOutputStream());
                    RecvThread recvthread=new RecvThread(sock.getInputStream());
                    ChatRecv chatRecv=new ChatRecv(datagramSocket);

                    thread.start();
                    recvthread.start();
                    chatRecv.start();
                    thread.join();
                    recvthread.join();
                    chatRecv.join();
                } catch (Exception e) {
                    doPrintIn(e.getMessage());
                }
                sock.close();
                doPrintIn(">> 클라이언트 접속 : " + ip + "/" + port);
            }
        } catch (Exception e) {
            doPrintIn(e.getMessage());
        } finally {
            try {
                if (servSock != null) {
                    servSock.close();
                }
                doPrintIn(">> 서버(사용자)자 종료 되었습니다.");
            } catch (IOException e) {
                doPrintIn(e.getMessage());
            }
        }
    }

    public static void doPrintIn(String str) {
        Message msg = Message.obtain();
        msg.what = CameraActivity.CMD_APPEND_TEXT;
        msg.obj = str + "\n";
        mMainHandler.sendMessage(msg);
    }

    private String getDeviceIp() {

        String ipaddr = getWifiIp();
        if (ipaddr == null)
            ipaddr = getMobileIp();
        if (ipaddr == null)
            ipaddr = "";
        return ipaddr;
    }

    private String getWifiIp() {
        WifiManager wifiMgr = (WifiManager) mContext.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr != null && wifiMgr.isWifiEnabled()) {
            int ip = wifiMgr.getConnectionInfo().getIpAddress();
            return Formatter.formatIpAddress(ip);
        }
        return null;
    }

    private String getMobileIp() {
        try {
            for (Enumeration<NetworkInterface> e1 = NetworkInterface.getNetworkInterfaces(); e1.hasMoreElements(); ) {
                NetworkInterface networkInterface = e1.nextElement();
                for (Enumeration<InetAddress> e2 = networkInterface.getInetAddresses(); e2.hasMoreElements(); ) {
                    InetAddress inetAddress = e2.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String host = inetAddress.getHostAddress();
                        if (!TextUtils.isEmpty(host)) {
                            return host;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}