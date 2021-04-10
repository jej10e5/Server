package com.toure.objectdetection;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;

public class ServerThread extends Thread {
    private Context mContext;
    public static Handler mMainHandler;

    public String ipaddr;

    public ServerThread(Context context, Handler mainHandler) {
        mContext = context;
        mMainHandler = mainHandler;
    }

    @Override
    public void run() {
        ServerSocket servSockt = null;

        try {
            servSockt = new ServerSocket(9000);
            while (true) {
                Socket sock = servSockt.accept();
                String ip = sock.getInetAddress().getHostAddress();
                int port = sock.getPort();
                ipaddr = sock.getInetAddress().getHostAddress();
                try {
                    DatagramSocket datagramSocket = new DatagramSocket(50000);
                    SendThread thread = new SendThread(sock.getOutputStream(), getDeviceIP());
                    RecvThread recvThread = new RecvThread(sock.getInputStream());
                    UDPRecvThread udpRecvThread = new UDPRecvThread(datagramSocket);
                    recvThread.setPriority(9);
                    thread.start();
                    recvThread.start();
                    udpRecvThread.start();
                    udpRecvThread.join();
                    thread.join();
                    recvThread.join();
                } catch (Exception e) {
                    doPrintln(e.getMessage());
                }
                sock.close();
            }
        } catch (Exception e) {
            doPrintln(e.getMessage());
        } finally {
            try {
                if (servSockt != null) {
                    servSockt.close();
                }
            } catch (IOException e) {
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

    public String getDeviceIP() {
        String ipaddr = getWifiIp();
        if(ipaddr == null)
            ipaddr = getMobileIP();
        if (ipaddr == null)
            ipaddr = "127.0.0.1";
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

    private String getMobileIP() {
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
