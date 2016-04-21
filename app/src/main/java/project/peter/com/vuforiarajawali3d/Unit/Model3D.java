package project.peter.com.vuforiarajawali3d.Unit;

import android.content.Context;

import org.rajawali3d.Camera;
import org.rajawali3d.Object3D;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.renderer.RajawaliRenderer;

import java.util.ArrayList;

/**
 * Created by linweijie on 4/18/16.
 */
public class Model3D {

    private Context context;
    private int resId_obj;

    private ArrayList<Integer> resId_textures = new ArrayList<>();

    private float obj_scale = 1.0f;
    private float obj_translate_x = 0.0f;
    private float obj_translate_y = 0.0f;
    private float obj_rotate_angle = 0.0f;

    private boolean visiable = false;

    private Object3D Object3D;
    private Material material;
    private Matrix4 vpMatrix;
    private Matrix4 projMatrix;
    private Matrix4 vMatrix;

    public void setVpMatrix(float[] vpMatrix) {
        this.vpMatrix = new Matrix4().setAll(vpMatrix);
    }
    public void setProjMatrix(float[] projMatrix) {
        this.projMatrix = new Matrix4().setAll(projMatrix);
    }
    public void setvMatrix(float[] vMatrix) {
        this.vMatrix = new Matrix4().setAll(vMatrix);
    }

    public Model3D(Context c, int resId_obj) {
        this.context = c;
        this.resId_obj = resId_obj;
    }

    public void addTexture(int resId){
        resId_textures.add(resId);
    }

    public float getObj_scale() {
        return obj_scale;
    }
    public float getObj_translate_x() {
        return obj_translate_x;
    }
    public float getObj_translate_y() {
        return obj_translate_y;
    }
    public float getObj_rotate_angle() {
        return obj_rotate_angle;
    }
    public void setObj_scale(float obj_scale) {
        this.obj_scale = obj_scale;
    }
    public void setObj_translate_x(float obj_translate_x) {
        this.obj_translate_x = obj_translate_x;
    }
    public void setObj_translate_y(float obj_translate_y) {
        this.obj_translate_y = obj_translate_y;
    }
    public void setObj_rotate_angle(float obj_rotate_angle) {
        this.obj_rotate_angle = obj_rotate_angle;
    }

    public boolean isVisiable() {
        return visiable;
    }
    public void setVisiable(boolean visiable) {
        this.visiable = visiable;

        try {
            Object3D.setVisible(visiable);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void parse(RajawaliRenderer renderer){

        try{
            /** 設定模型 */
            // 讀入檔案
            LoaderOBJ parser = new LoaderOBJ(context.getResources(), renderer.getTextureManager(), resId_obj);
            parser.parse();

            Object3D = parser.getParsedObject();

            // 材質貼皮
            material = new Material();
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());

            for (int i=0; i<resId_textures.size(); ++i){
                material.addTexture(new Texture("Object3D", resId_textures.get(i)));
            }
            Object3D.setMaterial(material);
        } catch (Exception e){
            e.printStackTrace();
        } catch (OutOfMemoryError outOfMemoryError){
            outOfMemoryError.printStackTrace();
        }
    }

    public void render(Camera camera){
        try {
            Object3D.render(camera, vpMatrix, projMatrix, vMatrix, null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
