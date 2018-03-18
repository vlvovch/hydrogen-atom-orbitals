/*
 * This is the source code of the Hydrogen Atom Orbitals app for Android.
 * It is licensed under the MIT License.
 *
 * Copyright (c) 2015-2018 Volodymyr Vovchenko.
 */

package com.vlvolad.hydrogenatom;

import android.app.ActivityManager;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Volodymyr on 28.04.2015.
 */
class GLvector
{
    float fX;
    float fY;
    float fZ;
}

public class HydrogenAtomRender {
    int Width, Height;
    double scale;

    boolean motion;
    boolean animatemode;
    boolean dlistgen;

    int ncubes, ncubesanim;
    boolean pause;

    volatile int n,l,m;				// quantum numbers
    volatile boolean drawsign;
    volatile boolean drawrks;					// real or complex wave function basis
    volatile int fin;                   // is generation done

    volatile boolean overflow;
    boolean candraw;

    double avt1;


    int size1, size2;
    volatile FloatBuffer trivertex;
    volatile FloatBuffer trinormal;
    FloatBuffer tricolor1, tricolor2;
    volatile FloatBuffer trivertexanim;
    volatile FloatBuffer trinormalanim;
    FloatBuffer tricoloranim1, tricoloranim2;

    FloatBuffer trivertexf;
    FloatBuffer trinormalf;
    FloatBuffer tricolorf;
    FloatBuffer trivertexanimf;
    FloatBuffer trinormalanimf;
    FloatBuffer tricoloranimf;

    FloatBuffer lineVertexBuffer;

    // how many triangles already generated
    volatile int trcnt, trcntanim;
    boolean anim, rounderror;
    volatile boolean stopThread;

    public volatile boolean toCont;

    volatile float   fStepSize;
    float fStepSize1, fStepSizeanim;
    float   fTargetValue;
    volatile double  pct;

    float [] valtable;
    int maxinda;

    HydrogenAtomMath hAtom;

    // zoom and rotation
    int ox, oy;
    public volatile float camera_trans[];
    public volatile float camera_rot[];
    public volatile float camera_trans_lag[];
    public volatile float camera_rot_lag[];
    public volatile float zoomIn;
    public volatile float inertia;
    int buttonState;
    int memoryclass;

    public int mProgram;

    public final float[] mMVPMatrix = new float[16];
    public final float[] mProjMatrix = new float[16];
    public final float[] mVMatrix = new float[16];

    public volatile float[] mAccumulatedRotation;
    public volatile float[] mCurrentRotation;

    private FloatBuffer lightDir, lightHP, lightAC, lightDC, lightSC;
    private FloatBuffer materialAF, materialDF, materialSF;
    float materialshin;

    public volatile int progress, totalprogress;

    int index_table(int ix, int iy, int iz)
    {
        return maxinda*maxinda*ix + maxinda*iy + iz;
    }

    public static Random generator = new Random();

    public HydrogenAtomRender() {
        scale = 1.;
        motion = false;
        pause = false;
        n = 8;
        l = 5;
        m = 2;
        fin = 0;
        ox = 0;
        oy = 0;
        buttonState = 0;
        candraw = true;
        trcnt = 0;
        trcntanim = 0;
        anim = false;
        rounderror = false;
        fStepSize = 6.f;
        fStepSizeanim = 8.f;
        fTargetValue = 0.00172655f;
        pct = 70;
        ncubes = 0;
        ncubesanim = 0;
        overflow = false;
        animatemode = true;
        avt1 = 0.;
        dlistgen = false;
        toCont = false;

        hAtom = new HydrogenAtomMath();

        drawrks = false;
        hAtom.realksi = false;

        lightDir = fill3DVector(0.f, 0.f, 1.0f);
        lightHP = fill3DVector(0.f, 0.f, 1.0f);
        lightAC = fill4DVector(0.3f, 0.3f, 0.3f, 1.0f);
        lightDC = fill4DVector(1.0f, 1.0f, 1.0f, 1.0f);
        lightSC = fill4DVector(1.0f, 1.0f, 1.0f, 1.0f);
        materialAF = fill4DVector(0.2f, 0.2f, 0.2f, 1.0f);
        materialDF = fill4DVector(0.8f, 0.8f, 0.8f, 1.0f);
        materialSF = fill4DVector(1.0f, 1.0f, 1.0f, 1.0f);
        materialshin = 40.0f;

        camera_trans = new float[3];
        camera_rot = new float[3];
        camera_trans_lag = new float[3];
        camera_rot_lag = new float[3];
        camera_trans[0] = camera_trans[1] = camera_trans[2] = 0;
        camera_rot[0] = camera_rot[1] = camera_rot[2] = 0;
        camera_trans_lag[0] = camera_trans_lag[1] = camera_trans_lag[2] = 0;
        camera_rot_lag[0] = camera_rot_lag[1] = camera_rot_lag[2] = 0;

        //inertia = 0.2f;
        inertia = 1.0f;

        zoomIn = 0.85f;
        //zoomIn = 1.0f;

        pickRandomOrbital(5);

        stopThread = false;

        mAccumulatedRotation = new float[16];
        Matrix.setIdentityM(mAccumulatedRotation, 0);
        mCurrentRotation = new float[16];
        Matrix.setIdentityM(mCurrentRotation, 0);


        // For rotating axes
        ByteBuffer vbb = ByteBuffer.allocateDirect(18 * 4);
        vbb.order(ByteOrder.nativeOrder());
        lineVertexBuffer = vbb.asFloatBuffer();
        // x axis
        lineVertexBuffer.put(0.f);
        lineVertexBuffer.put(0.f);
        lineVertexBuffer.put(0.f);
        lineVertexBuffer.put(1.f);
        lineVertexBuffer.put(0.f);
        lineVertexBuffer.put(0.f);
        // y axis
        lineVertexBuffer.put(0.f);
        lineVertexBuffer.put(0.f);
        lineVertexBuffer.put(0.f);
        lineVertexBuffer.put(0.f);
        lineVertexBuffer.put(1.f);
        lineVertexBuffer.put(0.f);
        // z axis
        lineVertexBuffer.put(0.f);
        lineVertexBuffer.put(0.f);
        lineVertexBuffer.put(0.f);
        lineVertexBuffer.put(0.f);
        lineVertexBuffer.put(0.f);
        lineVertexBuffer.put(1.f);
    }

    void pickRandomOrbital(int nmax) {
        n = generator.nextInt(nmax) + 1;
        l = generator.nextInt(n);
        m = -l + generator.nextInt(2*l+1);
    }

    void reallocateMemory() {
//        size1 = 600000;
//        size2 = 1000000;
        size1 = 300000;
        size2 = 800000;

        if (memoryclass<=64) {
            size1 /= 2;
            size2 /= 2;
        }

        if (memoryclass<=32) {
            size1 /= 2;
            size2 /= 2;
        }

        if (memoryclass<=16) {
            size1 /= 2;
            size2 /= 2;
        }

        trivertex = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();

        trinormal = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();

        tricolor1 = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();

        tricolor2 = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();

        trivertexanim = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();

        trinormalanim = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();

        tricoloranim1 = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();

        tricoloranim2 = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size1 * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();
    }

    FloatBuffer trimFB(FloatBuffer in, int size) {
        FloatBuffer ret = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();
        float[] arr = new float[size * 3];
        in.position(0);
        in.get(arr, 0, size * 3);
        //System.arraycopy(in.get(), 0, arr, 0, trcnt * 3 * 3);
        ret.put(arr);
        return ret;
    }

    FloatBuffer expandFB(FloatBuffer in, int size, int size2) {
        FloatBuffer ret = ByteBuffer.allocateDirect(
                // (number of coordinate values * 4 bytes per float)
                size * 3 * 4).
                order(ByteOrder.nativeOrder()).
                asFloatBuffer();
        float[] arr = new float[size * 3];
        in.position(0);
        in.get(arr, 0, size2 * 3);
        //System.arraycopy(in.get(), 0, arr, 0, trcnt * 3 * 3);
        ret.put(arr);
        return ret;
    }

    void reallocateMemoryFinal() {
        trivertex = trimFB(trivertex, trcnt*3);
        trivertex.position(0);

        trinormal = trimFB(trinormal, trcnt*3);
        trinormal.position(0);

        tricolor1 = trimFB(tricolor1, trcnt*3);
        tricolor1.position(0);

        tricolor2 = trimFB(tricolor2, trcnt*3);
        tricolor2.position(0);

        trivertexanim = trimFB(trivertexanim, trcnt * 3);
        trivertexanim.position(0);

        trinormalanim = trimFB(trinormalanim, trcnt * 3);
        trinormalanim.position(0);

        tricoloranim1 = trimFB(tricoloranim1, trcnt*3);
        tricoloranim1.position(0);

        tricoloranim2 = trimFB(tricoloranim2, trcnt*3);
        tricoloranim2.position(0);
    }

    FloatBuffer fill3DVector(float x, float y, float z)
    {
        FloatBuffer buf;
        ByteBuffer vbb = ByteBuffer.allocateDirect(3 * 4);
        vbb.order(ByteOrder.nativeOrder());
        buf = vbb.asFloatBuffer();
        buf.put(x);
        buf.put(y);
        buf.put(z);
        return buf;
    }

    FloatBuffer fill4DVector(float x, float y, float z, float w)
    {
        FloatBuffer buf;
        ByteBuffer vbb = ByteBuffer.allocateDirect(4 * 4);
        vbb.order(ByteOrder.nativeOrder());
        buf = vbb.asFloatBuffer();
        buf.put(x);
        buf.put(y);
        buf.put(z);
        buf.put(w);
        return buf;
    }

    public boolean rotationFinished() {
        boolean ret = true;
        for (int k = 0; k < 3; ++k)
        {
            if (Math.abs(camera_rot_lag[k]-camera_rot[k])>1.e0f)
                ret = false;
            //Log.v("rotation", k + " "+  Math.abs(camera_rot_lag[k]-camera_rot[k]));
        }
        if (ret) {
            for (int k = 0; k < 3; ++k)
                camera_rot_lag[k] = camera_rot[k];
        }
        return ret;
    }

    public void drawAxes(GL10 unused, int Width, int Height)
    {
        HAGLRenderer.perspectiveGL(mProjMatrix, 45.0f,(float)(Width)/Height,10.0f,4000.0f);
        Matrix.setIdentityM(mVMatrix, 0);

        float xymove = 5.5f;
        Matrix.translateM(mVMatrix, 0, xymove * (float)(Width)/Height, 0.9f*xymove, -20.f);


        {
            float[] mTemporaryMatrix = new float[16];

            // Rotate the cube taking the overall rotation into account.
            Matrix.multiplyMM(mTemporaryMatrix, 0, mVMatrix, 0, mAccumulatedRotation, 0);
            System.arraycopy(mTemporaryMatrix, 0, mVMatrix, 0, 16);
        }

        float mn = 1.f;

        Matrix.scaleM(mVMatrix, 0, mn, mn, mn);

        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        int tHandle = GLES20.glGetUniformLocation(mProgram, "u_mvpMatrix");
        GLES20.glUniformMatrix4fv(tHandle, 1, false, mMVPMatrix, 0);
        tHandle = GLES20.glGetUniformLocation(mProgram, "u_mvMatrix");
        GLES20.glUniformMatrix4fv(tHandle, 1, false, mVMatrix, 0);

        int tHandleColor = GLES20.glGetUniformLocation(mProgram, "color");
        GLES20.glUniform4f(tHandleColor, 1.0f, 1.0f, 1.0f, 1.0f);

        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // x axis
        lineVertexBuffer.position(12);
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, lineVertexBuffer);
        GLES20.glUniform4f(tHandleColor, 1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

        // y axis
        lineVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, lineVertexBuffer);
        GLES20.glUniform4f(tHandleColor, 0.0f, 1.0f, 0.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);

        // z axis
        lineVertexBuffer.position(6);
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, lineVertexBuffer);
        GLES20.glUniform4f(tHandleColor, 0.0f, 0.0f, 1.0f, 1.0f);
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, 2);
    }

    // surface drawing
    public void draw(GL10 unused, int Width, int Height)
    {
//        Log.d("Draw", "Start");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        HAGLRenderer.perspectiveGL(mProjMatrix, 45.0f,(float)(Width)/Height,10.0f,4000.0f);
        Matrix.setIdentityM(mVMatrix, 0);
        int tHandle = GLES20.glGetUniformLocation(mProgram, "light");
        GLES20.glUniform1i(tHandle, 1);
        tHandle = GLES20.glGetUniformLocation(mProgram, "color");
        GLES20.glUniform4f(tHandle, 1.0f, 0.0f, 0.0f, 1.0f);
        tHandle = GLES20.glGetUniformLocation(mProgram, "trajectory");
        GLES20.glUniform1i(tHandle, 1);

        tHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
        GLES20.glEnableVertexAttribArray(tHandle);

        tHandle = GLES20.glGetUniformLocation(mProgram, "u_directionalLight.direction");
        lightDir.position(0);
        GLES20.glUniform3fv(tHandle, 1, lightDir);
        tHandle = GLES20.glGetUniformLocation(mProgram, "u_directionalLight.halfplane");
        lightHP.position(0);
        GLES20.glUniform3fv(tHandle, 1, lightHP);
        tHandle = GLES20.glGetUniformLocation(mProgram, "u_directionalLight.ambientColor");
        lightAC.position(0);
        GLES20.glUniform4fv(tHandle, 1, lightAC);
        tHandle = GLES20.glGetUniformLocation(mProgram, "u_directionalLight.diffuseColor");
        lightDC.position(0);
        GLES20.glUniform4fv(tHandle, 1, lightDC);
        tHandle = GLES20.glGetUniformLocation(mProgram, "u_directionalLight.specularColor");
        lightSC.position(0);
        GLES20.glUniform4fv(tHandle, 1, lightSC);

        tHandle = GLES20.glGetUniformLocation(mProgram, "u_material.shininess");
        GLES20.glUniform1f(tHandle, materialshin);
        tHandle = GLES20.glGetUniformLocation(mProgram, "u_material.specularFactor");
        materialSF.position(0);
        GLES20.glUniform4fv(tHandle, 1, materialSF);

        Matrix.translateM(mVMatrix, 0, 0, 0, -1000 / 2 / zoomIn);

        float dx = camera_rot[0] - camera_rot_lag[0];
        float dy = camera_rot[1] - camera_rot_lag[1];
        float dz = camera_rot[2] - camera_rot_lag[2];

        dx = (camera_rot[0] - camera_rot_lag[0])  * inertia;
        dy = (camera_rot[1] - camera_rot_lag[1])  * inertia;
        dz = (camera_rot[2] - camera_rot_lag[2])  * inertia;

        for (int k = 0; k < 3; ++k)
        {
            camera_trans_lag[k] += (camera_trans[k] - camera_trans_lag[k]) * inertia;
            camera_rot_lag[k] += (camera_rot[k] - camera_rot_lag[k])  * inertia;
        }
        Matrix.translateM(mVMatrix, 0, camera_trans_lag[0],
                camera_trans_lag[1],
                camera_trans_lag[2]);


        {
            Matrix.setIdentityM(mCurrentRotation, 0);
            Matrix.rotateM(mCurrentRotation, 0, dx, 1.0f, 0.0f, 0.0f);
            Matrix.rotateM(mCurrentRotation, 0, dy, 0.0f, 0.0f, -1.0f);
            //Matrix.rotateM(mCurrentRotation, 0, dz, 0.0f, 1.0f, 0.0f);

            float[] mTemporaryMatrix = new float[16];
            // Multiply the current rotation by the accumulated rotation, and then set the accumulated
            // rotation to the result.
            Matrix.multiplyMM(mTemporaryMatrix, 0, mCurrentRotation, 0, mAccumulatedRotation, 0);
            System.arraycopy(mTemporaryMatrix, 0, mAccumulatedRotation, 0, 16);

            // Rotate the cube taking the overall rotation into account.
            Matrix.multiplyMM(mTemporaryMatrix, 0, mVMatrix, 0, mAccumulatedRotation, 0);
            System.arraycopy(mTemporaryMatrix, 0, mVMatrix, 0, 16);
        }

        trivertex.position(0);
        trinormal.position(0);
        trivertexanim.position(0);
        trinormalanim.position(0);

        dlistgen = true;

        if (fin>0 && !dlistgen)
        {
            if (!animatemode)
            {
                dlistgen = true;
            }
            fin = 2;
        }

        if (fin>0 && (motion || !rotationFinished()) && animatemode) {
//            Log.d("Draw", "Animate");
            int inv = 0;
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
            GLES20.glVertexAttribPointer(tHandle, 3,
                    GLES20.GL_FLOAT, false,
                    0, trivertexanim);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_normal");
            GLES20.glEnableVertexAttribArray(tHandle);
            GLES20.glVertexAttribPointer(tHandle, 3,
                    GLES20.GL_FLOAT, false,
                    0, trinormalanim);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
            GLES20.glEnableVertexAttribArray(tHandle);
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
            for (int rotX = 0; rotX < 2; rotX++)
                for (int rotY = 0; rotY < 2; rotY++)
                    for (int rotZ = 0; rotZ < 2; rotZ++) {
                        Matrix.scaleM(mVMatrix, 0, 1.0f - 2.0f * rotX, 1.0f - 2.0f * rotY, 1.0f - 2.0f * rotZ);  // symmetry wrt to coordinate inversion

                        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

                        tHandle = GLES20.glGetUniformLocation(mProgram, "u_mvpMatrix");
                        GLES20.glUniformMatrix4fv(tHandle, 1, false, mMVPMatrix, 0);
                        tHandle = GLES20.glGetUniformLocation(mProgram, "u_mvMatrix");
                        GLES20.glUniformMatrix4fv(tHandle, 1, false, mVMatrix, 0);

                        if (rotY > 0 && (((l - m) & 1)) > 0)
                            inv = 1; //wave function changes sign wrt to z inversion when l-|m| is odd
                        else inv = 0;
                        if (drawrks && m != 0)   // for real basis sign can change also when x or y are inverted
                        {
                            if (rotZ > 0 && rotX == 0 && (m % 2 == 0) && !drawsign) inv++;
                            if (rotZ > 0 && rotX == 0 && (m & 1) > 0 && drawsign) inv++;
                            if (rotX > 0 && rotZ == 0 && !drawsign) inv++;
                            if (rotX > 0 && rotZ > 0 && (m & 1) > 0) inv++;
                        }

                        tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
                        if (inv % 2 > 0) GLES20.glVertexAttribPointer(tHandle, 3,
                                GLES20.GL_FLOAT, false,
                                0, tricoloranim2);
                        else GLES20.glVertexAttribPointer(tHandle, 3,
                                GLES20.GL_FLOAT, false,
                                0, tricoloranim1);

                        if (((rotX + rotY + rotZ) % 2) > 0)
                            GLES20.glFrontFace(GLES20.GL_CW);  //odd number of inversions changes vertices traversing order
                        else GLES20.glFrontFace(GLES20.GL_CCW);

                        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3 * trcntanim);
                        Matrix.scaleM(mVMatrix, 0, 1.0f / (1.0f - 2.0f * rotX), 1.0f / (1.0f - 2.0f * rotY), 1.0f / (1.0f - 2.0f * rotZ));
                    }
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_normal");
            GLES20.glDisableVertexAttribArray(tHandle);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
            GLES20.glDisableVertexAttribArray(tHandle);
        }
        else
        {
//            Log.d("Draw", "Normal");
            int inv = 0;
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
            GLES20.glVertexAttribPointer(tHandle, 3,
                    GLES20.GL_FLOAT, false,
                    0, trivertex);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_normal");
            GLES20.glEnableVertexAttribArray(tHandle);
            GLES20.glVertexAttribPointer(tHandle, 3,
                    GLES20.GL_FLOAT, false,
                    0, trinormal);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
            GLES20.glEnableVertexAttribArray(tHandle);
            for(int rotX = 0; rotX < 2; rotX++)
                for(int rotY = 0; rotY < 2; rotY++)
                    for(int rotZ = 0; rotZ < 2; rotZ++)
                    {
                        Matrix.scaleM(mVMatrix, 0, 1.0f-2.0f*rotX, 1.0f-2.0f*rotY, 1.0f-2.0f*rotZ);

                        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

                        tHandle = GLES20.glGetUniformLocation(mProgram, "u_mvpMatrix");
                        GLES20.glUniformMatrix4fv(tHandle, 1, false, mMVPMatrix, 0);
                        tHandle = GLES20.glGetUniformLocation(mProgram, "u_mvMatrix");
                        GLES20.glUniformMatrix4fv(tHandle, 1, false, mVMatrix, 0);

                        if (rotY>0 && (((l-m) & 1))>0) inv = 1;
                        else inv = 0;
                        if (drawrks && m!=0)
                        {
                            if (rotZ>0 && rotX==0 && (m%2==0) && !drawsign) inv++;
                            if (rotZ>0 && rotX==0 && (m & 1)>0 && drawsign) inv++;
                            if (rotX>0 && rotZ==0 && !drawsign) inv++;
                            if (rotX>0 && rotZ>0 && (m & 1)>0) inv++;
                        }

                        tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
                        if (inv%2>0) GLES20.glVertexAttribPointer(tHandle, 3,
                                GLES20.GL_FLOAT, false,
                                0, tricolor2);  //выбор цвета
                        else GLES20.glVertexAttribPointer(tHandle, 3,
                                GLES20.GL_FLOAT, false,
                                0, tricolor1);

                        if (((rotX+rotY+rotZ)%2)>0) GLES20.glFrontFace(GLES20.GL_CW);
                        else GLES20.glFrontFace(GLES20.GL_CCW);

//                        Log.d("Draw", "trcnt = " + trcnt);
                        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3*trcnt);
                        Matrix.scaleM(mVMatrix, 0, 1.0f/(1.0f-2.0f*rotX), 1.0f/(1.0f-2.0f*rotY), 1.0f/(1.0f-2.0f*rotZ));
                    }
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_normal");
            GLES20.glDisableVertexAttribArray(tHandle);
            tHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
            GLES20.glDisableVertexAttribArray(tHandle);
        }

        tHandle = GLES20.glGetUniformLocation(mProgram, "light");
        GLES20.glUniform1i(tHandle, 0);
    }

    float fSample(float fX, float fY, float fZ)
    {
        double fResult = hAtom.ksi(fZ*avt1/100,fX*avt1/100,fY*avt1/100,n,l,m);
        return (float) (fResult*fResult);
    }

    //fGetOffset finds the approximate point of intersection of the surface
// between two points with the values fValue1 and fValue2
    float fGetOffset(float fValue1, float fValue2, float fValueDesired)
    {
        double fDelta = fValue2 - fValue1;

        if(fDelta == 0.0)
        {
            return 0.5f;
        }
        return (float)((fValueDesired - fValue1)/fDelta);
    }

    //vGetColor generates a color from a given position and normal of a point
    void vGetColor(GLvector rfColor, GLvector rfPosition, GLvector rfNormal)
    {
        float fX = rfNormal.fX;
        float fY = rfNormal.fY;
        float fZ = rfNormal.fZ;
        rfColor.fX = (fX > 0.0f ? fX : 0.0f) + (fY < 0.0f ? -0.5f*fY : 0.0f) + (fZ < 0.0f ? -0.5f*fZ : 0.0f);
        rfColor.fY = (fY > 0.0f ? fY : 0.0f) + (fZ < 0.0f ? -0.5f*fZ : 0.0f) + (fX < 0.0f ? -0.5f*fX : 0.0f);
        rfColor.fZ = (fZ > 0.0f ? fZ : 0.0f) + (fX < 0.0f ? -0.5f*fX : 0.0f) + (fY < 0.0f ? -0.5f*fY : 0.0f);
    }

    void vNormalizeVector(GLvector rfVectorResult, GLvector rfVectorSource)
    {
        float fOldLength;
        float fScale;

        fOldLength = (float)Math.sqrt( (rfVectorSource.fX * rfVectorSource.fX) +
                (rfVectorSource.fY * rfVectorSource.fY) +
                (rfVectorSource.fZ * rfVectorSource.fZ) );

        if(fOldLength == 0.0)
        {
            rfVectorResult.fX = rfVectorSource.fX;
            rfVectorResult.fY = rfVectorSource.fY;
            rfVectorResult.fZ = rfVectorSource.fZ;
        }
        else
        {
            fScale = 1.0f/fOldLength;
            rfVectorResult.fX = rfVectorSource.fX*fScale;
            rfVectorResult.fY = rfVectorSource.fY*fScale;
            rfVectorResult.fZ = rfVectorSource.fZ*fScale;
        }
    }

    void vGetNormal(GLvector rfNormal, float fX, float fY, float fZ)
    {
        rfNormal.fX = fSample(fX-0.01f, fY, fZ) - fSample(fX+0.01f, fY, fZ);
        rfNormal.fY = fSample(fX, fY-0.01f, fZ) - fSample(fX, fY+0.01f, fZ);
        rfNormal.fZ = fSample(fX, fY, fZ-0.01f) - fSample(fX, fY, fZ+0.01f);
        vNormalizeVector(rfNormal, rfNormal);
    }

    void vMarchCube(int ix, int iy, int iz, float fScale)
    {

        int iCorner, iVertex, iVertexTest, iEdge, iTriangle, iFlagIndex, iEdgeFlags;
        float fOffset;
        GLvector sColor;
        float afCubeValue[] = new float[8];
        GLvector asEdgeVertex[] = new GLvector[12];
        GLvector asEdgeNorm[] = new GLvector[12];
        for(int i=0;i<12;++i)
        {
            asEdgeVertex[i] = new GLvector();
            asEdgeNorm[i] = new GLvector();
        }
        float fX, fY, fZ;
        fX = -250.0f + (ix)*fScale;
        fY = -250.0f + (iy)*fScale;
        fZ = -250.0f + (iz)*fScale;

        //Make a local copy of the values at the cube's corners
        for(iVertex = 0; iVertex < 8; iVertex++)
        {
            afCubeValue[iVertex] = valtable[index_table(ix+(int)MarchingCubes.a2fVertexOffset[iVertex][0],iy+(int)MarchingCubes.a2fVertexOffset[iVertex][1],iz+(int)MarchingCubes.a2fVertexOffset[iVertex][2])];
        }

        //Find which vertices are inside of the surface and which are outside
        iFlagIndex = 0;
        for(iVertexTest = 0; iVertexTest < 8; iVertexTest++)
        {
            if(afCubeValue[iVertexTest] <= fTargetValue)
                iFlagIndex |= 1<<iVertexTest;
        }

        //Find which edges are intersected by the surface
        iEdgeFlags = MarchingCubes.aiCubeEdgeFlags[iFlagIndex];

        //If the cube is entirely inside or outside of the surface, then there will be no intersections
        if(iEdgeFlags == 0)
        {
            return;
        }

        //Find the point of intersection of the surface with each edge
        //Then find the normal to the surface at those points
        for(iEdge = 0; iEdge < 12; iEdge++)
        {
            //if there is an intersection on this edge
            if ((iEdgeFlags & (1<<iEdge))!=0)
            {
                fOffset = fGetOffset(afCubeValue[ MarchingCubes.a2iEdgeConnection[iEdge][0] ],
                        afCubeValue[ MarchingCubes.a2iEdgeConnection[iEdge][1] ], fTargetValue);

                asEdgeVertex[iEdge].fX = fX + (MarchingCubes.a2fVertexOffset[ MarchingCubes.a2iEdgeConnection[iEdge][0] ][0]  +  fOffset * MarchingCubes.a2fEdgeDirection[iEdge][0]) * fScale;
                asEdgeVertex[iEdge].fY = fY + (MarchingCubes.a2fVertexOffset[ MarchingCubes.a2iEdgeConnection[iEdge][0] ][1]  +  fOffset * MarchingCubes.a2fEdgeDirection[iEdge][1]) * fScale;
                asEdgeVertex[iEdge].fZ = fZ + (MarchingCubes.a2fVertexOffset[ MarchingCubes.a2iEdgeConnection[iEdge][0] ][2]  +  fOffset * MarchingCubes.a2fEdgeDirection[iEdge][2]) * fScale;

                vGetNormal(asEdgeNorm[iEdge], asEdgeVertex[iEdge].fX, asEdgeVertex[iEdge].fY, asEdgeVertex[iEdge].fZ);
            }
        }


        //Draw the triangles that were found.  There can be up to five per cube
        boolean fl = true;
        if (!anim)
        {
            for(iTriangle = 0; iTriangle < 5 && fl; iTriangle++)
            {
                if(MarchingCubes.a2iTriangleConnectionTable[iFlagIndex][3*iTriangle] < 0)
                    break;

                if ((9*trcnt + 9)>=size1) {
                    fl = false;
                    overflow = true;
                    break;
                }

                for(iCorner = 0; iCorner < 3; iCorner++)
                {
                    iVertex = MarchingCubes.a2iTriangleConnectionTable[iFlagIndex][3*iTriangle+iCorner];

                    trivertex.put(9*trcnt + 3*iCorner, asEdgeVertex[iVertex].fX);
                    trivertex.put(9*trcnt + 3*iCorner + 1, asEdgeVertex[iVertex].fY);
                    trivertex.put(9*trcnt + 3*iCorner + 2, asEdgeVertex[iVertex].fZ);
                    trinormal.put(9*trcnt + 3*iCorner, asEdgeNorm[iVertex].fX);
                    trinormal.put(9*trcnt + 3*iCorner + 1, asEdgeNorm[iVertex].fY);
                    trinormal.put(9*trcnt + 3*iCorner + 2, asEdgeNorm[iVertex].fZ);

                    if (hAtom.ksi(trivertex.get(9*trcnt + 3*iCorner + 2)*avt1/100,
                            trivertex.get(9*trcnt + 3*iCorner)*avt1/100,
                            trivertex.get(9*trcnt + 3*iCorner + 1)*avt1/100,n,l,m)>0)
                    {
                        tricolor1.put(9*trcnt + 3*iCorner, 1.0f);
                        tricolor1.put(9*trcnt + 3*iCorner + 1, 0.0f);
                        tricolor1.put(9*trcnt + 3*iCorner + 2, 0.0f);
                        tricolor2.put(9*trcnt + 3*iCorner, 0.0f);
                        tricolor2.put(9*trcnt + 3*iCorner + 1, 0.0f);
                        tricolor2.put(9*trcnt + 3*iCorner + 2, 1.0f);
                    }
                    else
                    {
                        tricolor1.put(9*trcnt + 3*iCorner, 0.0f);
                        tricolor1.put(9*trcnt + 3*iCorner + 1, 0.0f);
                        tricolor1.put(9*trcnt + 3*iCorner + 2, 1.0f);
                        tricolor2.put(9*trcnt + 3*iCorner, 1.0f);
                        tricolor2.put(9*trcnt + 3*iCorner + 1, 0.0f);
                        tricolor2.put(9*trcnt + 3*iCorner + 2, 0.0f);
                    }
                }
                trcnt++;
            }
        }
        else
        {
            for(iTriangle = 0; iTriangle < 5; iTriangle++)
            {
                if(MarchingCubes.a2iTriangleConnectionTable[iFlagIndex][3*iTriangle] < 0)
                    break;


                if ((9*trcntanim + 9)>=size1) {
                    fl = false;
                    overflow = true;
                    break;
                }



                for(iCorner = 0; iCorner < 3; iCorner++)
                {
                    iVertex = MarchingCubes.a2iTriangleConnectionTable[iFlagIndex][3*iTriangle+iCorner];


                    trivertexanim.put(9*trcntanim + 3*iCorner, asEdgeVertex[iVertex].fX);
                    trivertexanim.put(9*trcntanim + 3*iCorner + 1, asEdgeVertex[iVertex].fY);
                    trivertexanim.put(9*trcntanim + 3*iCorner + 2, asEdgeVertex[iVertex].fZ);
                    trinormalanim.put(9*trcntanim + 3*iCorner, asEdgeNorm[iVertex].fX);
                    trinormalanim.put(9*trcntanim + 3*iCorner + 1, asEdgeNorm[iVertex].fY);
                    trinormalanim.put(9*trcntanim + 3*iCorner + 2, asEdgeNorm[iVertex].fZ);
                    if (hAtom.ksi(trivertexanim.get(9*trcntanim + 3*iCorner + 2)*avt1/100,
                            trivertexanim.get(9*trcntanim + 3*iCorner)*avt1/100,
                            trivertexanim.get(9*trcntanim + 3*iCorner + 1)*avt1/100,n,l,m)>0)
                    {
                        tricoloranim1.put(9*trcntanim + 3*iCorner, 1.0f);
                        tricoloranim1.put(9*trcntanim + 3*iCorner + 1, 0.0f);
                        tricoloranim1.put(9*trcntanim + 3*iCorner + 2, 0.0f);
                        tricoloranim2.put(9*trcntanim + 3*iCorner, 0.0f);
                        tricoloranim2.put(9*trcntanim + 3*iCorner + 1, 0.0f);
                        tricoloranim2.put(9*trcntanim + 3*iCorner + 2, 1.0f);
                    }
                    else
                    {
                        tricoloranim1.put(9*trcntanim + 3*iCorner, 0.0f);
                        tricoloranim1.put(9*trcntanim + 3*iCorner + 1, 0.0f);
                        tricoloranim1.put(9*trcntanim + 3*iCorner + 2, 1.0f);
                        tricoloranim2.put(9*trcntanim + 3*iCorner, 1.0f);
                        tricoloranim2.put(9*trcntanim + 3*iCorner + 1, 0.0f);
                        tricoloranim2.put(9*trcntanim + 3*iCorner + 2, 0.0f);
                    }
                }
                trcntanim++;
            }
        }
    }

    // Surface generation using the Marching Cubes algorithm
    public void vMarchingCubes()
    {
//        Log.d("MarchingCubes", "Start");
        trcntanim = trcnt = 0;
        totalprogress = progress = 0;
        double [] sortvaltable;
        anim = true;
        fStepSize1 = Math.max(fStepSizeanim, fStepSize);
        int ncbs = (int)(250.0f/fStepSize1+1);
        ncubesanim = ncbs;
        int tncbs = (int)(250.0f/fStepSize+1);
        totalprogress = (ncbs)*(ncbs)*(ncbs) + (tncbs)*(tncbs)*(tncbs);
        valtable = new float[(ncbs+1)*(ncbs+1)*(ncbs+1)];
        int nraz = ncbs/101 + 1;
        maxinda = ncbs+1;
//        Log.d("MarchingCubes", "Anim start");
        {
            for(int iy=0;iy<ncbs+1 && !stopThread;iy++)
                for(int ix=0;ix<ncbs+1 && !stopThread;ix++)
                    for(int iz=0;iz<ncbs+1 && !stopThread;iz++)
                        valtable[index_table(ix,iy,iz)] = fSample(-250.0f + fStepSize1*(ix),
                                -250.0f + fStepSize1*(iy),
                                -250.0f + fStepSize1*(iz));


            for(int iy=0;iy<ncbs && !overflow && !stopThread;iy++)
                for(int ix=0;ix<ncbs && !overflow && !stopThread;ix++)
                    for(int iz=0;iz<ncbs && !overflow && !stopThread;iz++) {
                        vMarchCube(ix, iy, iz, fStepSize1);
                        progress++;
                    }
        }
//        Log.d("MarchingCubes", "Anim finish");
        anim = false;
        fStepSize1 = fStepSize;

        ncbs = (int)(250.0f/fStepSize1+1);
        ncubes = ncbs;
        valtable = new float[(ncbs+1)*(ncbs+1)*(ncbs+1)];
        nraz = ncbs/101 + 1;
        maxinda = ncbs+1;
//        Log.d("MarchingCubes", "Normal start");
        {
            for(int iy=0;iy<ncbs+1 && !stopThread;iy++)
                for(int ix=0;ix<ncbs+1 && !stopThread;ix++)
                    for(int iz=0;iz<ncbs+1 && !stopThread;iz++)
                        valtable[index_table(ix,iy,iz)] = fSample(-250.0f + fStepSize1*(ix),
                                    -250.0f + fStepSize1*(iy),
                                    -250.0f + fStepSize1*(iz));

            for(int iy=0;iy<ncbs && !overflow && !stopThread;iy++)
                for(int ix=0;ix<ncbs && !overflow && !stopThread;ix++)
                    for(int iz=0;iz<ncbs && !overflow && !stopThread;iz++) {
                        vMarchCube(ix, iy, iz, fStepSize1);
                        progress++;
                    }
        }
//        Log.d("MarchingCubes", "Normal finish");

        valtable = null;
//        Log.d("MarchingCubes", "trcntanim = " + trcntanim);
//        Log.d("MarchingCubes", "trcnt = " + trcnt);
//        Log.d("MarchingCubes", "Finish");
        fin = 1; // hypersurface calculation done
    }

    public void setRealKsi(boolean real) {
        drawrks = real;
        hAtom.realksi = real;
    }

    void regenerate()
    {
//        Log.d("Render", "Regenerate start");
        fin = 0;
        trcnt = 0;
        overflow = false;
        if (m<0)
        {
            hAtom.setsign(true);
            drawsign = true;
            m = -m;
        }
        else
        {
            hAtom.setsign(false);
            drawsign = false;
        }
        if (overflow)
        {
            overflow = false;
            trivertex.clear();
            trivertex = ByteBuffer.allocateDirect(
                    // (number of coordinate values * 4 bytes per float)
                    size1 * 3 * 4).
                    order(ByteOrder.nativeOrder()).
                    asFloatBuffer();
            trinormal.clear();
            trinormal = ByteBuffer.allocateDirect(
                    // (number of coordinate values * 4 bytes per float)
                    size1 * 3 * 4).
                    order(ByteOrder.nativeOrder()).
                    asFloatBuffer();
            tricolor1.clear();
            tricolor1 = ByteBuffer.allocateDirect(
                    // (number of coordinate values * 4 bytes per float)
                    size1 * 3 * 4).
                    order(ByteOrder.nativeOrder()).
                    asFloatBuffer();
            tricolor2.clear();
            tricolor2 = ByteBuffer.allocateDirect(
                    // (number of coordinate values * 4 bytes per float)
                    size1 * 3 * 4).
                    order(ByteOrder.nativeOrder()).
                    asFloatBuffer();
        }


        rounderror = false;

//        Log.d("Render", "Prepareallpov start");
        hAtom.prepareallpov(n, l, m);
//        Log.d("Render", "Prepareallpov finish");


        if (n==1) avt1 = 6.;
        else avt1 = 2.5 * n;

//        Log.d("Render", "Get target value start");
//        Log.d("Render", "Size: " + 10*(int)(250.0f/fStepSize+1)*(int)(100.f*Math.sqrt(l+1.f)));
          fTargetValue = (float)hAtom.getEquiValue(pct/100., 10*(int)(250.0f/fStepSize+1), 2.5*avt1, (int)(100.f*Math.sqrt(l+1.f)), n, l, m);
//        fTargetValue = (float)hAtom.getEquiValue(pct/100., 2*(int)(250.0f/fStepSize+1), 2.5*avt1, (int)(20.f*Math.sqrt(l+1.f)), n, l, m);
//        Log.d("Render", "fTargetValue: " + fTargetValue);
//        Log.d("Render", "Get target value finish");


        stopThread = false;
        Thread t = new Thread(new Runnable() {
            public void run() {
                vMarchingCubes();
            }
        });
        t.start();

    }

    public void InterruptThread() {
        if (fin==0) {
//            Log.d("Render", "Suspending thread");
            stopThread = true;
        }
    }
}
