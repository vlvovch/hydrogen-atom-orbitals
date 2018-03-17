/*
 * This is the source code of the Hydrogen Atom Orbitals app for Android.
 * It is licensed under the MIT License.
 *
 * Copyright (c) 2015-2018 Volodymyr Vovchenko.
 */

package com.vlvolad.hydrogenatom;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.preference.PreferenceManager;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Volodymyr on 28.04.2015.
 */
public class HAGLRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "HAGLRenderer";
    public static HydrogenAtomRender mAtom = new HydrogenAtomRender();
    public int Width, Height;

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Set the background frame color
        GLES20.glClearColor(0.f, 0.f, 0.f, 1.0f);

        GLES20.glClearDepthf(1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);


        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc( GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA );
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glLineWidth(2.0f);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        int mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables

        mAtom.mProgram = mProgram;

    }

    @Override
    public void onDrawFrame(GL10 unused) {

        GLES20.glUseProgram(mAtom.mProgram);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Matrix.setIdentityM(mAtom.mVMatrix, 0);


        // Draw pendulum
        mAtom.draw(unused, Width, Height);

        // Draw axes
        if (PreferenceManager.getDefaultSharedPreferences(HAGLActivity.getContextOfApplication()).getBoolean("pref_showaxes", true))
            mAtom.drawAxes(unused, Width, Height);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        Width = width;
        Height = height;
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        perspectiveGL(mAtom.mProjMatrix, 45.0f,(float)(Width)/Height,10.0f,4000.0f);

    }

    public static void orthoGL(float[] ProjectionMatrix, float Width, float Height)
    {
        Matrix.orthoM(ProjectionMatrix, 0, 0, Width, 0, Height, -1000.f, 1000.f);
    }

    public static void perspectiveGL(float[] ProjectionMatrix, float fovY, float aspect, float zNear, float zFar)
    {
        final float pi = (float) 3.1415926535897932384626433832795;
        float fW, fH;
        fH = (float) (Math.tan( fovY / 360 * pi ) * zNear);
        fW = (float) (fH * aspect);
        Matrix.frustumM(ProjectionMatrix, 0, -fW, fW, -fH-0.25f*fH, fH-0.25f*fH, zNear, zFar);
        //GLES20.glFrustumf( -fW, fW, -fH, fH, zNear, zFar );
    }

    private final String vertexShaderCode =
            "struct DirectionalLight { \n" +
                    "vec3 direction; \n" +
                    "vec3 halfplane; \n" +
                    "vec4 ambientColor; \n" +
                    "vec4 diffuseColor; \n" +
                    "vec4 specularColor; \n" +
                    "}; \n" +
                    "struct Material { \n" +
                    "    vec4 ambientFactor; \n" +
                    "    vec4 diffuseFactor; \n" +
                    "    vec4 specularFactor; \n" +
                    "    float shininess; \n" +
                    "}; \n" +
                    "// Light \n" +
                    "uniform DirectionalLight u_directionalLight; \n" +
                    "// Material \n" +
                    "uniform Material u_material; \n" +
                    "// Matrices \n" +
                    "uniform mat4 u_mvMatrix; \n" +
                    "uniform mat4 u_mvpMatrix; \n" +
                    "uniform vec4 color \n;" +
                    "// Attributes \n" +
                    "attribute vec4 a_position; \n" +
                    "attribute vec4 a_color; \n" +
                    "attribute vec3 a_normal; \n" +
                    "// Varyings \n" +
                    "varying vec4 v_light; \n" +
                    "varying vec4 v_color; \n" +
                    "void main() { \n" +
                    "    // Define position and normal in model coordinates \n" +
                    "    vec4 mcPosition = a_position; \n" +
                    "    vec3 mcNormal = a_normal; \n" +
                    "    // Calculate and normalize eye space normal \n" +
                    "    vec3 ecNormal = vec3(u_mvMatrix * vec4(mcNormal, 0.0)); \n" +
                    "    ecNormal = ecNormal / length(ecNormal); \n" +
                    "    // Do light calculations \n" +
                    "    float ecNormalDotLightDirection = max(0.0, dot(ecNormal, u_directionalLight.direction)); \n" +
                    "    float ecNormalDotLightHalfplane = max(0.0, dot(ecNormal, u_directionalLight.halfplane)); \n" +
                    "    // Ambient light \n" +
                    "    vec4 ambientLight = u_directionalLight.ambientColor * a_color;//u_material.ambientFactor; \n" +
                    "    // Diffuse light \n" +
                    "    vec4 diffuseLight = ecNormalDotLightDirection * u_directionalLight.diffuseColor * a_color;//u_material.diffuseFactor; \n" +
                    "    // Specular light \n" +
                    "    vec4 specularLight = vec4(0.0); \n" +
                    "    if (ecNormalDotLightHalfplane > 0.0) { \n" +
                    "        specularLight = pow(ecNormalDotLightHalfplane, u_material.shininess) * u_directionalLight.specularColor * u_material.specularFactor; \n" +
                    "    } \n" +
                    "    v_light = ambientLight + diffuseLight + specularLight; \n" +
                    "    v_color = a_color; \n" +
                    //"    v_color = color; \n" +
                    "    gl_Position = u_mvpMatrix * mcPosition; \n" +
                    "}";


    private final String fragmentShaderCode =
            "precision highp float; \n" +
                    "uniform vec4 color; \n" +
                    "uniform int light; \n" +
                    "uniform int trajectory; \n" +
                    "varying vec4 v_light; \n" +
                    "varying vec4 v_color; \n" +
                    "void main() { \n" +
                    "    if (light>0) gl_FragColor = v_light; \n" +
                    //"    else if (trajectory>0) gl_FragColor = v_color; \n" +
                    "    else gl_FragColor = color; \n" +
                    "}";


    private final String vertexShaderCodeTraj =
            "uniform mat4 u_mvpMatrix; \n" +
                    "// Attributes \n" +
                    "attribute vec4 a_position; \n" +
                    "attribute vec4 a_color; \n" +
                    "// Varyings \n" +
                    "varying vec4 v_color; \n" +
                    "void main() { \n" +
                    "    // Define position and normal in model coordinates \n" +
                    "    vec4 mcPosition = a_position; \n" +
                    "    gl_Position = u_mvpMatrix * mcPosition; \n" +
                    "    v_color = a_color; \n" +
                    "}";


    private final String fragmentShaderCodeTraj =
            "varying vec4 v_color; \n" +
                    "void main() { \n" +
                    "    gl_FragColor = vec4(1.0, 0., 0., 1.); \n" + //v_color; \n" +
                    "}";

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
}
