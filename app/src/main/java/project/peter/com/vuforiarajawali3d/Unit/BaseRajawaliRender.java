package project.peter.com.vuforiarajawali3d.Unit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.MotionEvent;

import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.lights.PointLight;

import java.util.ArrayList;

import project.peter.com.vuforiarajawali3d.R;

/**
 * Created by linweijie on 4/8/16.
 */
public class BaseRajawaliRender extends org.rajawali3d.renderer.RajawaliRenderer {

    private static final String LOGTAG = "BaseRajawaliRender";

    private int FRAME_RATE = 60;

    private Activity activity;
    private Context context;
    private DirectionalLight directionalLight;
    private ArcballCamera arcball;

    private ArrayList<Model3D> model3DArrayList = new ArrayList<>();

    public BaseRajawaliRender(Context context) {
        super(context);
        this.context = context;
        this.activity = (Activity)context;
        setFrameRate(FRAME_RATE);
    }

    /**
     * Vuforia 控制用
     * */
    public void moveObject3D(
            int index,
            float[] vpMatrix,
            float[] projMatrix,
            float[] vMatrix){

        if (index>=model3DArrayList.size()){
            if (model3DArrayList.size()==1){
                model3DArrayList.get(0).setVpMatrix(vpMatrix);
                model3DArrayList.get(0).setProjMatrix(projMatrix);
                model3DArrayList.get(0).setvMatrix(vMatrix);
                return;
            } else {
                return;
            }
        }
        model3DArrayList.get(index).setVpMatrix(vpMatrix);
        model3DArrayList.get(index).setProjMatrix(projMatrix);
        model3DArrayList.get(index).setvMatrix(vMatrix);
    }

    /**
     * 載入模型陣列用
     * */
    public void setModel3DArrayList(ArrayList<Model3D> model3DArrayList) {
        this.model3DArrayList = model3DArrayList;
    }
    public void HideAllModel(){
        for (int i=0; i<model3DArrayList.size(); ++i){
            setVisiableModelByIndex(i, false);
        }
    }
    public void setVisiableModelByIndex(int index, boolean visiable){

        if (index>=model3DArrayList.size()){
            if (model3DArrayList.size()==1){
                model3DArrayList.get(0).setVisiable(visiable);
                return;
            } else {
                return;
            }
        }
        model3DArrayList.get(index).setVisiable(visiable);
    }

    public float getObjScale(int index){
        if (index>=model3DArrayList.size()){
            if (model3DArrayList.size()==1){
                return model3DArrayList.get(0).getObj_scale();
            } else {
                return 0.0f;
            }
        }
        return model3DArrayList.get(index).getObj_scale();
    }
    public float getObjTranslateX(int index){
        if (index>=model3DArrayList.size()){
            if (model3DArrayList.size()==1){
                return model3DArrayList.get(0).getObj_translate_x();
            } else {
                return 0.0f;
            }
        }
        return model3DArrayList.get(index).getObj_translate_x();
    }
    public float getObjTranslateY(int index){
        if (index>=model3DArrayList.size()){
            if (model3DArrayList.size()==1){
                return model3DArrayList.get(0).getObj_translate_y();
            } else {
                return 0.0f;
            }
        }
        return model3DArrayList.get(index).getObj_translate_y();
    }
    public float getObjRotateAngle(int index){
        if (index>=model3DArrayList.size()){
            if (model3DArrayList.size()==1){
                return model3DArrayList.get(0).getObj_rotate_angle();
            } else {
                return 0.0f;
            }
        }
        return model3DArrayList.get(index).getObj_rotate_angle();
    }

    /**
     * RajawaliRenderer Override Function
     * */
    @Override
    protected void initScene() {

        showLoadingDialog();

        /** 定向光 */
        directionalLight = new DirectionalLight(0.1f, -10.0f, 0.0f); // 座標
        directionalLight.setColor(1.0f, 1.0f, 1.0f);             // 燈光色
        directionalLight.setPower(2);                            // 燈光強度
        getCurrentScene().addLight(directionalLight);

        PointLight mLight = new PointLight();//物件本身亮度
        mLight.setPosition(0.1f, 40.0f, 0.0f);//設定光亮動畫位置
        mLight.setPower(25); //3D物件亮度調整
        getCurrentScene().addLight(mLight);

        /** 視角相機設定 */
        arcball = new ArcballCamera(mContext, ((Activity)mContext).findViewById(R.id.rajawali_layout));
        arcball.setPosition(0.1f, 40.0f, 0.0f);
        getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), arcball);

        for (int i=0; i<model3DArrayList.size(); ++i){
            model3DArrayList.get(i).parse(this);
            model3DArrayList.get(i).setVisiable(false);
        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
//        Log.d(LOGTAG, "onOffsetsChanged");
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
//        Log.d(LOGTAG, "onTouchEvent");
    }

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);

        for (int i=0; i<model3DArrayList.size(); ++i){
            model3DArrayList.get(i).render(arcball);
        }
    }

    private void showLoadingDialog(){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                ProgressDialog myDialog;
                myDialog = ProgressDialog.show(context, "Loading", "please wait...", true);
                final ProgressDialog finalMyDialog = myDialog;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            finalMyDialog.dismiss();
                        }
                    }
                }).start();
            }
        });
    }
}