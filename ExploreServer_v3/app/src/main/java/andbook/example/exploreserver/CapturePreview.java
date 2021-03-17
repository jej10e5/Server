package andbook.example.exploreserver;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Message;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@SuppressWarnings("deprecation")
public class CapturePreview implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private Activity mActivity;
    private ImageView mImageView;

    private Camera mCamera;
    private int mCameraId = 0; // 후면 카메라
    private int mCount;

    public CapturePreview(Activity activity, SurfaceView surfaceView, ImageView imageView) {
        mActivity = activity;
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        mImageView = imageView;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera = Camera.open(mCameraId);
            setCameraDisplayOrientation(mCamera);
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        try {
            mCamera.stopPreview();
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(this); // 교재에서 누락된 한 줄을 추가합니다.
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.setPreviewCallback(null);
        mCamera.release();
    }

    private int getCameraDisplayOrientation() {
        int degrees = 0;
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();

        switch (rotation) {
        case Surface.ROTATION_0:
            degrees = 0;
            break;
        case Surface.ROTATION_90:
            degrees = 90;
            break;
        case Surface.ROTATION_180:
            degrees = 180;
            break;
        case Surface.ROTATION_270:
            degrees = 270;
            break;
        }

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, cameraInfo);
        return (cameraInfo.orientation - degrees + 360) % 360;
    }

    private void setCameraDisplayOrientation(Camera camera) {
        int orientation = getCameraDisplayOrientation();
        camera.setDisplayOrientation(orientation);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (++mCount == 10) {
            mCount = 0;

            Camera.Parameters parameters = camera.getParameters();
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;
            int format = parameters.getPreviewFormat();
            int orientation = getCameraDisplayOrientation();

            YuvImage image = new YuvImage(data, format, width, height, null);
            Rect rect = new Rect(0, 0, width, height);
            Bitmap bitmap = rotateBitmap(image, orientation, rect);

            mImageView.setImageBitmap(bitmap);
            sendBitmapThroughNetwork(bitmap);
        }
    }

    private Bitmap rotateBitmap(YuvImage yuvImage, int orientation, Rect rect) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(rect, 100, os);

        byte[] bytes = os.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        return Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void sendBitmapThroughNetwork(Bitmap bitmap) {
        if (SendThread.mHandler == null) return;
        if (SendThread.mHandler.hasMessages(SendThread.CMD_SEND_BITMAP)) {
            SendThread.mHandler.removeMessages(SendThread.CMD_SEND_BITMAP);
        }
        Message msg = Message.obtain();
        msg.what = SendThread.CMD_SEND_BITMAP;
        msg.obj = bitmap;
        SendThread.mHandler.sendMessage(msg);
    }
}
