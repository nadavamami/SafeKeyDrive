package com.na.safekeydrive.floatbutton;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by Arthur on 2/24/2015.
 */
public class BouncingImageView extends ImageView {

    private ViewParent mParent;
    private int displayHeight;
    private int displayWidth;
    private WindowManager.LayoutParams params;
    private WindowManager windowManager;
    private View that=this;
    private boolean isClicked = false;
    public static final String OVERRIDE = "com.na.safekeydrive.floatbutton.BouncingImageView";
    public static final String EXTRA = "Status";


    public BouncingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BouncingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BouncingImageView(Context context) {
        super(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mParent = (ViewParent) getParent();
        getHandler().post(mRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        getHandler().removeCallbacks(mRunnable);
        super.onDetachedFromWindow();
    }

    public final Runnable mRunnable = new Runnable() {
        private static final int DIRECTION_POSITIVE = 1;
        private static final int DIRECTION_NEGATIVE = -1;
        private static final int ANIMATION_STEPS = 200;
        private int mHorizontalDirection = DIRECTION_POSITIVE;
        private int mVerticalDirection = DIRECTION_NEGATIVE;
        private boolean isClicked = false;
        public boolean mStarted = false;

        @Override
        public void run() {
            if (mParent == null) {
                return;
            }

            final float width = getMeasuredWidth();
            final float height = getMeasuredHeight()/2;
            final float parentWidth = displayWidth;
            final float parentHeight =displayHeight/2;
            float x = getX();
            float y = getY();
                  if (!mStarted) {
                      /***
                       * Randomize initial position
                       */
                      x = 0;//(float) Math.random() * (parentWidth - width);
                      y = 0;//(float) Math.random() * (parentHeight - height);
                      mHorizontalDirection = ((int) x % 2 == 0) ? DIRECTION_NEGATIVE : DIRECTION_POSITIVE;
                      mVerticalDirection = ((int) y % 2 == 0) ? DIRECTION_NEGATIVE : DIRECTION_POSITIVE;
                      mStarted = true;
                  } else {
                      if (mHorizontalDirection == DIRECTION_NEGATIVE) {
                          x -= ANIMATION_STEPS;
                      } else {
                          x += ANIMATION_STEPS;
                      }

                      if (mVerticalDirection == DIRECTION_NEGATIVE) {
                          y -= ANIMATION_STEPS;
                      } else {
                          y += ANIMATION_STEPS;
                      }

                      if (x - (width / 3) < 0) {
                          mHorizontalDirection = DIRECTION_POSITIVE;
                      } else if (x + (width / 3) > (parentWidth - width)) {
                          mHorizontalDirection = DIRECTION_NEGATIVE;
                      }

                      if (y - (height / 3) < 0) {
                          mVerticalDirection = DIRECTION_POSITIVE;
                      } else if (y + (width / 3) > (parentHeight - height)) {
                          mVerticalDirection = DIRECTION_NEGATIVE;
                      }
                  }
                  setX(x);
                  setY(y);

                params.x = (int) x;
                params.y = (int) y;
                final Runnable thatis = this;


            /*{

                public void onTick(long millisUntilFinished) {
                    Toast.makeText(getContext(),String.valueOf(millisUntilFinished),Toast.LENGTH_SHORT).show();

                }

                public void onFinish() {
                    Toast.makeText(getContext(),"TURNHIMOFFF",Toast.LENGTH_SHORT).show();
                    *//*  Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent.setAction("com.na.safekeydrive.floatbutton.BouncingImageView");
                    intent.putExtra("Status", "Clicked");        *//*
                }
            }.start();*/
//                getHandler().postDelayed(this, 2000);
                windowManager.updateViewLayout(that, params);

            boolean isReset = false;
         /*   TimerTask task=new TimerTask(){
                long elapsed = 0L;

                @Override
                public void run() {
                    elapsed+=1000;
                    if(elapsed>=5000){
//                        this.cancel();
                        //Toast.makeText(getContext(), "DUBI LO BUBAAAAA", Toast.LENGTH_SHORT);
                        return;
                    }
                    //if(some other conditions)
                    //   this.cancel();
//                    displayText("seconds elapsed: " + elapsed / 1000);
                }
            };
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(task, 1000, 1000);*/

            that.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            setClicked(true);
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                            intent.setAction(OVERRIDE);
                            intent.putExtra(EXTRA, true);
                            getContext().sendBroadcast(intent);
                           // windowManager.removeView(that);
                            //getHandler().postDelayed(thatis, 1000);
                          //  windowManager.addView(that, params);
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            //getHandler().removeCallbacks(mRunnable);
                            that.setVisibility(View.GONE);
                            invalidate();
//                            ((WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE)).removeView(that);
//
//                            ((WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE)).addView(that, params);
//                            windowManager.updateViewLayout(that, params);
//                            MyCountDownTimer countDownTimer = MyCountDownTimer.get_mInstance();
//                            countDownTimer.cancel();
                            new CountDownTimer(3000,1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {

                                }

                                @Override
                                public void onFinish() {
                                    that.setVisibility(VISIBLE);
                                    setClicked(false);
//                                    that.setVisibility(View.VISIBLE);
//                                    ((WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE)).removeView(that);
//
//                                    ((WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE)).addView(that, params);
//
//                                    windowManager.updateViewLayout(that, params);
                                    invalidate();
//                                    mTimer.cancel();
//                                    mTimer.start();
                                    startOverrideTimer();
                                }
                            }.start();
//                            countDownTimer.restart();
//                            SystemClock.sleep(3000);
//
//                            countDownTimer.start();
//
//                            that.setVisibility(View.VISIBLE);
//                            ((WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE)).removeView(that);
//
//                            ((WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE)).addView(that, params);
//
//                            windowManager.updateViewLayout(that, params);
//                            invalidate();
                            return true;
                        case MotionEvent.ACTION_UP:
                            isClicked = false;
                            //getHandler().post(mRunnable);
                            //windowManager.updateViewLayout(that, params);
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            isClicked = false;
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            isClicked = false;
                            // windowManager.updateViewLayout(that, params);
                            return true;
                    }
                    return false;
                }
            });
        }

    };


    public void setDisplayHeight(int displayHeight) {
        this.displayHeight = displayHeight;
    }

    public void setDisplayWidth(int displayWidth) {
        this.displayWidth = displayWidth;
    }

    public void setLayoutParams(WindowManager.LayoutParams params) {
        this.params = params;
    }

    public void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    private void startOverrideTimer(){
        new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (getClicked()){
                    cancel();
                }
            }

            @Override
            public void onFinish() {
                if (!getClicked()){
                    Log.i("override timer","time is up");
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent.setAction(OVERRIDE);
                    intent.putExtra(EXTRA, false);
                    isClicked = false;
                    getContext().sendBroadcast(intent);
                }
            }
        }.start();
    }

    private synchronized void setClicked(boolean clicked){
        isClicked = clicked;
    }

    private synchronized boolean getClicked(){
        return isClicked;
    }
}

