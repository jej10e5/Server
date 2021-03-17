package andbook.example.exploreserver;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static final int CMD_APPEND_TEXT = 0;

    private TextView mTextStatus;
    private CapturePreview mCapturePreview;
    private DeviceLocation mDeviceLocation;
    private ServerThread mServerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mCapturePreview = new CapturePreview(this,
                (SurfaceView) findViewById(R.id.surfPreview),
                (ImageView) findViewById(R.id.imageFrame));
        mDeviceLocation = new DeviceLocation(this);

        if (mServerThread == null) { // 서버 시작
            mServerThread = new ServerThread(this, mMainHandler);
            mServerThread.start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDeviceLocation.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDeviceLocation.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void mOnClick(View v) {
        switch (v.getId()) {
        case R.id.btnQuit:
            finish();
            break;
        }
    }

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case CMD_APPEND_TEXT: // 텍스트 출력
                mTextStatus.append((String) msg.obj);
                break;
            }
        }
    };
}
