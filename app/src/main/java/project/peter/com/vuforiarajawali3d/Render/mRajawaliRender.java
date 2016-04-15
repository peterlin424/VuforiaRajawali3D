package project.peter.com.vuforiarajawali3d.Render;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;

import project.peter.com.vuforiarajawali3d.Unit.ArcballCamera;
import project.peter.com.vuforiarajawali3d.R;

/**
 * Created by linweijie on 4/8/16.
 */
public class mRajawaliRender extends org.rajawali3d.renderer.RajawaliRenderer {

    public Context context;
    private DirectionalLight directionalLight;
    private Object3D WatchObj;
    private ArcballCamera arcball;
    private Material material;
    private Matrix4 vpMatrix;
    private Matrix4 projMatrix;
    private Matrix4 vMatrix;

    public mRajawaliRender(Context context) {
        super(context);
        this.context = context;
        setFrameRate(60);
    }

    // TODO 判斷出辨識物
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
            this.WatchObj.setVisible(show);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

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

            /** debug 隔線 */
//            DebugVisualizer debugViz = new DebugVisualizer(this);
//            debugViz.addChild(new GridFloor());
//            getCurrentScene().addChild(debugViz);

            /** 載入模型 */
//            final LoaderOBJ parser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.watch_obj);
            final LoaderOBJ parser = new LoaderOBJ(mContext.getResources(), mTextureManager, R.raw.roadcar_obj);
            parser.parse();

            WatchObj = parser.getParsedObject();

            /** 模型材質貼皮 */
            material = new Material();
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
//            material.addTexture(new Texture("Watch1", R.drawable.watch001));
//            material.addTexture(new Texture("Watch2", R.drawable.watch002));
            material.addTexture(new Texture("u1", R.drawable.u1));
            material.addTexture(new Texture("u2", R.drawable.u2));
            WatchObj.setMaterial(material);

            /** 視角相機設定 */
            arcball = new ArcballCamera(mContext, ((Activity)mContext).findViewById(R.id.drawer_layout));
            arcball.setPosition(0.1f, 40.0f, 0.0f);
            getCurrentScene().replaceAndSwitchCamera(getCurrentCamera(), arcball);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
//        Log.d("Peter", "onOffsetsChanged");
    }

    @Override
    public void onTouchEvent(MotionEvent event) {
//        Log.d("Peter", "onTouchEvent");
    }

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
//        Log.d("Peter", "onRender");

        try {
            WatchObj.render(arcball, vpMatrix, projMatrix, vMatrix, null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
