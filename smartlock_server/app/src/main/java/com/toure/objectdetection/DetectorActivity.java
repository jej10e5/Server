/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.toure.objectdetection;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.hardware.Camera;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Size;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import com.toure.objectdetection.customview.OverlayView;
import com.toure.objectdetection.env.BorderedText;
import com.toure.objectdetection.env.ImageUtils;
import com.toure.objectdetection.env.Logger;
import com.toure.objectdetection.tflite.Classifier;
import com.toure.objectdetection.tflite.TFLiteObjectDetectionAPIModel;
import com.toure.objectdetection.tracking.MultiBoxTracker;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class DetectorActivity extends MainActivity implements OnImageAvailableListener,
        Camera.PreviewCallback {
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 5);
            toast("권한 허용해주세용용");
        }
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.KOREA);
            }
        });
    }

    public void inputVoice() {
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
            final SpeechRecognizer stt = SpeechRecognizer.createSpeechRecognizer(this);
            stt.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    toast("음성 입력 시작...");
                }

                @Override
                public void onBeginningOfSpeech() {
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                    toast("음성 입력 종료");
                }

                @Override
                public void onError(int error)
                {
                    stt.destroy();
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> result = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
                    replyAnswer(result.get(0));
                    stt.destroy();
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });
            stt.startListening(intent);
        } catch (Exception e) {
            toast(e.toString());
        }
    }

    public void inputVoice2() {
        try {
            Thread.sleep(5000);
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
            final SpeechRecognizer stt = SpeechRecognizer.createSpeechRecognizer(this);
            stt.setRecognitionListener(new RecognitionListener() {

                @Override
                public void onReadyForSpeech(Bundle params) {
                    toast("Password");
                }

                @Override
                public void onBeginningOfSpeech() {
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                }

                @Override
                public void onEndOfSpeech() {
                    toast("음성 입력 종료");
                }

                @Override
                public void onError(int error) {
                    stt.destroy();
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> result2 = (ArrayList<String>) results.get(SpeechRecognizer.RESULTS_RECOGNITION);
                    replyAnswer2(result2.get(0));
                    stt.destroy();
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                }
            });
            stt.startListening(intent);
        } catch (Exception e) {
            toast(e.toString());
        }
    }

    private void replyAnswer(String input) {
        try {
            if (input.equals("박민우")) {
                tts.speak("The return of the master", TextToSpeech.QUEUE_FLUSH, null);
                try{
                    Thread.sleep(2000);
                    tts.speak("Password", TextToSpeech.QUEUE_FLUSH, null);
                    Thread.sleep(2000);
                    inputVoice2();
                }
                catch(Exception e){
                }
            }
            else if (input.equals("김태호")) {
                tts.speak("The return of the master", TextToSpeech.QUEUE_FLUSH, null);
                try{
                    Thread.sleep(2000);
                    tts.speak("Password", TextToSpeech.QUEUE_FLUSH, null);
                    Thread.sleep(2000);
                    inputVoice2();
                }
                catch(Exception e){
                }

            }
            else if (input.equals("임형신")) {
                tts.speak("The return of the master", TextToSpeech.QUEUE_FLUSH, null);
                try{
                    Thread.sleep(2000);
                    tts.speak("Password", TextToSpeech.QUEUE_FLUSH, null);
                    Thread.sleep(2000);
                    inputVoice2();
                }
                catch(Exception e){
                }
            }else if (input.equals("끝낸다")) {
                finish();
            } else {
                tts.speak("Be not a master", TextToSpeech.QUEUE_FLUSH, null);
                aram.sendEmptyMessage(0);
            }
        } catch (Exception e) {
            toast(e.toString());
        }
    }

    private void replyAnswer2(String input) {
        try {
            if (input.equals("열려라 참깨")) {
                try{
                    Thread.sleep(2000);
                }
                catch(Exception e){
                }
                tts.speak("Master, thank you for coming. The door is open", TextToSpeech.QUEUE_FLUSH, null);
                mhanmder.sendEmptyMessage(0);

            } else if (input.equals("끝낸다")) {
                finish();
            } else {
                tts.speak("Go back to the beginning", TextToSpeech.QUEUE_FLUSH, null);
            }
        } catch (Exception e) {
            toast(e.toString());
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private static final Logger LOGGER = new Logger();

    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "detectkingest.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmapking.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.9f;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(1920, 1080);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;
    OverlayView trackingOverlay;
    private Integer sensorOrientation;

    private Classifier detector;
    private int mCount =0;

    private long lastProcessingTimeMs;
    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;
    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;

    private BorderedText borderedText;

    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this);

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            getAssets(),
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast toast =
                    Toast.makeText(
                            getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
            finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

        if (++mCount == 1) {
            mCount = 0;
        }

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                        tracker.draw(canvas);
                        if (isDebug()) {
                            tracker.drawDebug(canvas);

                        }
                    }
                });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    public void sendBitmapThroughNetwork(Bitmap bitmap) {
        if (SendThread.mHandler == null){
            return;
        }
        if (SendThread.mHandler.hasMessages(SendThread.CMD_SEND_BITMAP)) {
            SendThread.mHandler.removeMessages(SendThread.CMD_SEND_BITMAP);
        }
        Message msg = Message.obtain();
        msg.what = SendThread.CMD_SEND_BITMAP;
        msg.obj = bitmap;
        SendThread.mHandler.sendMessage(msg);
    }

    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
        sendBitmapThroughNetwork(rgbFrameBitmap);
        LOGGER.i("제발 ㅠ");

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        runInBackground(
                new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        LOGGER.i("Running detection on image " + currTimestamp);
                                        final long startTime = SystemClock.uptimeMillis();
                                        final List<Classifier.Recognition> results = detector.recognizeImage(croppedBitmap);
                                        lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                                        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
                                        final Canvas canvas = new Canvas(cropCopyBitmap);
                                        final Paint paint = new Paint();
                                        paint.setColor(Color.RED);
                                        paint.setStyle(Style.STROKE);
                                        paint.setStrokeWidth(2.0f);

                                        float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                                        switch (MODE) {
                                            case TF_OD_API:
                                                minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                                                break;
                                        }

                                        final List<Classifier.Recognition> mappedRecognitions =
                                                new LinkedList<Classifier.Recognition>();

                                        for (final Classifier.Recognition result : results) {
                                            final RectF location = result.getLocation();

                                            if (location != null && result.getConfidence() >= minimumConfidence) {
                                                canvas.drawRect(location, paint);
                                                cropToFrameTransform.mapRect(location);
                                                result.setLocation(location);
                                                mappedRecognitions.add(result);
                                                Button btn = (Button) findViewById(R.id.btnVoice);
                                                btn.setOnTouchListener(new View.OnTouchListener() {
                                                    public boolean onTouch(View v, MotionEvent event) {
                                                        switch (event.getAction()) {
                                                            case MotionEvent.ACTION_DOWN:
                                                                inputVoice();
                                                                return true;
                                                        }
                                                        return false;
                                                    }
                                                });

                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable(){
                                                    @Override
                                                    public void run() {
                                                        long downTime = SystemClock.uptimeMillis();
                                                        long eventTime = SystemClock.uptimeMillis();
                                                        MotionEvent down_event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 0, 0, 0);
                                                        (btn).dispatchTouchEvent(down_event);
                                                        down_event.recycle();

                                                    }
                                                },5000);
                                            }
                                            else if(location != null && result.getConfidence() < minimumConfidence){
                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable(){
                                                    @Override
                                                    public void run() {
                                                        Button btn = (Button) findViewById(R.id.btnVoice);
                                                        btn.setOnTouchListener(new View.OnTouchListener() {
                                                            @Override
                                                            public boolean onTouch(View v, MotionEvent event) {
                                                                return true;
                                                            }
                                                        });
                                                    }
                                                },1);
                                            }
                                        }
                                        tracker.trackResults(mappedRecognitions, currTimestamp);
                                        trackingOverlay.postInvalidate();
                                        computingDetection = false;
                                    }
                                });
                    }
                });
    }


    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API;
    }

    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        runInBackground(() -> detector.setUseNNAPI(isChecked));
    }

    @Override
    protected void setNumThreads(final int numThreads) {
        runInBackground(() -> detector.setNumThreads(numThreads));
    }
}
