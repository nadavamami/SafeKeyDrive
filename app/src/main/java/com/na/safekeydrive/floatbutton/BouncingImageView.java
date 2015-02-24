package com.na.safekeydrive.floatbutton;

import android.content.Context;
import android.util.AttributeSet;
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
        private static final int ANIMATION_STEPS = 1;
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
                      x = (float) Math.random() * (parentWidth - width);
                      y = (float) Math.random() * (parentHeight - height);
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

                getHandler().post(this);
                windowManager.updateViewLayout(that, params);

            that.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            getHandler().removeCallbacks(mRunnable);
                            windowManager.updateViewLayout(that, params);
                            return true;
                        case MotionEvent.ACTION_UP:
                            isClicked = false;
                            getHandler().post(mRunnable);
                            windowManager.updateViewLayout(that, params);
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
}

