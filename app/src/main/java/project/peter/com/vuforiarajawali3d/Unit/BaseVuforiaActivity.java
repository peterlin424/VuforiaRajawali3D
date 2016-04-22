package project.peter.com.vuforiarajawali3d.Unit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ImageTarget;
import com.qualcomm.vuforia.Marker;
import com.qualcomm.vuforia.MarkerTracker;
import com.qualcomm.vuforia.ObjectTracker;
import com.qualcomm.vuforia.Rectangle;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.TargetFinder;
import com.qualcomm.vuforia.TargetSearchResult;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.VirtualButton;
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

/**
 * Created by linweijie on 4/13/16.
 */
public class BaseVuforiaActivity extends AppCompatActivity implements SampleApplicationControl {

    private static final String LOGTAG = "BaseVuforiaActivity";

    public final static int MODE_ImageTarget = 0;
    public final static int MODE_CloudReco = 1;
    public final static int MODE_FrameMarkers = 2;
    public final static int MODE_CubiodBox = 3;
    public final static int MODE_Cylinder = 4;
    private int MODE = MODE_ImageTarget;

    private boolean PLUGIN_VirtualButtons = false;

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
    public void setPLUGIN_VirtualButtons(boolean plugin){
        this.PLUGIN_VirtualButtons = plugin;
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

    /**
     * onCreate 後所需的設定：顯示 3D 模型 list, 切換要顯示的 3D 模型
     * */
    public void setModel3DArrayList(ArrayList<Model3D> model3DArrayList){
        BaseRajawaliRender.setModel3DArrayList(model3DArrayList);
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
//        Log.d(LOGTAG, "onConfigurationChanged");
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
                    ObjectTracker objectTracker = (ObjectTracker) trackerManager
                            .getTracker(ObjectTracker.getClassType());
                    if (objectTracker == null)
                        return false;

                    if (mCurrentDataset == null)
                        mCurrentDataset = objectTracker.createDataSet();

                    if (mCurrentDataset == null)
                        return false;

                    if (!mCurrentDataset.load(
                            mDatasetStrings.get(mCurrentDatasetSelectionIndex),
                            STORAGE_TYPE.STORAGE_APPRESOURCE))
                        return false;

                    if (!objectTracker.activateDataSet(mCurrentDataset))
                        return false;

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
            }
        }catch (Exception e){
            e.printStackTrace();
        }

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
                ObjectTracker objectTracker = (ObjectTracker) tManager
                        .getTracker(ObjectTracker.getClassType());
                if (objectTracker == null)
                    return false;

                if (mCurrentDataset != null && mCurrentDataset.isActive())
                {
                    if (objectTracker.getActiveDataSet().equals(mCurrentDataset)
                            && !objectTracker.deactivateDataSet(mCurrentDataset))
                    {
                        result = false;
                    } else if (!objectTracker.destroyDataSet(mCurrentDataset))
                    {
                        result = false;
                    }

                    mCurrentDataset = null;
                }


                break;
            case MODE_CloudReco:
            case MODE_FrameMarkers:
                break;
        }
        return result;
    }

    @Override
    public boolean doDeinitTrackers() {
        boolean result = true;
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
        }

        if (BaseVuforiaRender != null)
            mGlView.setRenderer(BaseVuforiaRender);
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
            case MODE_FrameMarkers:
                break;
        }
    }

    // Create/destroy a Virtual Button at runtime
    //
    // Note: This will NOT work if the tracker is active!
    boolean toggleVirtualButton(ImageTarget imageTarget, String name,
                                float left, float top, float right, float bottom)
    {
        Log.d(LOGTAG, "toggleVirtualButton");

        boolean buttonToggleSuccess = false;

        VirtualButton virtualButton = imageTarget.getVirtualButton(name);
        if (virtualButton != null)
        {
            Log.d(LOGTAG, "Destroying Virtual Button> " + name);
            buttonToggleSuccess = imageTarget
                    .destroyVirtualButton(virtualButton);
        } else
        {
            Log.d(LOGTAG, "Creating Virtual Button> " + name);
            Rectangle vbRectangle = new Rectangle(left, top, right, bottom);
            VirtualButton virtualButton2 = imageTarget.createVirtualButton(
                    name, vbRectangle);

            if (virtualButton2 != null)
            {
                // This is just a showcase. The values used here a set by
                // default on Virtual Button creation
                virtualButton2.setEnabled(true);
                virtualButton2.setSensitivity(VirtualButton.SENSITIVITY.MEDIUM);
                buttonToggleSuccess = true;
            }
        }

        return buttonToggleSuccess;
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

}
