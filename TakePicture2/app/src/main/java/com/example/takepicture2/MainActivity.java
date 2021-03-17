
package com.example.takepicture2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    private File mFile;
    private ImageView mViewPicture;
    private android.hardware.Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File sdcard= Environment.getExternalStorageDirectory();
        mFile=new File(sdcard.getAbsolutePath()+"/picture2.jpg");
        mViewPicture=(ImageView)findViewById(R.id.imgViewPicture);

        SurfaceView surfPreview=(SurfaceView)findViewById(R.id.surfPreview);
        SurfaceHolder surfHolder=surfPreview.getHolder();
        surfHolder.addCallback(mPreviewCallback);
    }

    private SurfaceHolder.Callback mPreviewCallback=new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            try {
                mCamera = android.hardware.Camera.open(0);
                if (Build.MODEL.equals("Nexus 5X")) {
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setRotation(180);
                    mCamera.setParameters(parameters);
                }
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            try{
                mCamera.stopPreview();
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            mCamera.stopPreview();
            mCamera.release();
        }
    };

    public void mOnClick(View v){
        switch (v.getId()){
            case R.id.btnTakePicture:
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if(success){
                            mCamera.takePicture(null,null,mPictureCallback);
                        }
                    }
                });
                break;
        }
    }
    private Camera.PictureCallback mPictureCallback=new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try{
                FileOutputStream fos=new FileOutputStream(mFile);
                fos.write(data);
                fos.flush();
                fos.close();
                viewPicture(mFile);
                mCamera.startPreview();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private void viewPicture(File file){
        int width=mViewPicture.getWidth();
        int height=mViewPicture.getHeight();

        BitmapFactory.Options bmpFactoryOptions=new BitmapFactory.Options();
        bmpFactoryOptions.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(file.getAbsolutePath(),bmpFactoryOptions);

        int widthRatio=(int)Math.ceil(bmpFactoryOptions.outWidth/(float)width);
        int heigthRatio=(int)Math.ceil(bmpFactoryOptions.outHeight/(float)height);
        if(heigthRatio>1||widthRatio>1){
            if(heigthRatio>widthRatio){
                bmpFactoryOptions.inSampleSize=heigthRatio;
            }else{
                bmpFactoryOptions.inSampleSize=widthRatio;
            }
        }

        bmpFactoryOptions.inJustDecodeBounds=false;
        Bitmap bmp=BitmapFactory.decodeFile(file.getAbsolutePath(),bmpFactoryOptions);
        mViewPicture.setImageBitmap(bmp);
    }
}