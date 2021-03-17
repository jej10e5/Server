package andbook.example.exploreserver;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;

public class SendThread extends Thread {

    public static final int CMD_SEND_BITMAP = 1;
    public static final int CMD_SEND_LOCATION = 2;

    private static final int HEADER_BITMAP = 0x11111111;
    private static final int HEADER_LOCATION = 0x22222222;

    private DataOutputStream mDataOutputStream;
    public static Handler mHandler;

    public SendThread(OutputStream os) {
        mDataOutputStream = new DataOutputStream(os);
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                byte[] byteArray;
                try {
                    switch (msg.what) {
                    case CMD_SEND_BITMAP: // 비트맵 전송
                        Bitmap bitmap = (Bitmap) msg.obj;
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, stream);
                        byteArray = stream.toByteArray();
                        // 헤더 + 길이 + 데이터 순으로 보낸다.
                        mDataOutputStream.writeInt(HEADER_BITMAP);
                        mDataOutputStream.writeInt(byteArray.length);
                        mDataOutputStream.write(byteArray);
                        mDataOutputStream.flush();
                        break;
                    case CMD_SEND_LOCATION: // 위치 전송
                        DeviceLocation loc = (DeviceLocation) msg.obj;
                        // 헤더 + 길이 + 데이터 순으로 보낸다.
                        mDataOutputStream.writeInt(HEADER_LOCATION);
                        mDataOutputStream.writeInt(8 * 2); // 길이
                        mDataOutputStream.writeDouble(loc.mLatitude);
                        mDataOutputStream.writeDouble(loc.mLongitude);
                        mDataOutputStream.flush();
                        break;
                    }
                } catch (Exception e) {
                    getLooper().quit();
                }
            }
        };
        Looper.loop();
    }
}
