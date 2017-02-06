package su.moy.chernihiv;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;

public class ScalingTextureView extends TextureView {
    private static final String TAG = "ScalingTextureView";
    private boolean firstDoubleTap = true;
    private float scaleFactor = 1f;
    private static final float MIN_SCALE = 1f;
    private static final float MID_SCALE = 2f;
    private static final float MAX_SCALE = 2.5f;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private boolean scaleIsStart = false;
    private Context mContext;
    private OnScalingTextureViewTapDetectorListener onScalingTextureViewTapDetectorListener;

    private boolean isOneFingerDown = false;


    public ScalingTextureView(Context context) {
        super(context);
        initScalingSurface(context);
        mContext = context;
    }


    public ScalingTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScalingSurface(context);
        mContext = context;
    }


    public ScalingTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initScalingSurface(context);
        mContext = context;
    }

    public void setTapDetectorListener(OnScalingTextureViewTapDetectorListener listener) {
        onScalingTextureViewTapDetectorListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(TAG, "onSizeChanged");
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void set4to3Size() {
        if (getOrientation(mContext) == Configuration.ORIENTATION_LANDSCAPE) {
            setDimensions(this, (getScreenHeight() / 3) * 4, getScreenHeight());
        } else {
            setDimensions(this, getScreenWidth(), (getScreenWidth() / 4) * 3);
        }
    }

    private int getOrientation(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int orientation;
        if (display.getWidth() == display.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (display.getWidth() < display.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    private int getScreenHeight() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int height = display.getHeight();
        return height;
    }

    private int getScreenWidth() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth();
        return width;
    }

    private void setDimensions(View view, int width, int height) {
        android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

    private void initScalingSurface(Context context) {

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureListener());

        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                scaleGestureDetector.onTouchEvent(motionEvent);
                gestureDetector.onTouchEvent(motionEvent);

                //this detections use for scrolling
                if (motionEvent.getPointerCount() == 1) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            isOneFingerDown = true;

                            break;
                        case MotionEvent.ACTION_MOVE:

                            break;
                        case MotionEvent.ACTION_POINTER_DOWN: {
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                            isOneFingerDown = false;

                            break;
                        case MotionEvent.ACTION_CANCEL:
                            isOneFingerDown = false;
                            break;
                    }
                }
                return true;
            }
        });

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            Log.d(TAG, "onScaleBegin");
            scaleIsStart = true;
            float newX = detector.getFocusX();
            float newY = detector.getFocusY();
            scaleFactor = getScaleX();
            if (scaleFactor <= detector.getScaleFactor()) {
                setPivotX(newX);
                setPivotY(newY);
            }
            isOneFingerDown = false;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.d(TAG, "onScale");
            scaleFactor *= detector.getScaleFactor();
            //scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));
            scaleFactor = (scaleFactor < MIN_SCALE ? MIN_SCALE : scaleFactor);
            scaleFactor = (scaleFactor > MAX_SCALE ? MAX_SCALE : scaleFactor); // prevent our view from becoming too small //
            scaleFactor = ((float) ((int) (scaleFactor * 100))) / 100; // Change precision to help with jitter when user just rests their fingers //
            setScaleX(scaleFactor);
            setScaleY(scaleFactor);
            return false;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            scaleIsStart = false;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        // event when double tap occurs
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "onSingleTap. X = " + getScaleX() + ". Y = " + getScaleY());
            if (onScalingTextureViewTapDetectorListener != null && scaleFactor == MIN_SCALE) {
                onScalingTextureViewTapDetectorListener.onSingleTapOnTextureView();
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.d(TAG, "onDoubleTap.==> X = " + getScaleX() + ". Y = " + getScaleY());
            if (onScalingTextureViewTapDetectorListener != null) {
                onScalingTextureViewTapDetectorListener.onDoubleTapOnTextureView();
            }
            if (firstDoubleTap && getScaleX() <= MIN_SCALE && getScaleY() <= MIN_SCALE) {
                Log.d(TAG, "onDoubleTap.First");
                setPivotX(e.getX());
                setPivotY(e.getY());
                setScaleX(MID_SCALE);
                setScaleY(MID_SCALE);
                startZoomAnimation(MIN_SCALE, MID_SCALE - 0.9f);
                scaleFactor = MID_SCALE;
                firstDoubleTap = false;
            } else {
                Log.d(TAG, "onDoubleTap.Second");
                setPivotX(e.getX());
                setPivotY(e.getY());
                setScaleX(MIN_SCALE);
                setScaleY(MIN_SCALE);
                scaleFactor = MIN_SCALE;
                startZoomAnimation(MID_SCALE, MIN_SCALE + 0.3f);
                firstDoubleTap = true;
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!scaleIsStart && e1.getPointerCount() == 1 && e2.getPointerCount() == 1 && scaleFactor > MIN_SCALE && isOneFingerDown) {

                float delta;
                if (distanceX > 0) {

                    delta = getPivotX() + Math.abs(distanceX);//*kX;
                    if (delta > getWidth()) {
                        delta = getWidth();
                    }
                    if (delta < 0.0f) {
                        delta = 0;
                    }
                    setPivotX(delta);
                    Log.e(TAG, "pivotX = " + getPivotX());

                }
                if (distanceX < 0) {
                    delta = getPivotX() - Math.abs(distanceX);// * kX;
                    if (delta > getWidth()) {
                        delta = getWidth();
                    }
                    if (delta < 0.0f) {
                        delta = 0;
                    }
                    setPivotX(delta);
                    Log.e(TAG, "pivotX = " + getPivotX());

                }
                if (distanceY > 0) {
                    delta = getPivotY() + Math.abs(distanceY);// * kY;
                    if (delta > getHeight()) {
                        delta = getHeight();
                    }
                    if (delta < 0.0f) {
                        delta = 0;
                    }
                    setPivotY(delta);
                }
                if (distanceY < 0) {
                    delta = getPivotY() - Math.abs(distanceY);// * kY;
                    if (delta > getHeight()) {
                        delta = getHeight();
                    }
                    if (delta < 0.0f) {
                        delta = 0;
                    }
                    setPivotY(delta);
                }
            }
            return true;
        }
    }

    private void startZoomAnimation(float from, float to) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(from, to, from, to, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
        scaleAnimation.setDuration(500);
        scaleAnimation.setInterpolator(new OvershootInterpolator(6f));
        startAnimation(scaleAnimation);
    }


}
