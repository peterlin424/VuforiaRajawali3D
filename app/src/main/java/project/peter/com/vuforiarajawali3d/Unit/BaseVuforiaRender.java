package project.peter.com.vuforiarajawali3d.Unit;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.MarkerResult;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vuforia;

import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

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

    public void setARMode(int mode){
        this.MODE = mode;
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
//        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");
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
//        Log.d(LOGTAG, "getNumTrackableResults : " + String.valueOf(state.getNumTrackableResults()));
        switch (MODE){
            case BaseVuforiaActivity.MODE_ImageTarget:
                ImageTarget_FindTrackables(state);
                break;
            case BaseVuforiaActivity.MODE_CloudReco:
                CloudReco_FindTrackables(state);
                break;
            case BaseVuforiaActivity.MODE_FrameMarkers:
                FrameMarker_FindTrackables(state);
                break;
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mRenderer.end();
    }

    private void ImageTarget_FindTrackables(State state){

        if (state.getNumTrackableResults()>0){

            DataSet temp_dataset = mActivity.getmCurrentDataset();
            HashMap<Integer, TrackableResult> recorder = new HashMap<>();

            for(int tIdx=0; tIdx<state.getNumTrackableResults(); tIdx++){
                TrackableResult result = state.getTrackableResult(tIdx);
                if (result == null)
                {
                    mActivity.HideAllModel();
                    return;
                }
//                Log.d(LOGTAG, "ImageTarget trackable " + tIdx + " Name : " + result.getTrackable().getName());

                // 依本地設定的 DataSet xml 內容來選擇所要顯示的 model
                for (int i=0; i<temp_dataset.getNumTrackables(); ++i){
                    if (temp_dataset.getTrackable(i).getName().equals(result.getTrackable().getName())){
                        recorder.put(i, result);
                    }
                }
            }

            for (int i=0; i<temp_dataset.getNumTrackables(); ++i){
                if (recorder.get(i)!=null){
                    mActivity.setVisiableModelByIndex(i, true);

                    renderAugmentation(recorder.get(i), i);


                } else {
                    mActivity.setVisiableModelByIndex(i, false);
                }
            }
        } else {
            mActivity.HideAllModel();
        }
    }

    private void CloudReco_FindTrackables(State state){

        // 雲端辨識僅追蹤一筆
        if (state.getNumTrackableResults()>0){

            ArrayList<String> temp = mActivity.getCloudDataSet();

            for(int tIdx=0; tIdx<state.getNumTrackableResults(); tIdx++){
                TrackableResult result = state.getTrackableResult(tIdx);
                if (result == null)
                {
                    mActivity.HideAllModel();
                    return;
                }
//                Log.d(LOGTAG, "CloudReco trackable " + tIdx + " Name : " + result.getTrackable().getName());

                // 依參考雲端自訂的 Target Name 來選擇所要顯示的 model
                int textureIndex = 0;
                for (int i=0; i<temp.size(); ++i){
                    if (result.getTrackable().getName().equals(temp.get(i))){
                        textureIndex = i;
                    }
                }

                mActivity.stopFinderIfStarted();
                mActivity.setVisiableModelByIndex(textureIndex, true);
                renderAugmentation(result, textureIndex);
            }
        } else {
            mActivity.startFinderIfStopped();
            mActivity.HideAllModel();
        }
    }

    private void FrameMarker_FindTrackables(State state){
        if (state.getNumTrackableResults()>0){

            HashMap<Integer, TrackableResult> recorder = new HashMap<>();

            for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
            {
                TrackableResult result = state.getTrackableResult(tIdx);
                if (result == null)
                {
                    mActivity.HideAllModel();
                    return;
                }
                Marker marker = (Marker) ((MarkerResult)result).getTrackable();
                int id = marker.getMarkerId();

                for (int i=0; i<mActivity.getMarkerDataSetArray().size(); ++i){
                    if (mActivity.getMarkerDataSetArray().get(i) == id){
                        recorder.put(i, result);
                    }
                }
            }

            for (int i=0; i<mActivity.getMarkerDataSetArray().size(); ++i){
                if (recorder.get(i)!=null){
                    mActivity.setVisiableModelByIndex(i, true);
                    renderAugmentation(recorder.get(i), i);
                } else {
                    mActivity.setVisiableModelByIndex(i, false);
                }
            }
        } else {
            mActivity.HideAllModel();
        }
    }

    private void renderAugmentation(TrackableResult trackableResult, int textureIndex)
    {

        // 取得辨識變形矩陣
        Matrix44F modelViewMatrix_Vuforia = Tool
                .convertPose2GLMatrix(trackableResult.getPose());
        float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
        float[] modelViewProjection = new float[16];

        Matrix.translateM(modelViewMatrix, 0,
                mActivity.getOBJECT_TRANSLATE_X_FLOAT(textureIndex),
                mActivity.getOBJECT_TRANSLATE_Y_FLOAT(textureIndex),
                mActivity.getOBJECT_SCALE_FLOAT(textureIndex));
        Matrix.rotateM(modelViewMatrix, 0,
                mActivity.getOBJECT_ROTATE_ANGLE_FLOAT(textureIndex), 1.0f, 0, 0);
        Matrix.scaleM(modelViewMatrix, 0,
                mActivity.getOBJECT_SCALE_FLOAT(textureIndex),
                mActivity.getOBJECT_SCALE_FLOAT(textureIndex),
                mActivity.getOBJECT_SCALE_FLOAT(textureIndex));
        Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession.getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

        // 繪製於 BaseRajawaliRender 目前的模型上面
        mActivity.BaseRajawaliRender.moveObject3D(textureIndex, modelViewProjection, vuforiaAppSession.getProjectionMatrix().getData(), modelViewMatrix);

        SampleUtils.checkGLError("Render Frame");
    }


}
