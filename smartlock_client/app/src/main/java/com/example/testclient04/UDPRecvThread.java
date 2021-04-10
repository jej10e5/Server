package com.example.testclient04;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPRecvThread extends Thread {
    private static final int SAMPLE_RATE = 8000;
    private static final int SAMPLE_INTERVAL = 20;
    private static final int SAMPLE_SIZE = 2;
    private static final int BUF_SIZE = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE * 2; //Bytes
    private boolean speakers = true;
    DatagramSocket datagramSocket;

    public UDPRecvThread(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    @Override
    public void run() {
        AudioTrack track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, AudioTrack.MODE_STREAM);
        track.play();
        try {
            // Define a socket to receive the audio
            byte[] buf = new byte[BUF_SIZE];
            while(speakers) {
                // Play back the audio received from packets
                DatagramPacket packet = new DatagramPacket(buf, BUF_SIZE);
                datagramSocket.receive(packet);
                track.write(packet.getData(), 0, BUF_SIZE);
            }
            // Stop playing back and release resources
            datagramSocket.disconnect();
            datagramSocket.close();
            track.stop();
            track.flush();
            track.release();
            return;
        }
        catch(SocketException e) {
            speakers = false;
        }
        catch(IOException e) {
            speakers = false;
        }
    }
}
