package project.peter.com.vuforiarajawali3d.Unit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ImageTargetBuilder;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.ObjectTracker;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.TargetFinder;
import com.qualcomm.vuforia.TargetSearchResult;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vuforia;

import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.ArrayList;

import project.peter.com.vuforiarajawali3d.R;
import project.peter.com.vuforiarajawali3d.SampleApplication.SampleApplicationControl;
import project.peter.com.vuforiarajawali3d.SampleApplication.SampleApplicationException;
import project.peter.com.vuforiarajawali3d.SampleApplication.SampleApplicationSession;
import project.peter.com.vuforiarajawali3d.SampleApplication.utils.LoadingDialogHandler;
import project.peter.com.vuforiarajawali3d.SampleApplication.utils.SampleApplicationGLView;
import project.peter.com.vuforiarajawali3d.SampleApplication.utils.Texture;

/**
 * Created by linweijie on 4/13/16.
 */
public class BaseVuforiaActivity extends AppCompatActivity implements SampleApplicationControl {

    private static final String LOGTAG = "BaseVuforiaActivity";

    public final static int MODE_ImageTarget = 0;
    public final static int MODE_CloudReco = 1;
    public final static int MODE_FrameMarkers = 2;
    public final static int MODE_VirtualButton = 3;
    public final static int MODE_UserDefinedTarget = 4;
//    public final static int MODE_CubiodBox = 5;
//    public final static int MODE_Cylinder = 6;
    private int MODE = MODE_ImageTarget;

    private int MAX_TARGETS_COUNT = 1;

    private SampleApplicationSession vuforiaAppSession;

    private static final int HIDE_LOADING_DIALOG = 0;
    private static final int SHOW_LOADING_DIALOG = 1;
    private boolean mIsDroidDevice = false;
    private boolean mExtendedTracking = false;
    private SampleApplicationGLView mGlView;
    private RelativeLayout mUILayout;
    private GestureDetector mGestureDetector;
    private AlertDialog mErrorDialog;
    public LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);

    private RelativeLayout VuforiaLayout;
    private RelativeLayout RajawaliLayout;

    // ImageTarget
    private int mCurrentDatasetSelectionIndex = 0;
    private boolean mContAutofocus = false;
    private ArrayList<String> mDatasetStrings = new ArrayList<>();
    private DataSet mCurrentDataset;

    // CloudReco
    private static final int INIT_SUCCESS = 2;
    private static final int INIT_ERROR_NO_NETWORK_CONNECTION = -1;
    private static final int INIT_ERROR_SERVICE_NOT_AVAILABLE = -2;
    private static final int UPDATE_ERROR_AUTHORIZATION_FAILED = -1;
    private static final int UPDATE_ERROR_PROJECT_SUSPENDED = -2;
    private static final int UPDATE_ERROR_NO_NETWORK_CONNECTION = -3;
    private static final int UPDATE_ERROR_SERVICE_NOT_AVAILABLE = -4;
    private static final int UPDATE_ERROR_BAD_FRAME_QUALITY = -5;
    private static final int UPDATE_ERROR_UPDATE_SDK = -6;
    private static final int UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE = -7;
    private static final int UPDATE_ERROR_REQUEST_TIMEOUT = -8;

    boolean mFinderStarted = false;
    boolean mStopFinderIfStarted = false;

    private static String kAccessKey;
    private static String kSecretKey;

    private ArrayList<String> CloudDataSet = new ArrayList<>();

    // FrameMarker
    private ArrayList<Integer> MarkerDataSetArray = new ArrayList<>();
    private Marker MarkerDataSet[];

    // Virtual Button
    private ArrayList<String[]> VirtualButtonName = new ArrayList<>();
    private VirtualButtonCallback buttonCallback;

    // UserDefinedTarget
    private AlertDialog mDialog;    // Alert dialog for displaying SDK errors
    public RefFreeFrame refFreeFrame;
    private View mBottomBar;
    private View mCameraButton;
    private int targetBuilderCounter = 1;
    private DataSet dataSetUserDef = null;

    // Error message handling:
    private int mlastErrorCode = 0;
    private int mInitErrorCode = 0;
    private boolean mFinishActivityOnError;
    private double mLastErrorTime;

    // Renders
    public static BaseRajawaliRender BaseRajawaliRender = null;
    public static BaseVuforiaRender BaseVuforiaRender = null;

    /**
     * onCreate 前所需的設定：辨識模式, 本地辨識物 list, 雲端辨識物 keys
     * */
    public void setARMode(int mode){
        this.MODE = mode;
    }
    public void setButtonCallback(VirtualButtonCallback callback){
        this.buttonCallback = callback;
    }
    public void setCollisionCallback(CollisionCallback callback){
        BaseRajawaliRender.setCollisionCallback(callback);
    }

    //ImageTarget
    public void setDatasetStrings(ArrayList<String> datasetStrings){
        this.mDatasetStrings = datasetStrings;
    }
    public DataSet getmCurrentDataset() {
        return mCurrentDataset;
    }
    public void setMAX_TARGETS_COUNT(int MAX_TARGETS_COUNT) {
        this.MAX_TARGETS_COUNT = MAX_TARGETS_COUNT;
    }

    //CloudReco
    public void setCloudDataSet(ArrayList<String> cloudDataSet){
        this.CloudDataSet = cloudDataSet;
    }
    public ArrayList<String> getCloudDataSet(){
        return this.CloudDataSet;
    }
    public void setCloudTargetsKey(String accessKey, String secretKey){
        this.kAccessKey = accessKey;
        this.kSecretKey = secretKey;
    }

    //FrameMarker
    public ArrayList<Integer> getMarkerDataSetArray() {
        return MarkerDataSetArray;
    }
    public void setMarkerDataSetArray(ArrayList<Integer> markerDataSetArray) {
        MarkerDataSetArray = markerDataSetArray;
    }

    //VirtualButton
    public void setVirtualButtonName(ArrayList<String[]> vbName){
        this.VirtualButtonName = vbName;
    }
    public ArrayList<String[]> getVirtualButtonName(){
       return VirtualButtonName;
    }

    //UserDefined
    public DataSet getDataSetUserDef(){
        return dataSetUserDef;
    }
    /**
     * onCreate 後所需的設定：顯示 3D 模型 list, 切換要顯示的 3D 模型
     * */
    public void setModel3DArrayList(ArrayList<Model3D> model3DArrayList){
        BaseRajawaliRender.setModel3DArrayList(model3DArrayList);
    }
    public ArrayList<Model3D> getModel3DArrayList(){
        return BaseRajawaliRender.getModel3DArrayList();
    }

    /**
     * 控制 Rajawali
     * */
    public void HideAllModel(){
        BaseRajawaliRender.HideAllModel();
    }
    public void setVisiableModelByIndex(int index, boolean visiable){
        BaseRajawaliRender.setVisiableModelByIndex(index, visiable);
    }
    public float getOBJECT_TRANSLATE_X_FLOAT(int index){
        return BaseRajawaliRender.getObjTranslateX(index);
    }
    public float getOBJECT_TRANSLATE_Y_FLOAT(int index){
        return BaseRajawaliRender.getObjTranslateY(index);
    }
    public float getOBJECT_SCALE_FLOAT(int index){
        return BaseRajawaliRender.getObjScale(index);
    }
    public float getOBJECT_ROTATE_ANGLE_FLOAT(int index){
        return BaseRajawaliRender.getObjRotateAngle(index);
    }
    public float getOBJECT_ROTATE_X_FLOAT(int index){
        return BaseRajawaliRender.getObjRotateX(index);
    }
    public float getOBJECT_ROTATE_Y_FLOAT(int index){
        return BaseRajawaliRender.getObjRotateY(index);
    }
    public float getOBJECT_ROTATE_Z_FLOAT(int index){
        return BaseRajawaliRender.getObjRotateZ(index);
    }

    /**
     * Android life cycle
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_vura_layout);
        VuforiaLayout = (RelativeLayout)findViewById(R.id.vuforia_layout);
        RajawaliLayout = (RelativeLayout)findViewById(R.id.rajawali_layout);

        switch (MODE){
            case MODE_ImageTarget:
                if (mDatasetStrings.size()<=0){
                    new AlertDialog.Builder(this)
                            .setTitle("Init Error")
                            .setMessage("Please setDatasetStrings")
                            .setPositiveButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    BaseVuforiaActivity.this.finish();
                                }
                            })
                            .show();
                }
                break;
            case MODE_CloudReco:
                if (kAccessKey==null || kSecretKey==null){
                    new AlertDialog.Builder(this)
                            .setTitle("Init Error")
                            .setMessage("Please setCloudTargetsKey")
                            .setPositiveButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    BaseVuforiaActivity.this.finish();
                                }
                            })
                            .show();
                }
                break;
            case MODE_FrameMarkers:
                if (MarkerDataSetArray.size()==0){
                    new AlertDialog.Builder(this)
                            .setTitle("Init Error")
                            .setMessage("Please setFrameMarkerCount")
                            .setPositiveButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    BaseVuforiaActivity.this.finish();
                                }
                            })
                            .show();
                }
                break;
            case MODE_VirtualButton:
                if (VirtualButtonName.size()==0){
                    new AlertDialog.Builder(this)
                            .setTitle("Init Error")
                            .setMessage("Please setVirtualButton")
                            .setPositiveButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    BaseVuforiaActivity.this.finish();
                                }
                            })
                            .show();
                }
        }

        vuforiaAppSession = new SampleApplicationSession(this);
        startLoadingAnimation();

        vuforiaAppSession
                .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Creates the GestureDetector listener for processing double tap
        mGestureDetector = new GestureDetector(this, new GestureListener());

        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
                "droid");

        // init rajawali view
        // TODO RajawaliSurfaceView
        final RajawaliSurfaceView surface = new RajawaliSurfaceView(this);
        surface.setFrameRate(60.0);
        surface.setRenderMode(IRajawaliSurface.RENDERMODE_WHEN_DIRTY);

        // Add mSurface to your root view
        RajawaliLayout.addView(surface);

        BaseRajawaliRender = new BaseRajawaliRender(this);
        surface.setSurfaceRenderer(BaseRajawaliRender);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        try
        {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        // Pauses the OpenGLView
        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }

        System.gc();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        return mGestureDetector.onTouchEvent(event);
    }

    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
            GestureDetector.SimpleOnGestureListener
    {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();


        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            // Generates a Handler to trigger autofocus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable()
            {
                public void run()
                {
                    boolean result = CameraDevice.getInstance().setFocusMode(
                            CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);

                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);

            return true;
        }
    }

    private void startLoadingAnimation()
    {
        // Inflates the Overlay Layout to be displayed above the Camera View
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay,
                null, false);

        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);

        // By default
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_indicator);
        loadingDialogHandler.mLoadingDialogContainer
                .setVisibility(View.VISIBLE);

        VuforiaLayout.addView(mUILayout);
    }

    /**
     * SampleApplicationControl interface function
     * */
    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);

        vuforiaAppSession.onConfigurationChanged();
    }

    @Override
    public boolean doInitTrackers() {

        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager trackerManager = TrackerManager.getInstance();

        switch (MODE){
            case MODE_ImageTarget:
            case MODE_CloudReco:
            case MODE_VirtualButton:
            case MODE_UserDefinedTarget:
                Tracker tracker = trackerManager.initTracker(ObjectTracker.getClassType());
                if (tracker == null)
                {
                    Log.e(
                            LOGTAG,
                            "Tracker not initialized. Tracker already initialized or the camera is already started");
                    result = false;
                } else
                {
                    Log.i(LOGTAG, "Tracker successfully initialized");
                }
                break;
            case MODE_FrameMarkers:
                Tracker trackerBase = trackerManager.initTracker(MarkerTracker
                        .getClassType());
                MarkerTracker markerTracker = (MarkerTracker) (trackerBase);

                if (markerTracker == null)
                {
                    Log.e(
                            LOGTAG,
                            "Tracker not initialized. Tracker already initialized or the camera is already started");
                    result = false;
                } else
                {
                    Log.i(LOGTAG, "Tracker successfully initialized");
                }
                break;
        }

        return result;
    }

    @Override
    public boolean doLoadTrackersData() {
        // Get the object tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();

        try {
            switch (MODE){
                case MODE_ImageTarget:
                case MODE_VirtualButton:
                    ObjectTracker objectTracker = (ObjectTracker) trackerManager
                            .getTracker(ObjectTracker.getClassType());
                    if (objectTracker == null){
                        Log.d(LOGTAG, "Failed to load tracking data set because the ObjectTracker has not been initialized.");
                        return false;
                    }

                    if (mCurrentDataset == null){
                        Log.d(LOGTAG, "Failed to create a new tracking data.");
                        mCurrentDataset = objectTracker.createDataSet();
                    }

                    if (mCurrentDataset == null)
                        return false;

                    if (!mCurrentDataset.load(
                            mDatasetStrings.get(mCurrentDatasetSelectionIndex),
                            STORAGE_TYPE.STORAGE_APPRESOURCE)){
                        Log.d(LOGTAG, "Failed to load data set.");
                        return false;
                    }

                    if (!objectTracker.activateDataSet(mCurrentDataset)){
                        Log.d(LOGTAG, "Failed to activate data set.");
                        return false;
                    }

                    int numTrackables = mCurrentDataset.getNumTrackables();
                    for (int count = 0; count < numTrackables; count++)
                    {
                        Trackable trackable = mCurrentDataset.getTrackable(count);
                        if(mExtendedTracking)
                        {
                            trackable.startExtendedTracking();
                        }

                        String name = "Current Dataset : " + trackable.getName();
                        trackable.setUserData(name);
                    }
                    break;
                case MODE_CloudReco:
                    ObjectTracker objectTracker_ = (ObjectTracker) trackerManager
                            .getTracker(ObjectTracker.getClassType());

                    // Initialize target finder:
                    TargetFinder targetFinder = objectTracker_.getTargetFinder();
                    // Start initialization:
                    if (targetFinder.startInit(kAccessKey, kSecretKey))
                    {
                        targetFinder.waitUntilInitFinished();
                    }

                    int resultCode = targetFinder.getInitState();
                    if (resultCode != TargetFinder.INIT_SUCCESS)
                    {
                        if(resultCode == TargetFinder.INIT_ERROR_NO_NETWORK_CONNECTION)
                        {
                            mInitErrorCode = UPDATE_ERROR_NO_NETWORK_CONNECTION;
                        }
                        else
                        {
                            mInitErrorCode = UPDATE_ERROR_SERVICE_NOT_AVAILABLE;
                        }

                        Log.e(LOGTAG, "Failed to initialize target finder.");
                        return false;
                    }
                    break;
                case MODE_FrameMarkers:
                    MarkerTracker markerTracker = (MarkerTracker) trackerManager
                            .getTracker(MarkerTracker.getClassType());
                    if (markerTracker == null)
                        return false;

                    MarkerDataSet = new Marker[MarkerDataSetArray.size()];

                    for (int i=0; i<MarkerDataSetArray.size(); ++i){
                        MarkerDataSet[i] = markerTracker.createFrameMarker(
                                MarkerDataSetArray.get(i),
                                "Marker"+String.valueOf(i),
                                new Vec2F(50, 50));
                        if (MarkerDataSet[i] == null)
                        {
                            Log.e(LOGTAG, "Failed to create frame marker Q.");
                            return false;
                        }
                    }
                    Log.i(LOGTAG, "Successfully initialized MarkerTracker.");
                    break;
                case MODE_UserDefinedTarget:
                    // Get the image tracker:
                    ObjectTracker objectTracker_udt = (ObjectTracker) trackerManager
                            .getTracker(ObjectTracker.getClassType());
                    if (objectTracker_udt == null)
                    {
                        Log.d(
                                LOGTAG,
                                "Failed to load tracking data set because the ObjectTracker has not been initialized.");
                        return false;
                    }
                    // Create the data set:
                    dataSetUserDef = objectTracker_udt.createDataSet();
                    if (dataSetUserDef == null)
                    {
                        Log.d(LOGTAG, "Failed to create a new tracking data.");
                        return false;
                    }

                    if (!objectTracker_udt.activateDataSet(dataSetUserDef))
                    {
                        Log.d(LOGTAG, "Failed to activate data set.");
                        return false;
                    }
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(LOGTAG, "Successfully loaded and activated data set.");
        return true;
    }

    @Override
    public boolean doStartTrackers() {

        boolean result = true;
        // Start the tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();

        Vuforia.setHint(com.qualcomm.vuforia.HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS, MAX_TARGETS_COUNT);
        switch (MODE){
            case MODE_ImageTarget:
            case MODE_VirtualButton:
            case MODE_UserDefinedTarget:
                Tracker tracker = TrackerManager.getInstance().getTracker(
                        ObjectTracker.getClassType());
                if (tracker != null)
                    tracker.start();
                break;
            case MODE_CloudReco:
                // Start the tracker:
                ObjectTracker objectTracker = (ObjectTracker) trackerManager
                        .getTracker(ObjectTracker.getClassType());
                objectTracker.start();

                // Start cloud based recognition if we are in scanning mode:
                TargetFinder targetFinder = objectTracker.getTargetFinder();
                targetFinder.startRecognition();
                mFinderStarted = true;
                break;
            case MODE_FrameMarkers:
                MarkerTracker markerTracker = (MarkerTracker) trackerManager
                        .getTracker(MarkerTracker.getClassType());
                if (markerTracker != null)
                    markerTracker.start();
                break;
        }

        return result;
    }

    @Override
    public boolean doStopTrackers() {

        boolean result = true;

        TrackerManager trackerManager = TrackerManager.getInstance();

        switch (MODE){
            case MODE_ImageTarget:
            case MODE_VirtualButton:
            case MODE_UserDefinedTarget:
                Tracker tracker = trackerManager.getTracker(
                        ObjectTracker.getClassType());
                if (tracker != null)
                    tracker.stop();
                break;
            case MODE_CloudReco:
                ObjectTracker objectTracker = (ObjectTracker) trackerManager
                        .getTracker(ObjectTracker.getClassType());

                if(objectTracker != null)
                {
                    objectTracker.stop();

                    // Stop cloud based recognition:
                    TargetFinder targetFinder = objectTracker.getTargetFinder();
                    targetFinder.stop();
                    mFinderStarted = false;

                    // Clears the trackables
                    targetFinder.clearTrackables();
                }
                else
                {
                    result = false;
                }
                break;
            case MODE_FrameMarkers:
                MarkerTracker markerTracker = (MarkerTracker) trackerManager
                        .getTracker(MarkerTracker.getClassType());
                if (markerTracker != null)
                    markerTracker.stop();
                break;
        }

        return result;
    }

    @Override
    public boolean doUnloadTrackersData() {

        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        switch (MODE){
            case MODE_ImageTarget:
            case MODE_VirtualButton:
                ObjectTracker objectTracker = (ObjectTracker) tManager
                        .getTracker(ObjectTracker.getClassType());
                if (objectTracker == null){
                    Log.d(LOGTAG, "Failed to destroy the tracking data set because the ObjectTracker has not been initialized.");
                    return false;
                }

                if (mCurrentDataset != null && mCurrentDataset.isActive())
                {
                    if (objectTracker.getActiveDataSet().equals(mCurrentDataset)
                            && !objectTracker.deactivateDataSet(mCurrentDataset))
                    {
                        Log.d(LOGTAG, "Failed to destroy the tracking data set because the data set could not be deactivated.");
                        result = false;
                    } else if (!objectTracker.destroyDataSet(mCurrentDataset))
                    {
                        Log.d(LOGTAG, "Failed to destroy the tracking data set.");
                        result = false;
                    }

                    if (result)
                        Log.d(LOGTAG, "Successfully destroyed the data set.");

                    mCurrentDataset = null;
                }

                break;
            case MODE_CloudReco:
            case MODE_FrameMarkers:
                break;
            case MODE_UserDefinedTarget:
                ObjectTracker objectTracker_udt = (ObjectTracker) tManager
                        .getTracker(ObjectTracker.getClassType());
                if (objectTracker_udt == null)
                {
                    result = false;
                    Log.d(
                            LOGTAG,
                            "Failed to destroy the tracking data set because the ObjectTracker has not been initialized.");
                }

                if (dataSetUserDef != null)
                {
                    if (objectTracker_udt.getActiveDataSet() != null
                            && !objectTracker_udt.deactivateDataSet(dataSetUserDef))
                    {
                        Log.d(
                                LOGTAG,
                                "Failed to destroy the tracking data set because the data set could not be deactivated.");
                        result = false;
                    }

                    if (!objectTracker_udt.destroyDataSet(dataSetUserDef))
                    {
                        Log.d(LOGTAG, "Failed to destroy the tracking data set.");
                        result = false;
                    }

                    Log.d(LOGTAG, "Successfully destroyed the data set.");
                    dataSetUserDef = null;
                }
                break;
        }
        return result;
    }

    @Override
    public boolean doDeinitTrackers() {
        boolean result = true;

        if (MODE == MODE_UserDefinedTarget && refFreeFrame != null)
            refFreeFrame.deInit();

        TrackerManager.getInstance().deinitTracker(ObjectTracker.getClassType());
        return result;
    }

    @Override
    public void onInitARDone(SampleApplicationException exception) {
        if (exception == null) {
            initApplicationAR();

            VuforiaLayout.addView(mGlView);

            // Start the camera:
            try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
            } catch (SampleApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (result)
                mContAutofocus = true;
            else
                Log.e(LOGTAG, "Unable to enable continuous autofocus");

            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();

            // Hides the Loading Dialog
            loadingDialogHandler.sendEmptyMessage(HIDE_LOADING_DIALOG);

            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);

        } else {
            Log.e(LOGTAG, exception.getString());
            if(mInitErrorCode != 0)
            {
                showErrorMessage(mInitErrorCode,10, true);
            }
            else
            {
                showInitializationErrorMessage(exception.getString());
            }
        }
    }

    // Initializes AR application components.
    private void initApplicationAR()
    {
        if (MODE == BaseVuforiaActivity.MODE_UserDefinedTarget){
            refFreeFrame = new RefFreeFrame(this, vuforiaAppSession);
            refFreeFrame.init();
        }

        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();

        // Initialize the GLView with proper flags
        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);

        BaseVuforiaRender = new BaseVuforiaRender(this, vuforiaAppSession);
        switch (MODE){
            case MODE_ImageTarget:
                BaseVuforiaRender.setARMode(BaseVuforiaActivity.MODE_ImageTarget);
                break;
            case MODE_CloudReco:
                BaseVuforiaRender.setARMode(BaseVuforiaActivity.MODE_CloudReco);
                break;
            case MODE_FrameMarkers:
                BaseVuforiaRender.setARMode(BaseVuforiaActivity.MODE_FrameMarkers);
                break;
            case MODE_VirtualButton:
                BaseVuforiaRender.setARMode(BaseVuforiaActivity.MODE_VirtualButton);
                BaseVuforiaRender.setButtonCallback(buttonCallback);
            case MODE_UserDefinedTarget:
                BaseVuforiaRender.setARMode(BaseVuforiaActivity.MODE_UserDefinedTarget);
                break;
        }

        if (BaseVuforiaRender != null)
            mGlView.setRenderer(BaseVuforiaRender);

        if (MODE == BaseVuforiaActivity.MODE_UserDefinedTarget)
            addOverlayView(true);
    }

    public void showErrorMessage(int errorCode, double errorTime, boolean finishActivityOnError)
    {
        if (errorTime < (mLastErrorTime + 5.0) || errorCode == mlastErrorCode)
            return;

        mlastErrorCode = errorCode;
        mFinishActivityOnError = finishActivityOnError;

        runOnUiThread(new Runnable() {
            public void run() {
                if (mErrorDialog != null) {
                    mErrorDialog.dismiss();
                }

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(BaseVuforiaActivity.this);
                builder
                        .setMessage(
                                getStatusDescString(BaseVuforiaActivity.this.mlastErrorCode))
                        .setTitle(
                                getStatusTitleString(BaseVuforiaActivity.this.mlastErrorCode))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton(getString(R.string.button_OK),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (mFinishActivityOnError) {
                                            finish();
                                        } else {
                                            dialog.dismiss();
                                        }
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }

    private String getStatusDescString(int code)
    {
        if (code == UPDATE_ERROR_AUTHORIZATION_FAILED)
            return getString(R.string.UPDATE_ERROR_AUTHORIZATION_FAILED_DESC);
        if (code == UPDATE_ERROR_PROJECT_SUSPENDED)
            return getString(R.string.UPDATE_ERROR_PROJECT_SUSPENDED_DESC);
        if (code == UPDATE_ERROR_NO_NETWORK_CONNECTION)
            return getString(R.string.UPDATE_ERROR_NO_NETWORK_CONNECTION_DESC);
        if (code == UPDATE_ERROR_SERVICE_NOT_AVAILABLE)
            return getString(R.string.UPDATE_ERROR_SERVICE_NOT_AVAILABLE_DESC);
        if (code == UPDATE_ERROR_UPDATE_SDK)
            return getString(R.string.UPDATE_ERROR_UPDATE_SDK_DESC);
        if (code == UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE)
            return getString(R.string.UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE_DESC);
        if (code == UPDATE_ERROR_REQUEST_TIMEOUT)
            return getString(R.string.UPDATE_ERROR_REQUEST_TIMEOUT_DESC);
        if (code == UPDATE_ERROR_BAD_FRAME_QUALITY)
            return getString(R.string.UPDATE_ERROR_BAD_FRAME_QUALITY_DESC);
        else
        {
            return getString(R.string.UPDATE_ERROR_UNKNOWN_DESC);
        }
    }

    private String getStatusTitleString(int code)
    {
        if (code == UPDATE_ERROR_AUTHORIZATION_FAILED)
            return getString(R.string.UPDATE_ERROR_AUTHORIZATION_FAILED_TITLE);
        if (code == UPDATE_ERROR_PROJECT_SUSPENDED)
            return getString(R.string.UPDATE_ERROR_PROJECT_SUSPENDED_TITLE);
        if (code == UPDATE_ERROR_NO_NETWORK_CONNECTION)
            return getString(R.string.UPDATE_ERROR_NO_NETWORK_CONNECTION_TITLE);
        if (code == UPDATE_ERROR_SERVICE_NOT_AVAILABLE)
            return getString(R.string.UPDATE_ERROR_SERVICE_NOT_AVAILABLE_TITLE);
        if (code == UPDATE_ERROR_UPDATE_SDK)
            return getString(R.string.UPDATE_ERROR_UPDATE_SDK_TITLE);
        if (code == UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE)
            return getString(R.string.UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE_TITLE);
        if (code == UPDATE_ERROR_REQUEST_TIMEOUT)
            return getString(R.string.UPDATE_ERROR_REQUEST_TIMEOUT_TITLE);
        if (code == UPDATE_ERROR_BAD_FRAME_QUALITY)
            return getString(R.string.UPDATE_ERROR_BAD_FRAME_QUALITY_TITLE);
        else
        {
            return getString(R.string.UPDATE_ERROR_UNKNOWN_TITLE);
        }
    }

    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String message)
    {
        final String errorMessage = message;
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (mErrorDialog != null)
                {
                    mErrorDialog.dismiss();
                }

                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        BaseVuforiaActivity.this);
                builder
                        .setMessage(errorMessage)
                        .setTitle(getString(R.string.INIT_ERROR))
                        .setCancelable(false)
                        .setIcon(0)
                        .setPositiveButton(getString(R.string.button_OK),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                    }
                                });

                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }
    @Override
    public void onQCARUpdate(State state) {

        switch (MODE){
            case MODE_ImageTarget:
            case MODE_FrameMarkers:
            case MODE_VirtualButton:
                break;
            case MODE_CloudReco:
                TrackerManager trackerManager = TrackerManager.getInstance();

                ObjectTracker objectTracker = (ObjectTracker) trackerManager
                        .getTracker(ObjectTracker.getClassType());

                TargetFinder mFinder = objectTracker.getTargetFinder();

                final int statusCode = mFinder.updateSearchResults();

                if (statusCode < 0)
                {

                    boolean closeAppAfterError = (
                            statusCode == UPDATE_ERROR_NO_NETWORK_CONNECTION ||
                                    statusCode == UPDATE_ERROR_SERVICE_NOT_AVAILABLE);

                    showErrorMessage(statusCode, state.getFrame().getTimeStamp(), closeAppAfterError);

                } else if (statusCode == TargetFinder.UPDATE_RESULTS_AVAILABLE)
                {
                    if (mFinder.getResultCount() > 0)
                    {
                        TargetSearchResult result = mFinder.getResult(0);

                        if (result.getTrackingRating() > 0)
                        {
                            Trackable trackable = mFinder.enableTracking(result);

                            if (mExtendedTracking)
                                trackable.startExtendedTracking();
                        }
                    }
                }
                break;
            case MODE_UserDefinedTarget:
                TrackerManager trackerManager_udt = TrackerManager.getInstance();
                ObjectTracker objectTracker_udt = (ObjectTracker) trackerManager_udt
                        .getTracker(ObjectTracker.getClassType());

                if (refFreeFrame.hasNewTrackableSource())
                {
                    Log.d(LOGTAG,
                            "Attempting to transfer the trackable source to the dataset");

                    // Deactivate current dataset
                    objectTracker_udt.deactivateDataSet(objectTracker_udt.getActiveDataSet());

                    // Clear the oldest target if the dataset is full or the dataset
                    // already contains five user-defined targets.
                    if (dataSetUserDef.hasReachedTrackableLimit()
                            || dataSetUserDef.getNumTrackables() >= 5)
                        dataSetUserDef.destroy(dataSetUserDef.getTrackable(0));

                    if (mExtendedTracking && dataSetUserDef.getNumTrackables() > 0)
                    {
                        // We need to stop the extended tracking for the previous target
                        // so we can enable it for the new one
                        int previousCreatedTrackableIndex =
                                dataSetUserDef.getNumTrackables() - 1;

                        objectTracker_udt.resetExtendedTracking();
                        dataSetUserDef.getTrackable(previousCreatedTrackableIndex)
                                .stopExtendedTracking();
                    }

                    // Add new trackable source
                    Trackable trackable = dataSetUserDef
                            .createTrackable(refFreeFrame.getNewTrackableSource());

                    // Reactivate current dataset
                    objectTracker_udt.activateDataSet(dataSetUserDef);

                    if (mExtendedTracking)
                    {
                        trackable.startExtendedTracking();
                    }

                }
                break;
        }
    }

    public void startFinderIfStopped()
    {
        if(!mFinderStarted)
        {
            mFinderStarted = true;

            // Get the object tracker:
            TrackerManager trackerManager = TrackerManager.getInstance();
            ObjectTracker objectTracker = (ObjectTracker) trackerManager
                    .getTracker(ObjectTracker.getClassType());

            // Initialize target finder:
            TargetFinder targetFinder = objectTracker.getTargetFinder();

            targetFinder.clearTrackables();
            targetFinder.startRecognition();
        }
    }


    public void stopFinderIfStarted()
    {
        if(mFinderStarted)
        {
            mFinderStarted = false;

            // Get the object tracker:
            TrackerManager trackerManager = TrackerManager.getInstance();
            ObjectTracker objectTracker = (ObjectTracker) trackerManager
                    .getTracker(ObjectTracker.getClassType());

            // Initialize target finder:
            TargetFinder targetFinder = objectTracker.getTargetFinder();

            targetFinder.stop();
        }
    }

    // Shows error message in a system dialog box
    private void showErrorDialog()
    {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();

        mDialog = new AlertDialog.Builder(this).create();
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        };

        mDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                getString(R.string.button_OK), clickListener);

        mDialog.setTitle("Low Quality Image");

        String message = "The image has very little detail, please try another.";

        // Show dialog box with error message:
        mDialog.setMessage(message);
        mDialog.show();
    }

    // Shows error message in a system dialog box on the UI thread
    void showErrorDialogInUIThread()
    {
        runOnUiThread(new Runnable() {
            public void run() {
                showErrorDialog();
            }
        });
    }

    // Callback function called when the target creation finished
    void targetCreated()
    {
        // Hides the loading dialog
        loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

        if (refFreeFrame != null)
        {
            refFreeFrame.reset();
        }

    }


    // Creates a texture given the filename
    Texture createTexture(String nName)
    {
        return Texture.loadTextureFromApk(nName, getAssets());
    }

    // Adds the Overlay view to the GLView
    private void addOverlayView(boolean initLayout)
    {
        // Inflates the Overlay Layout to be displayed above the Camera View
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(
                R.layout.camera_overlay_udt, null, false);

        mUILayout.setVisibility(View.VISIBLE);

        // If this is the first time that the application runs then the
        // uiLayout background is set to BLACK color, will be set to
        // transparent once the SDK is initialized and camera ready to draw
        if (initLayout)
        {
            mUILayout.setBackgroundColor(Color.BLACK);
        }

        // Adds the inflated layout to the view
        addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Gets a reference to the bottom navigation bar
        mBottomBar = mUILayout.findViewById(R.id.bottom_bar);

        // Gets a reference to the Camera button
        mCameraButton = mUILayout.findViewById(R.id.camera_button);

        // Gets a reference to the loading dialog container
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
                .findViewById(R.id.loading_layout);

        startUserDefinedTargets();
        initializeBuildTargetModeViews();

        mUILayout.bringToFront();
    }

    boolean startUserDefinedTargets()
    {
        Log.d(LOGTAG, "startUserDefinedTargets");

        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) (trackerManager
                .getTracker(ObjectTracker.getClassType()));
        if (objectTracker != null)
        {
            ImageTargetBuilder targetBuilder = objectTracker
                    .getImageTargetBuilder();

            if (targetBuilder != null)
            {
                // if needed, stop the target builder
                if (targetBuilder.getFrameQuality() != ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_NONE)
                    targetBuilder.stopScan();

                objectTracker.stop();

                targetBuilder.startScan();

            }
        } else
            return false;

        return true;
    }

    // Initialize views
    private void initializeBuildTargetModeViews()
    {
        // Shows the bottom bar
        mBottomBar.setVisibility(View.VISIBLE);
        mCameraButton.setVisibility(View.VISIBLE);
    }

    // Button Camera clicked
    public void onCameraClick(View v)
    {
        if (isUserDefinedTargetsRunning())
        {
            // Shows the loading dialog
            loadingDialogHandler
                    .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);

            // Builds the new target
            startBuild();
        }
    }

    boolean isUserDefinedTargetsRunning()
    {
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());

        if (objectTracker != null)
        {
            ImageTargetBuilder targetBuilder = objectTracker
                    .getImageTargetBuilder();
            if (targetBuilder != null)
            {
                Log.e(LOGTAG, "Quality> " + targetBuilder.getFrameQuality());
                return (targetBuilder.getFrameQuality() != ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_NONE) ? true
                        : false;
            }
        }

        return false;
    }

    void startBuild()
    {
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());

        if (objectTracker != null)
        {
            ImageTargetBuilder targetBuilder = objectTracker
                    .getImageTargetBuilder();

            if (targetBuilder != null)
            {
                if (targetBuilder.getFrameQuality() == ImageTargetBuilder.FRAME_QUALITY.FRAME_QUALITY_LOW)
                {
                    showErrorDialogInUIThread();
                }

                String name;
                do
                {
                    name = "UserTarget-" + targetBuilderCounter;
                    Log.d(LOGTAG, "TRYING " + name);
                    targetBuilderCounter++;
                } while (!targetBuilder.build(name, 320.0f));

                refFreeFrame.setCreating();
            }
        }
    }

    void updateRendering()
    {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        refFreeFrame.initGL(metrics.widthPixels, metrics.heightPixels);
    }
}
