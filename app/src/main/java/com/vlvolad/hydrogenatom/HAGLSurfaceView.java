/*
 * This is the source code of the Hydrogen Atom Orbitals app for Android.
 * It is licensed under the MIT License.
 *
 * Copyright (c) 2015-2018 Volodymyr Vovchenko.
 */

package com.vlvolad.hydrogenatom;

import android.content.Context;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by Volodymyr on 29.04.2015.
 */
public class HAGLSurfaceView extends GLSurfaceView {

    HAGLRenderer mRenderer;
    private ScaleGestureDetector mScaleDetector;
    private int count;
    public float mDensity;
    private Rect clickAxis;
    static int frequency = 50;
    boolean timerRunning;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            //Log.v("timerInertia", "timerInertia");
            if (!HAGLRenderer.mAtom.rotationFinished()) {
                timerHandler.postDelayed(this, frequency);
                return;
            }
            if (HAGLRenderer.mAtom.rotationFinished() && HAGLRenderer.mAtom.fin!=0) {
                // Render the view only when there is a change in the drawing data
                setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
            }
            timerHandler.removeCallbacks(timerRunnable);
            timerRunning = false;
        }
    };

    public HAGLSurfaceView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new HAGLRenderer();
        setRenderer(mRenderer);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        count = 0;

        clickAxis = new Rect(0, 0, 1, 1);


        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        timerRunning = false;
    }

    public HAGLSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new HAGLRenderer();
        setRenderer(mRenderer);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        count = 0;

        clickAxis = new Rect(0, 0, 1, 1);

        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mPreviousScale = false;

        timerRunning = false;
    }

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    private boolean mPreviousScale;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        clickAxis.left   = Math.round(0.8f    * mRenderer.Width );
        clickAxis.right  = Math.round(0.95f   * mRenderer.Width );
        clickAxis.top    = Math.round(0.02f   * mRenderer.Height);
        clickAxis.bottom = Math.round(0.10f   * mRenderer.Height);

        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();



        if (e.getPointerCount()<2)
        {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //Log.v("Touch down", "Rectangle: " + clickAxis.left + "<x<" + clickAxis.right
                    //        + " , " + clickAxis.top + "<y<" + clickAxis.bottom);
                    //Log.v("Touch down", "Touching down at (" + x + "," + y + ")" );
                    if(PreferenceManager.getDefaultSharedPreferences(HAGLActivity.getContextOfApplication()).getBoolean("pref_showaxes", true) &&  clickAxis.contains((int)e.getX(),(int)e.getY())){
                        HAGLRenderer.mAtom.camera_rot[0] = HAGLRenderer.mAtom.camera_rot_lag[0] = 0.f;
                        HAGLRenderer.mAtom.camera_rot[1] = HAGLRenderer.mAtom.camera_rot_lag[1] = 0.f;
                        Matrix.setIdentityM(HAGLRenderer.mAtom.mAccumulatedRotation, 0);
                        requestRender();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //Log.v("Touch up", "Up");
                    HAGLRenderer.mAtom.motion = false;
                    requestRender();
                    count = 0;
                    break;
                case MotionEvent.ACTION_MOVE:
                    //Log.v("Touch move", "Move");

                    count++;

                    float dx = x - mPreviousX;
                    float dy = y - mPreviousY;


                    if (!mPreviousScale) {
                        HAGLRenderer.mAtom.camera_rot[0] += dy / 10.f * 4.f / mDensity;
                        HAGLRenderer.mAtom.camera_rot[1] += -dx / 10.f * 4.f / mDensity * 2.0 * (y - getHeight()/2 + getHeight()/8) / (getHeight()/2);

                        // Some old experiments with rotation
                        //HAGLRenderer.mAtom.camera_rot[1] += -dx / 10.f * Math.cos(HAGLRenderer.mAtom.camera_rot[0] * Math.PI / 180.) * 1.5 * (y - getHeight()/2) / (getHeight()/2);
                        //HAGLRenderer.mAtom.camera_rot[2] += dx / 10.f * 4.f / mDensity * 2.0 * (7.f*getHeight()/8.f-Math.abs(y - getHeight()/2 + getHeight()/8)) / (getHeight()/2);

                        HAGLRenderer.mAtom.motion = true;
                        if (!timerRunning) {
                            //if (HAGLRenderer.mAtom.inertia != 1.0f) {
                            if (PreferenceManager.getDefaultSharedPreferences(
                                    HAGLActivity.getContextOfApplication()).getBoolean("pref_inertia", true)) {
                                setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                                timerHandler.postDelayed(timerRunnable, 0);
                                timerRunning = true;
                            }
                            else
                                requestRender();
                        }
                    }
                    else mPreviousScale = false;
            }
        }

        mPreviousX = x;
        mPreviousY = y;
        mScaleDetector.onTouchEvent(e);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = HAGLRenderer.mAtom.zoomIn;
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.20f, Math.min(mScaleFactor, 10.0f));

            HAGLRenderer.mAtom.zoomIn = mScaleFactor;
            HAGLRenderer.mAtom.motion = true;
            mPreviousScale = true;
//            HAGLRenderer.mAtom.camera_trans[2] += detector.getScaleFactor();
            requestRender();

            invalidate();
            return true;
        }
    }

}
