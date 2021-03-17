package andbook.example.exploreclient;

import android.graphics.BitmapFactory;
import android.os.Message;

import com.google.android.gms.maps.model.LatLng;

import java.io.DataInputStream;
import java.io.InputStream;

public class RecvThread extends Thread {

    public static final int HEADER_BITMAP = 0x11111111;
    public static final int HEADER_LOCATION = 0x22222222;

    private DataInputStream mDataInputStream;

    public RecvThread(InputStream is) {
        mDataInputStream = new DataInputStream(is);
    }

    @Override
    public void run() {
        int header, length;
        byte[] byteArray;

        try {
            while (true) {
                // (1) 헤더를 읽는다.
                header = mDataInputStream.readInt();
                // (2) 데이터의 길이를 읽는다.
                length = mDataInputStream.readInt();
                // (3) 헤더의 타입에 따라 다르게 처리한다.
                switch (header) {
                case HEADER_BITMAP:
                    byteArray = new byte[length];
                    mDataInputStream.readFully(byteArray);

                    Message msg1 = Message.obtain();
                    msg1.what = MainActivity.CMD_SHOW_BITMAP;
                    msg1.obj = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                    ClientThread.mMainHandler.sendMessage(msg1);
                    break;
                case HEADER_LOCATION:
                    double latitude = mDataInputStream.readDouble();
                    double longitude = mDataInputStream.readDouble();
                    LatLng latlng = new LatLng(latitude, longitude);

                    Message msg2 = Message.obtain();
                    msg2.what = MainActivity.CMD_SHOW_MAP;
                    msg2.obj = latlng;
                    ClientThread.mMainHandler.sendMessage(msg2);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
