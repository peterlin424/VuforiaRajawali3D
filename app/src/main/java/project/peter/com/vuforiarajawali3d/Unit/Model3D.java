package project.peter.com.vuforiarajawali3d.Unit;

import java.util.ArrayList;

/**
 * Created by linweijie on 4/18/16.
 */
public class Model3D {

    private int resId_obj;
    private ArrayList<Integer> resId_textures = new ArrayList<>();

    private float obj_scale = 1.0f;
    private float obj_translate_x = 0.0f;
    private float obj_translate_y = 0.0f;

    private float obj_rotate_angle = 0.0f;

    public Model3D(int resId_obj) {
        this.resId_obj = resId_obj;
    }

    public int getResId_obj() {
        return resId_obj;
    }
    public ArrayList<Integer> getResId_textures() {
        return resId_textures;
    }
    public void addTexture(int resId){
        resId_textures.add(resId);
    }

    public float getObj_scale() {
        return obj_scale;
    }
    public void setObj_scale(float obj_scale) {
        this.obj_scale = obj_scale;
    }

    public float getObj_translate_x() {
        return obj_translate_x;
    }
    public void setObj_translate_x(float obj_translate_x) {
        this.obj_translate_x = obj_translate_x;
    }

    public float getObj_translate_y() {
        return obj_translate_y;
    }
    public void setObj_translate_y(float obj_translate_y) {
        this.obj_translate_y = obj_translate_y;
    }

    public float getObj_rotate_angle() {
        return obj_rotate_angle;
    }
    public void setObj_rotate_angle(float obj_rotate_angle) {
        this.obj_rotate_angle = obj_rotate_angle;
    }
}
