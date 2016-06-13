package project.peter.com.vuforiarajawali3d.Unit;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ImageTargetResult;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.MarkerResult;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.VirtualButton;
import com.qualcomm.vuforia.VirtualButtonResult;
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

    private static final boolean DEBUG = true;
    private static final String LOGTAG = "BaseVuforiaRender";

    private int MODE = BaseVuforiaActivity.MODE_ImageTarget;

    private SampleApplicationSession vuforiaAppSession;
    private BaseVuforiaActivity mActivity;

    private Renderer mRenderer;

    private VirtualButtonCallback buttonCallback;

    public void setARMode(int mode){
        this.MODE = mode;
    }
    public void setButtonCallback(VirtualButtonCallback callback){
        this.buttonCallback = callback;
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
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");
        if (MODE == BaseVuforiaActivity.MODE_UserDefinedTarget){
            // Call function to update rendering when render surface
            // parameters have changed:
            mActivity.updateRendering();
        }
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
            case BaseVuforiaActivity.MODE_FrameMarkers:
                FrameMarker_FindTrackables(state);
                break;
            case BaseVuforiaActivity.MODE_VirtualButton:
                VirtualButton_FindTrackables(state);
                break;
            case BaseVuforiaActivity.MODE_UserDefinedTarget:
                // Render the RefFree UI elements depending on the current state
                mActivity.refFreeFrame.render();
                userDefinedTarget(state);
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

                if (DEBUG)
                    Log.d(LOGTAG, "ImageTarget trackable " + tIdx + " Name : " + result.getTrackable().getName());

                // 依本地設定的 DataSet xml 內容來選擇所要顯示的 model
                for (int i=0; i<temp_dataset.getNumTrackables(); ++i){
                    if (temp_dataset.getTrackable(i).getName().equals(result.getTrackable().getName())){
                        recorder.put(i, result);
                    }
                }
            }

            ArrayList<Boolean> visibleRecorder = new ArrayList<>();
            for (int i=0; i<temp_dataset.getNumTrackables(); ++i){
                if (recorder.get(i)!=null){
                    visibleRecorder.add(true);
                    renderAugmentation(recorder.get(i), i);
                } else {
                    visibleRecorder.add(false);
                }
            }
            mActivity.setVisibleModelByBoolenArray(visibleRecorder);

        } else {
            mActivity.HideAllModel();
        }
    }

    private void CloudReco_FindTrackables(State state){

        // 雲端辨識僅追蹤一筆
        if (state.getNumTrackableResults()>0){

            ArrayList<String> temp = mActivity.getCloudDataSet();
            ArrayList<Boolean> visibleRecorder = new ArrayList<>();

            for(int tIdx=0; tIdx<state.getNumTrackableResults(); tIdx++){
                TrackableResult result = state.getTrackableResult(tIdx);
                if (result == null)
                {
                    mActivity.HideAllModel();
                    return;
                }

                if (DEBUG)
                    Log.d(LOGTAG, "CloudReco trackable " + tIdx + " Name : " + result.getTrackable().getName());

                for (int i=0; i<temp.size(); ++i){
                    if (result.getTrackable().getName().equals(temp.get(i))){
                        visibleRecorder.add(true);
                        renderAugmentation(result, i);
                    } else {
                        visibleRecorder.add(false);
                    }
                }

                mActivity.stopFinderIfStarted();
            }
            mActivity.setVisibleModelByBoolenArray(visibleRecorder);

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

                if (DEBUG)
                    Log.d(LOGTAG,  "FrameMarker trackable " + tIdx + " MarkerId : " + String.valueOf(id));

                for (int i=0; i<mActivity.getMarkerDataSetArray().size(); ++i){
                    if (mActivity.getMarkerDataSetArray().get(i) == id){
                        recorder.put(i, result);
                    }
                }
            }

            ArrayList<Boolean> visibleRecorder = new ArrayList<>();
            for (int i=0; i<mActivity.getMarkerDataSetArray().size(); ++i){
                if (recorder.get(i)!=null){
                    visibleRecorder.add(true);
                    renderAugmentation(recorder.get(i), i);
                } else {
                    visibleRecorder.add(false);
                }
            }
            mActivity.setVisibleModelByBoolenArray(visibleRecorder);

        } else {
            mActivity.HideAllModel();
        }
    }

    private void VirtualButton_FindTrackables(State state){

        if (state.getNumTrackableResults()>0){

            ArrayList<String[]> buttons = mActivity.getVirtualButtonName();
            DataSet temp_dataset = mActivity.getmCurrentDataset();
            HashMap<Integer, TrackableResult> recorder = new HashMap<>();

            for(int tIdx=0; tIdx<state.getNumTrackableResults(); tIdx++){
                TrackableResult result = state.getTrackableResult(tIdx);
                if (result == null)
                {
                    mActivity.HideAllModel();
                    return;
                }

                if (DEBUG)
                    Log.d(LOGTAG, "ImageTarget trackable " + tIdx + " Name : " + result.getTrackable().getName());

                // 依本地設定的 DataSet xml 內容來選擇所要顯示的 model
                for (int i=0; i<temp_dataset.getNumTrackables(); ++i){
                    if (temp_dataset.getTrackable(i).getName().equals(result.getTrackable().getName())){
                        recorder.put(i, result);
                    }
                }
            }

            ArrayList<Boolean> visibleRecorder = new ArrayList<>();
            for (int i=0; i<temp_dataset.getNumTrackables(); ++i){
                if (recorder.get(i)!=null){
//                    mActivity.setVisibleModelByIndex(i, true);
                    visibleRecorder.add(true);
                    renderAugmentation(recorder.get(i), i);
                    if (buttons.size()!=0)
                        drawVirtualButton(recorder.get(i), i, buttons.get(i));
                    else
                        Log.d(LOGTAG, "Please set VirtualButtonName");
                } else {
//                    mActivity.setVisibleModelByIndex(i, false);
                    visibleRecorder.add(false);
                }
            }
            mActivity.setVisibleModelByBoolenArray(visibleRecorder);
        } else {
            mActivity.HideAllModel();
        }
    }

    private void userDefinedTarget(State state){

        if (state.getNumTrackableResults()>0){

            DataSet temp_dataset = mActivity.getDataSetUserDef();
            HashMap<Integer, TrackableResult> recorder = new HashMap<>();

            for(int tIdx=0; tIdx<state.getNumTrackableResults(); tIdx++){
                TrackableResult result = state.getTrackableResult(tIdx);
                if (result == null)
                {
                    mActivity.HideAllModel();
                    return;
                }

                if (DEBUG)
                    Log.d(LOGTAG, "ImageTarget trackable " + tIdx + " Name : " + result.getTrackable().getName());

                // 依本地設定的 DataSet xml 內容來選擇所要顯示的 model
                for (int i=0; i<temp_dataset.getNumTrackables(); ++i){
                    if (temp_dataset.getTrackable(i).getName().equals(result.getTrackable().getName())){
                        recorder.put(i, result);
                    }
                }
            }

            ArrayList<Boolean> visibleRecorder = new ArrayList<>();
            for (int i=0; i<temp_dataset.getNumTrackables(); ++i){
                if (recorder.get(i)!=null){
                    visibleRecorder.add(true);
                    renderAugmentation(recorder.get(i), i);

                } else {
                    visibleRecorder.add(false);
                }
            }
            mActivity.setVisibleModelByBoolenArray(visibleRecorder);

        } else {
            mActivity.HideAllModel();
        }
    }


    private void renderAugmentation(TrackableResult trackableResult, int textureIndex)
    {
        Log.d(LOGTAG, "trackableResult : " + trackableResult.getTrackable().getName());

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
                mActivity.getOBJECT_ROTATE_ANGLE_FLOAT(textureIndex),
                mActivity.getOBJECT_ROTATE_X_FLOAT(textureIndex),
                mActivity.getOBJECT_ROTATE_Y_FLOAT(textureIndex),
                mActivity.getOBJECT_ROTATE_Z_FLOAT(textureIndex)
        );
        Matrix.scaleM(modelViewMatrix, 0,
                mActivity.getOBJECT_SCALE_FLOAT(textureIndex),
                mActivity.getOBJECT_SCALE_FLOAT(textureIndex),
                mActivity.getOBJECT_SCALE_FLOAT(textureIndex));
        Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession.getProjectionMatrix().getData(), 0, modelViewMatrix, 0);

        // 繪製於 BaseRajawaliRender 目前的模型上面
        mActivity.BaseRajawaliRender.moveObject3D(textureIndex, modelViewProjection, vuforiaAppSession.getProjectionMatrix().getData(), modelViewMatrix);

        SampleUtils.checkGLError("Render Frame");
    }


    private void drawVirtualButton(TrackableResult trackableResult, int textureIndex, String[] buttons){
        ImageTargetResult imageTargetResult = (ImageTargetResult) trackableResult;

        for (int i = 0; i < imageTargetResult.getNumVirtualButtons(); ++i)
        {
            VirtualButtonResult buttonResult = imageTargetResult
                    .getVirtualButtonResult(i);
            VirtualButton button = buttonResult.getVirtualButton();

            int buttonIndex = 0;
            // Run through button name array to find button index
            for (int j = 0; j < buttons.length; ++j)
            {
                if (button.getName().compareTo(
                        buttons[j]) == 0)
                {
                    buttonIndex = j;
                    break;
                }
            }

            // If the button is pressed, than use this texture:
            if (buttonResult.isPressed())
            {
                if (buttonCallback!=null)
                    buttonCallback.isPressed(textureIndex, buttonIndex);
            }

            //TODO 按鈕框處理
        }
    }
}
