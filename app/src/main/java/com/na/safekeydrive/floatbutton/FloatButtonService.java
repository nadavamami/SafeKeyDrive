package com.na.safekeydrive.floatbutton;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by Arthur on 2/20/2015.
 */
public class FloatButtonService extends Service {

    private WindowManager windowManager;
    private BouncingImageView chatHead;

    @Override public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        chatHead = new BouncingImageView(this);
        chatHead.setImageResource(android.R.drawable.sym_def_app_icon);

        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        chatHead.setDisplayHeight(size.y);
        chatHead.setDisplayWidth(size.x);


       final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        chatHead.setLayoutParams(params);
        chatHead.setWindowManager(windowManager);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;


        chatHead.mRunnable.run();
        windowManager.addView(chatHead, params);
        final MyCountDownTimer countDownTimer = MyCountDownTimer.get_mInstance().start();
        countDownTimer.setContext(getApplicationContext());


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatHead != null) windowManager.removeView(chatHead);
    }
}