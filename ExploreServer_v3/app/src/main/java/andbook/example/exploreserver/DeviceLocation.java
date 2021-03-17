package andbook.example.exploreserver;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;

public class DeviceLocation {

    private LocationManager mLocMgr;
    public double mLatitude;
    public double mLongitude;

    public DeviceLocation(Context context) {
        mLocMgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void start() {
        String provider = mLocMgr.getBestProvider(new Criteria(), true);
        mLocMgr.requestLocationUpdates(provider, 3000, 0, mLocListener);
    }

    public void stop() {
        mLocMgr.removeUpdates(mLocListener);
    }

    LocationListener mLocListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if (SendThread.mHandler == null) return;
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            if (SendThread.mHandler.hasMessages(SendThread.CMD_SEND_LOCATION)) {
                SendThread.mHandler.removeMessages(SendThread.CMD_SEND_LOCATION);
            }
            Message msg = Message.obtain();
            msg.what = SendThread.CMD_SEND_LOCATION;
            msg.obj = DeviceLocation.this;
            SendThread.mHandler.sendMessage(msg);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };
}
