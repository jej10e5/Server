package com.example.exploreclient;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

public class RepeatListener implements View.OnTouchListener {

    private Handler handler = new Handler();

    private int initialInterval;
    private final int normalInterval;
    private final View.OnClickListener clickListener;

    private Runnable handlerRunnable = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, normalInterval);
            clickListener.onClick(downView);
        }
    };

    private View downView;
    public RepeatListener(int initialInterval, int normalInterval,
                          View.OnClickListener clickListener) {
        if (clickListener == null)
            throw new IllegalArgumentException("null runnable");
        if (initialInterval < 0 || normalInterval < 0)
            throw new IllegalArgumentException("negative interval");

        this.initialInterval = initialInterval;
        this.normalInterval = normalInterval;
        this.clickListener = clickListener;
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handler.removeCallbacks(handlerRunnable);
                handler.postDelayed(handlerRunnable, initialInterval);
                downView = view;
                clickListener.onClick(view);
                break;
            case MotionEvent.ACTION_UP:
                handler.removeCallbacks(handlerRunnable);
                Message msg = Message.obtain();
                msg.what = SendThread.CMD_STOP;
                msg.obj = "S";
                SendThread.mHandler.sendMessage(msg);
                break;

            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(handlerRunnable);

                downView = null;
                break;

        }
        return false;
    }

}