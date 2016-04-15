package project.peter.com.vuforiarajawali3d.Render;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vuforia;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import project.peter.com.vuforiarajawali3d.SampleApplication.SampleApplicationSession;
import project.peter.com.vuforiarajawali3d.SampleApplication.utils.SampleUtils;
import project.peter.com.vuforiarajawali3d.Unit.BaseVuforiaActivity;

/**
 * Created by linweijie on 4/12/16.
 */
public class mCloudRecoRender implements GLSurfaceView.Renderer {

    private static final String LOGTAG = "CloudRecoRenderer";
    private SampleApplicationSession vuforiaAppSession;
    private BaseVuforiaActivity mActivity;

    private Renderer mRenderer;

    // TODO 顯示模型放大倍數
    private static final float OBJECT_SCALE_FLOAT = 0.1f;


    public mCloudRecoRender(BaseVuforiaActivity activity,
                            SampleApplicationSession session) {
        mActivity = activity;
        vuforiaAppSession = session;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
        initRendering();
        vuforiaAppSession.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        vuforiaAppSession.onSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        renderFrame();
    }

    private void initRendering()
    {
        mRenderer = Renderer.getInstance();
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

        // TODO
        // Hide the Loading Dialog
//        mActivity.loadingDialogHandler
//                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
    }

    private void renderFrame()
    {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        State state = mRenderer.begin();
        mRenderer.drawVideoBackground();

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW);  // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW);   // Back camera


        // Did we find any trackables this frame?
        if (state.getNumTrackableResults() > 0)
        {
            // TODO  Trackable().getName() 來判斷辨識物是 Cloud or Cloud2
            // Gets current trackable result
            TrackableResult trackableResult = state.getTrackableResult(0);
            Log.d("Peter", "Trackable Name : " + trackableResult.getTrackable().getName());

            if (trackableResult == null)
            {
                BaseVuforiaActivity.mRajawaliRender.isShowObject(false);
                return;
            }

            mActivity.stopFinderIfStarted();

            // Renders the Augmentation View with the 3D Book data Panel
            renderAugmentation(trackableResult);
            SampleUtils.checkGLError("Render Frame");
        }
        else
        {
            mActivity.startFinderIfStopped();
            BaseVuforiaActivity.mRajawaliRender.isShowObject(false);
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        Renderer.getInstance().end();
    }

    private void renderAugmentation(TrackableResult trackableResult)
    {
        Matrix44F modelViewMatrix_Vuforia = Tool
                .convertPose2GLMatrix(trackableResult.getPose());
        float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();

        int textureIndex = 0;

        // deal with the modelview and projection matrices
        float[] modelViewProjection = new float[16];
//        Matrix.translateM(modelViewMatrix, 0, 0.0f, 0.0f, OBJECT_SCALE_FLOAT);
//        Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
//                OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);
//        Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
//                .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);


        Matrix.translateM(modelViewMatrix, 0, -100.0f, -80.0f,
                OBJECT_SCALE_FLOAT);
        Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
        Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
                OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);

        Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession.getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

        BaseVuforiaActivity.mRajawaliRender.moveObject3D(modelViewProjection, vuforiaAppSession.getProjectionMatrix().getData(), modelViewMatrix);

        SampleUtils.checkGLError("Render Frame");

        BaseVuforiaActivity.mRajawaliRender.isShowObject(true);
    }
}
