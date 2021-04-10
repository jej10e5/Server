package com.example.exploreclient;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class VoiceSend extends Thread{
    private static final int SAMPLE_RATE = 8000; // Hertz
    private static final int SAMPLE_INTERVAL = 20; // Milliseconds
    private static final int SAMPLE_SIZE = 2; // Bytes
    private static final int BUF_SIZE = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE * 2; //Bytes
    String ip;
    private int port = 50005; // Port the packets are addressed to
    private boolean mic = false; // Enable mic?

    public VoiceSend(String ip){
        this.ip=ip;
    }

    public boolean MicOn(){
        mic=true;
        return mic;
    }

    public boolean MicOff(){
        mic=false;
        return  mic;
    }

    public void run() {
        MicOn();
        AudioRecord audioRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 10);
        int bytes_read = 0;
        int bytes_sent = 0;
        byte[] buf = new byte[BUF_SIZE];
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            audioRecorder.startRecording();

            while (mic) {
                // Capture audio from the mic and transmit it
                InetAddress inetAddress = InetAddress.getByName(ip);
                bytes_read = audioRecorder.read(buf, 0, BUF_SIZE);
                DatagramPacket packet = new DatagramPacket(buf, bytes_read, inetAddress, port);
                datagramSocket.send(packet);
                bytes_sent += bytes_read;
                Thread.sleep(SAMPLE_INTERVAL, 0);
            }
            // Stop recording and release resources
            audioRecorder.stop();
            audioRecorder.release();
            datagramSocket.disconnect();
            datagramSocket.close();
            return;
        } catch (InterruptedException e) {


            mic = false;
        } catch (SocketException e) {


            mic = false;
        } catch (UnknownHostException e) {


            mic = false;
        } catch (IOException e) {


            mic = false;
        }
    }
}
