package project.peter.com.vuforiarajawali3d.Unit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;

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
    private Object3D Object3D;
    private ArcballCamera arcball;
    private Material material;
    private Matrix4 vpMatrix;
    private Matrix4 projMatrix;
    private Matrix4 vMatrix;

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
            float[] vpMatrix,
            float[] projMatrix,
            float[] vMatrix){

        this.vpMatrix = new Matrix4().setAll(vpMatrix);
        this.projMatrix  = new Matrix4().setAll(projMatrix);
        this.vMatrix = new Matrix4().setAll(vMatrix);
    }

    public void isShowObject(boolean show){
        try {
            this.Object3D.setVisible(show);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 載入模型陣列用
     * */
    public void setModel3DArrayList(ArrayList<Model3D> model3DArrayList) {
        this.model3DArrayList = model3DArrayList;
    }

    public void setShowModelByIndex(int index){
        if (model3DArrayList.size()>0){
            if (model3DArrayList.size()-1<index){
                Log.d(LOGTAG, "model3DArrayList size is wrong");
                return;
            }

            showLoadingDialog();

            try{
                Model3D temp = model3DArrayList.get(index);
                /** 設定模型 */
                // 讀入檔案
                LoaderOBJ parser = new LoaderOBJ(mContext.getResources(), mTextureManager, temp.getResId_obj());
                parser.parse();

                Object3D = parser.getParsedObject();

                // 材質貼皮
                material = new Material();
                material.enableLighting(true);
                material.setDiffuseMethod(new DiffuseMethod.Lambert());

                for (int i=0; i<temp.getResId_textures().size(); ++i){
                    material.addTexture(new Texture("Object3D", temp.getResId_textures().get(i)));
                }
                Object3D.setMaterial(material);

                BaseVuforiaActivity.BaseVuforiaRender.setOBJECT_SCALE_FLOAT(temp.getObj_scale());
                BaseVuforiaActivity.BaseVuforiaRender.setOBJECT_TRANSLATE_X_FLOAT(temp.getObj_translate_x());
                BaseVuforiaActivity.BaseVuforiaRender.setOBJECT_TRANSLATE_Y_FLOAT(temp.getObj_translate_y());
                BaseVuforiaActivity.BaseVuforiaRender.setOBJECT_ROTATE_ANGLE_FLOAT(temp.getObj_rotate_angle());

            } catch (Exception e){
                e.printStackTrace();
            } catch (OutOfMemoryError outOfMemoryError){
                outOfMemoryError.printStackTrace();
            }
        } else {
            Log.d(LOGTAG, "model3DArrayList size is zero");
        }
    }

    private void showLoadingDialog(){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                ProgressDialog myDialog = new ProgressDialog(context);
                myDialog = ProgressDialog.show(context, "Loading", "please wait...", true);
                final ProgressDialog finalMyDialog = myDialog;
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try{
                            Thread.sleep(2000);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                        finally{
                            finalMyDialog.dismiss();
                        }
                    }
                }).start();
            }
        });
    }

    /**
     * RajawaliRenderer Override Function
     * */
    @Override
    protected void initScene() {
        try {

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

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
        Log.d(LOGTAG, "onOffsetsChanged");
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        Log.d(LOGTAG, "onTouchEvent");
    }

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);

        try {
            Object3D.render(arcball, vpMatrix, projMatrix, vMatrix, null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
