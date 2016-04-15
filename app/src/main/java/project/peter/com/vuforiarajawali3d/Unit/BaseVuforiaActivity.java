package project.peter.com.vuforiarajawali3d.Unit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.ObjectTracker;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.TargetFinder;
import com.qualcomm.vuforia.TargetSearchResult;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vuforia;

import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.ArrayList;

import project.peter.com.vuforiarajawali3d.R;
import project.peter.com.vuforiarajawali3d.Render.mCloudRecoRender;
import project.peter.com.vuforiarajawali3d.Render.mRajawaliRender;
import project.peter.com.vuforiarajawali3d.Render.mVuforiaRender;
import project.peter.com.vuforiarajawali3d.SampleApplication.SampleApplicationControl;
import project.peter.com.vuforiarajawali3d.SampleApplication.SampleApplicationException;
import project.peter.com.vuforiarajawali3d.SampleApplication.SampleApplicationSession;
import project.peter.com.vuforiarajawali3d.SampleApplication.utils.LoadingDialogHandler;
import project.peter.com.vuforiarajawali3d.SampleApplication.utils.SampleApplicationGLView;

/**
 * Created by linweijie on 4/13/16.
 */
public class BaseVuforiaActivity extends Activity implements SampleApplicationControl {

    public final static int MODE_ImageTarget = 0;
    public final static int MODE_CloudReco = 1;

    private int MODE = MODE_ImageTarget;







    private static final String LOGTAG = "BaseVuforia";
    SampleApplicationSession vuforiaAppSession;

    private SampleApplicationGLView mGlView;
    private RelativeLayout mUILayout;
    private GestureDetector mGestureDetector;
    public LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);
    private AlertDialog mErrorDialog;
    boolean mIsDroidDevice = false;
    private boolean mExtendedTracking = false;
    static final int HIDE_LOADING_DIALOG = 0;
    static final int SHOW_LOADING_DIALOG = 1;

    // ImageTarget
    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 0;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();
    private boolean mContAutofocus = false;

    // CloudReco
    static final int INIT_SUCCESS = 2;
    static final int INIT_ERROR_NO_NETWORK_CONNECTION = -1;
    static final int INIT_ERROR_SERVICE_NOT_AVAILABLE = -2;
    static final int UPDATE_ERROR_AUTHORIZATION_FAILED = -1;
    static final int UPDATE_ERROR_PROJECT_SUSPENDED = -2;
    static final int UPDATE_ERROR_NO_NETWORK_CONNECTION = -3;
    static final int UPDATE_ERROR_SERVICE_NOT_AVAILABLE = -4;
    static final int UPDATE_ERROR_BAD_FRAME_QUALITY = -5;
    static final int UPDATE_ERROR_UPDATE_SDK = -6;
    static final int UPDATE_ERROR_TIMESTAMP_OUT_OF_RANGE = -7;
    static final int UPDATE_ERROR_REQUEST_TIMEOUT = -8;

    boolean mFinderStarted = false;
    boolean mStopFinderIfStarted = false;

    private static String kAccessKey;
    private static String kSecretKey;

    // Error message handling:
    private int mlastErrorCode = 0;
    private int mInitErrorCode = 0;
    private boolean mFinishActivityOnError;
    private double mLastErrorTime;

    //
    public static mRajawaliRender mRajawaliRender;

    public void setARMode(int mode){
        this.MODE = mode;
    }
    public void setDatasetStrings(ArrayList<String> datasetStrings){
        this.mDatasetStrings = datasetStrings;
    }
    public void setCloudTargetsKey(String accessKey, String secretKey){
        this.kAccessKey = accessKey;
        this.kSecretKey = secretKey;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        }

        vuforiaAppSession = new SampleApplicationSession(this);
        startLoadingAnimation();

        vuforiaAppSession
                .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Creates the GestureDetector listener for processing double tap
        mGestureDetector = new GestureDetector(this, new GestureListener());

        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
                "droid");

        //
        final RajawaliSurfaceView surface = new RajawaliSurfaceView(this);
        surface.setFrameRate(60.0);
        surface.setRenderMode(IRajawaliSurface.RENDERMODE_WHEN_DIRTY);

        // Add mSurface to your root view
        addContentView(surface, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT));

        mRajawaliRender = new mRajawaliRender(this);
        surface.setSurfaceRenderer(mRajawaliRender);
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

        addContentView(mUILayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

    }

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
        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Indicate if the trackers were initialized correctly
        boolean result = true;

        tracker = tManager.initTracker(ObjectTracker.getClassType());
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

        return result;
    }

    @Override
    public boolean doLoadTrackersData() {
        // Get the object tracker:
        TrackerManager trackerManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) trackerManager
                .getTracker(ObjectTracker.getClassType());

        try {
            switch (MODE){
                case MODE_ImageTarget:
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
                    // Initialize target finder:
                    TargetFinder targetFinder = objectTracker.getTargetFinder();
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

            addContentView(mGlView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));

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

        // Setups the Renderer of the GLView
        GLSurfaceView.Renderer mRenderer = null;

        switch (MODE){
            case MODE_ImageTarget:
                mRenderer = new mVuforiaRender(this, vuforiaAppSession);
                break;
            case MODE_CloudReco:
                mRenderer = new mCloudRecoRender(this, vuforiaAppSession);
                break;
        }

        if (mRenderer != null)
            mGlView.setRenderer(mRenderer);

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

                TargetFinder finder = objectTracker.getTargetFinder();

                final int statusCode = finder.updateSearchResults();

                if (statusCode < 0)
                {

                    boolean closeAppAfterError = (
                            statusCode == UPDATE_ERROR_NO_NETWORK_CONNECTION ||
                                    statusCode == UPDATE_ERROR_SERVICE_NOT_AVAILABLE);

                    showErrorMessage(statusCode, state.getFrame().getTimeStamp(), closeAppAfterError);

                } else if (statusCode == TargetFinder.UPDATE_RESULTS_AVAILABLE)
                {
                    if (finder.getResultCount() > 0)
                    {
                        TargetSearchResult result = finder.getResult(0);

                        if (result.getTrackingRating() > 0)
                        {
                            Trackable trackable = finder.enableTracking(result);

                            if (mExtendedTracking)
                                trackable.startExtendedTracking();
                        }
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

}
