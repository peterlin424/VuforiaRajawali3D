package project.peter.com.vuforiarajawali3d.Unit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.MotionEvent;

import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.lights.PointLight;

import java.util.ArrayList;

import project.peter.com.vuforiarajawali3d.R;
import project.peter.com.vuforiarajawali3d.Unit.rajawali.ArcballCamera;

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

    private CollisionCallback collisionCallback;

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

        if (index>(model3DArrayList.size()-1)){
            index = model3DArrayList.size()-1;
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
    public ArrayList<Model3D> getModel3DArrayList() {
        return this.model3DArrayList;
    }
    public void HideAllModel(){
        for (int i=0; i<model3DArrayList.size(); ++i){
            setVisibleModelByIndex(i, false);
        }
    }
    public void setVisibleModelByIndex(int index, boolean visiable){

        model3DArrayList.get(index).setVisiable(visiable);
    }

    public void setVisibleModelByBoolenArray(ArrayList<Boolean> visibleRecorder){

        boolean result = false;
        // visibleRecorder.size > model3DArrayList.size
        if (visibleRecorder.size() > model3DArrayList.size()){
            for (int i=0; i<visibleRecorder.size(); ++i){

                if (i>=(model3DArrayList.size()-1)){

                    if (visibleRecorder.get(i))
                        result = true;

                    setVisibleModelByIndex(model3DArrayList.size()-1, result);
                } else {
                    setVisibleModelByIndex(i, visibleRecorder.get(i));
                }
            }
        } else {
            // visibleRecorder.size <= model3DArrayList.size
            for (int i=0; i<visibleRecorder.size(); ++i){
                setVisibleModelByIndex(i, visibleRecorder.get(i));
            }
        }
    }

    public float getObjScale(int index){
        if (index>=model3DArrayList.size()){
            if (model3DArrayList.size()==1){
                return model3DArrayList.get(0).getObj_scale();
            } else {
                return model3DArrayList.get(model3DArrayList.size()-1).getObj_scale();
            }
        }
        return model3DArrayList.get(index).getObj_scale();
    }
    public float getObjTranslateX(int index){
        if (index>=model3DArrayList.size()){
            if (model3DArrayList.size()==1){
                return model3DArrayList.get(0).getObj_translate_x();
            } else {
                return model3DArrayList.get(model3DArrayList.size()-1).getObj_translate_x();
            }
        }
        return model3DArrayList.get(index).getObj_translate_x();
    }
    public float getObjTranslateY(int index){
        if (index>=model3DArrayList.size()){
            if (model3DArrayList.size()==1){
                return model3DArrayList.get(0).getObj_translate_y();
            } else {
                return model3DArrayList.get(model3DArrayList.size()-1).getObj_translate_y();
            }
        }
        return model3DArrayList.get(index).getObj_translate_y();
    }
    public float getObjRotateAngle(int index){
        if (index>=model3DArrayList.size()){
            if (model3DArrayList.size()==1){
                return model3DArrayList.get(0).getObj_rotate_angle();
            } else {
                return model3DArrayList.get(model3DArrayList.size()-1).getObj_rotate_angle();
            }
        }
        return model3DArrayList.get(index).getObj_rotate_angle();
    }
    public float getObjRotateX(int index){
        if (index>=model3DArrayList.size()){
            if (model3DArrayList.size()==1){
                return model3DArrayList.get(0).getObj_rotate_x();
            } else {
                return model3DArrayList.get(model3DArrayList.size()-1).getObj_rotate_x();
            }
        }
        return model3DArrayList.get(index).getObj_rotate_x();
    }
    public float getObjRotateY(int index){
        if (index>=model3DArrayList.size()){
            if (model3DArrayList.size()==1){
                return model3DArrayList.get(0).getObj_rotate_y();
            } else {
                return model3DArrayList.get(model3DArrayList.size()-1).getObj_rotate_y();
            }
        }
        return model3DArrayList.get(index).getObj_rotate_y();
    }
    public float getObjRotateZ(int index){
        if (index>=model3DArrayList.size()){
            if (model3DArrayList.size()==1){
                return model3DArrayList.get(0).getObj_rotate_z();
            } else {
                return model3DArrayList.get(model3DArrayList.size()-1).getObj_rotate_z();
            }
        }
        return model3DArrayList.get(index).getObj_rotate_z();
    }

    /**
     * 碰撞偵測
     * */
    public void setCollisionCallback(CollisionCallback collisionCallback) {
        this.collisionCallback = collisionCallback;
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

        // 碰撞處理
        for (int i=0; i<model3DArrayList.size()-1; i++){
            if (model3DArrayList.get(i).getCanCollision()){
                for (int j=i+1; j<model3DArrayList.size(); j++){
                    if (model3DArrayList.get(i).isVisiable() && model3DArrayList.get(j).isVisiable()){
                        if (model3DArrayList.get(i).isCollision(model3DArrayList.get(j).getCollBounding())){
                            if (collisionCallback!=null)
                                collisionCallback.ObjectCollision(i, j);
                        }
                    }
                }
            }
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
