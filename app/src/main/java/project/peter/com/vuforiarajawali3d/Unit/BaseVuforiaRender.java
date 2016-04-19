package project.peter.com.vuforiarajawali3d.Unit;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vuforia;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import project.peter.com.vuforiarajawali3d.ImageTargetActivity;
import project.peter.com.vuforiarajawali3d.SampleApplication.SampleApplicationSession;
import project.peter.com.vuforiarajawali3d.SampleApplication.utils.LoadingDialogHandler;
import project.peter.com.vuforiarajawali3d.SampleApplication.utils.SampleUtils;

/**
 * Created by linweijie on 4/18/16.
 */
public class BaseVuforiaRender implements GLSurfaceView.Renderer {

    private static final String LOGTAG = "BaseVuforiaRender";

    private int MODE = BaseVuforiaActivity.MODE_ImageTarget;

    private SampleApplicationSession vuforiaAppSession;
    private BaseVuforiaActivity mActivity;

    private Renderer mRenderer;

    private float OBJECT_SCALE_FLOAT = 1.0f;
    private float OBJECT_TRANSLATE_X_FLOAT = 0.0f;
    private float OBJECT_TRANSLATE_Y_FLOAT = 0.0f;
    private float OBJECT_ROTATE_ANGLE_FLOAT = 0.0f;

    private int LAST_INDEX = -1;

    public void setARMode(int mode){
        this.MODE = mode;
    }

    public void setOBJECT_SCALE_FLOAT(float OBJECT_SCALE_FLOAT) {
        this.OBJECT_SCALE_FLOAT = OBJECT_SCALE_FLOAT;
    }
    public void setOBJECT_TRANSLATE_X_FLOAT(float OBJECT_TRANSLATE_X_FLOAT) {
        this.OBJECT_TRANSLATE_X_FLOAT = OBJECT_TRANSLATE_X_FLOAT;
    }
    public void setOBJECT_TRANSLATE_Y_FLOAT(float OBJECT_TRANSLATE_Y_FLOAT) {
        this.OBJECT_TRANSLATE_Y_FLOAT = OBJECT_TRANSLATE_Y_FLOAT;
    }
    public void setOBJECT_ROTATE_ANGLE_FLOAT(float OBJECT_ROTATE_ANGLE_FLOAT) {
        this.OBJECT_ROTATE_ANGLE_FLOAT = OBJECT_ROTATE_ANGLE_FLOAT;
    }

    public BaseVuforiaRender(BaseVuforiaActivity activity,
                             SampleApplicationSession session) {
        mActivity = activity;
        vuforiaAppSession = session;
    }

    /**
     * GLSurfaceView.Renderer override function
     * */
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
        mActivity.loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);
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
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera

        // did we find any trackables this frame?
        switch (MODE){
            case BaseVuforiaActivity.MODE_ImageTarget:
                ImageTarget_FindTrackables(state);
                break;
            case BaseVuforiaActivity.MODE_CloudReco:
                CloudReco_FindTrackables(state);
                break;
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mRenderer.end();
    }

    private void ImageTarget_FindTrackables(State state){
        if (state.getNumTrackableResults()>0){

            for(int tIdx=0; tIdx<state.getNumTrackableResults(); tIdx++){
                TrackableResult result = state.getTrackableResult(tIdx);

                if (result == null)
                {
                    mActivity.BaseRajawaliRender.isShowObject(false);
                    return;
                }
                Log.d(LOGTAG, "ImageTarget trackable " + tIdx + " Name : " + result.getTrackable().getName());

                // 依本地設定的 DataSet xml 內容來選擇所要顯示的 model
                int textureIndex = 0;

                DataSet temp_dataset = mActivity.getmCurrentDataset();
                for (int i=0; i<temp_dataset.getNumTrackables(); ++i){
                    if (temp_dataset.getTrackable(i).getName().equals(result.getTrackable().getName())){
                        textureIndex = i;
                    }
                }
                renderAugmentation(result, textureIndex);
            }
            ImageTargetActivity.BaseRajawaliRender.isShowObject(true);

        } else {
            ImageTargetActivity.BaseRajawaliRender.isShowObject(false);
        }
    }

    private void CloudReco_FindTrackables(State state){
        if (state.getNumTrackableResults()>0){
            int test = state.getNumTrackableResults();
            for(int tIdx=0; tIdx<state.getNumTrackableResults(); tIdx++){
                TrackableResult result = state.getTrackableResult(tIdx);
                if (result == null)
                {
                    mActivity.BaseRajawaliRender.isShowObject(false);
                    return;
                }
                Log.d(LOGTAG, "CloudReco trackable " + tIdx + " Name : " + result.getTrackable().getName());

                // 依參考雲端自訂的 Target Name 來選擇所要顯示的 model
                int textureIndex = 0;

                ArrayList<String> temp = mActivity.getCloudDataSet();
                for (int i=0; i<temp.size(); ++i){
                    if (result.getTrackable().getName().equals(temp.get(i))){
                        textureIndex = i;
                    }
                }

                mActivity.stopFinderIfStarted();

                renderAugmentation(result, textureIndex);
            }
            ImageTargetActivity.BaseRajawaliRender.isShowObject(true);

        } else {
            mActivity.startFinderIfStopped();
            ImageTargetActivity.BaseRajawaliRender.isShowObject(false);
        }
    }

    private void renderAugmentation(TrackableResult trackableResult, int textureIndex)
    {
        // 當辨識物切換時，在進行模型切換
        if (LAST_INDEX!=textureIndex){
            mActivity.ChangeModelByIndex(textureIndex);
            LAST_INDEX = textureIndex;
        }

        // 取得辨識變形矩陣
        Matrix44F modelViewMatrix_Vuforia = Tool
                .convertPose2GLMatrix(trackableResult.getPose());
        float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
        float[] modelViewProjection = new float[16];

        Matrix.translateM(modelViewMatrix, 0, OBJECT_TRANSLATE_X_FLOAT, OBJECT_TRANSLATE_Y_FLOAT,
                OBJECT_SCALE_FLOAT);
        Matrix.rotateM(modelViewMatrix, 0, OBJECT_ROTATE_ANGLE_FLOAT, 1.0f, 0, 0);
        Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
                OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);
        Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession.getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

        // 繪製於 BaseRajawaliRender 目前的模型上面
        mActivity.BaseRajawaliRender.moveObject3D(modelViewProjection, vuforiaAppSession.getProjectionMatrix().getData(), modelViewMatrix);

        SampleUtils.checkGLError("Render Frame");
    }
}
