package andbook.example.exploreclient;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static andbook.example.exploreclient.R.id.map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final int CMD_APPEND_TEXT = 0;
    public static final int CMD_ENABLE_CONNECT_BUTTON = 1;
    public static final int CMD_SHOW_BITMAP = 2;
    public static final int CMD_SHOW_MAP = 3;

    private ImageView mImageFrame;
    private TextView mTextStatus;
    private EditText mEditIP;
    private Button mBtnConnect;
    private ClientThread mClientThread;

    private GoogleMap mMap;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageFrame = (ImageView) findViewById(R.id.imageFrame);
        mTextStatus = (TextView) findViewById(R.id.textStatus);
        mEditIP = (EditText) findViewById(R.id.editIP);
        mBtnConnect = (Button) findViewById(R.id.btnConnect);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean auto_connect = prefs.getBoolean("pref_autoconnect", false);
        String default_ip = prefs.getString("pref_defaultip", "127.0.0.1");
        mEditIP.setText(default_ip);
        if (auto_connect)
            mOnClick(mBtnConnect);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.item_settings:
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void mOnClick(View v) {
        switch (v.getId()) {
        case R.id.btnConnect:
            if (mClientThread != null) return;
            String addr = mEditIP.getText().toString();
            if (addr.length() == 0) return;
            mClientThread = new ClientThread(addr, mMainHandler);
            mClientThread.start();
            mBtnConnect.setEnabled(false);
            break;
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
            case CMD_ENABLE_CONNECT_BUTTON: // 연결 버튼 활성화
                mClientThread = null;
                mBtnConnect.setEnabled(true);
                break;
            case CMD_SHOW_BITMAP: // 비트맵 출력
                Bitmap bitmap = (Bitmap) msg.obj;
                mImageFrame.setImageBitmap(bitmap);
                break;
            case CMD_SHOW_MAP: // 위치 출력
                if (mMap == null) return;
                LatLng latlng = (LatLng) msg.obj;
                if (mMarker != null) mMarker.remove();
                mMarker = mMap.addMarker(new MarkerOptions().position(latlng));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                break;
            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomTo(13));
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}
