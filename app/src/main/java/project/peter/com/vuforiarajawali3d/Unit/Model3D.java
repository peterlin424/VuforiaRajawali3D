package project.peter.com.vuforiarajawali3d.Unit;

import android.content.Context;
import android.opengl.GLES20;

import org.rajawali3d.Camera;
import org.rajawali3d.Object3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationObject3D;
import org.rajawali3d.animation.mesh.SkeletalAnimationSequence;
import org.rajawali3d.bounds.IBoundingVolume;
import org.rajawali3d.loader.LoaderOBJ;
import org.rajawali3d.loader.md5.LoaderMD5Anim;
import org.rajawali3d.loader.md5.LoaderMD5Mesh;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.primitives.Cube;
import org.rajawali3d.renderer.RajawaliRenderer;

import java.util.ArrayList;

/**
 * Created by linweijie on 4/18/16.
 */
public class Model3D {

    public final static int LOAD_MTL_OBJ = 0;
    public final static int LOAD_MD5_MASH = 1;

    private int MODE = LOAD_MTL_OBJ;

    private boolean visiable = false;
    private boolean canCollision = false;
    private boolean isShowBounding = false;
    private int CollBoxColor = 0x00000000;

    private IBoundingVolume mCollBounding;

    private int AnimFPS = 24;

    private Context context;
    private int resId_obj;

    private Material material;
    private Matrix4 vpMatrix, projMatrix, vMatrix;
    private ArrayList<Integer> resId_textures = new ArrayList<>();
    private ArrayList<Integer> resId_anims = new ArrayList<>();

    private float obj_scale = 1.0f;
    private float obj_translate_x = 0.0f;
    private float obj_translate_y = 0.0f;
    private float obj_rotate_angle = 0.0f;

    private float coll_scale_x = 1.0f;
    private float coll_scale_y = 1.0f;
    private float coll_scale_z = 1.0f;
    private float coll_pos_x = 0.0f;
    private float coll_pos_y = 0.0f;
    private float coll_pos_z = 0.0f;

    private Object3D Object3D;

    public org.rajawali3d.Object3D getmCollCube() {
        return mCollCube;
    }

    private Object3D mCollCube;
    private SkeletalAnimationObject3D ObjectMash;
    private ArrayList<SkeletalAnimationSequence> AnimsList = new ArrayList<>();

    public int getMODE() {
        return this.MODE;
    }
    public void setMODE(int MODE) {
        this.MODE = MODE;
    }

    public Model3D(Context c, int resId_obj) {
        this.context = c;
        this.resId_obj = resId_obj;
    }

    public void addTexture(int resId){
        resId_textures.add(resId);
    }
    public void addAnims(int resId){
        resId_anims.add(resId);
    }
    public void setAnimFPS(int animFPS) {
        AnimFPS = animFPS;
    }
    public void transitionAnimation(int index) {
        if (index+1>AnimsList.size())
            return;
        ObjectMash.transitionToAnimationSequence(AnimsList.get(index), 1000);
    }

    public boolean isVisiable() {
        return visiable;
    }
    public void setVisiable(boolean visiable) {
        this.visiable = visiable;

        try {
            switch (MODE){
                case Model3D.LOAD_MTL_OBJ:
                    Object3D.setVisible(visiable);
                    break;
                case Model3D.LOAD_MD5_MASH:
                    ObjectMash.setVisible(visiable);
                    break;
            }

            if (canCollision)
                mCollCube.setVisible(visiable);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void parse(RajawaliRenderer renderer){

        try{
            /** 設定模型 */
            switch (MODE){
                case Model3D.LOAD_MTL_OBJ:
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
                    break;

                case LOAD_MD5_MASH:
                    LoaderMD5Mesh meshParser = new LoaderMD5Mesh(renderer, resId_obj);
                    meshParser.parse();

                    ObjectMash = (SkeletalAnimationObject3D) meshParser.getParsedAnimationObject();

                    for (int i=0; i<resId_anims.size(); ++i){
                        LoaderMD5Anim animParser = new LoaderMD5Anim("anim" + String.valueOf(i), renderer, resId_anims.get(i));
                        animParser.parse();
                        AnimsList.add((SkeletalAnimationSequence) animParser.getParsedAnimationSequence());
                    }

                    if (AnimsList.size()>0){
                        ObjectMash.setAnimationSequence(AnimsList.get(0));
                        ObjectMash.setFps(AnimFPS);
                        ObjectMash.play();
                    }
                    break;
            }

            if (canCollision)
                setCollisionBox();

        } catch (Exception e){
            e.printStackTrace();
        } catch (OutOfMemoryError outOfMemoryError){
            outOfMemoryError.printStackTrace();
        }
    }

    public void render(Camera camera){
        try {
            switch (MODE){
                case Model3D.LOAD_MTL_OBJ:
                    Object3D.render(camera, vpMatrix, projMatrix, vMatrix, null);
                    break;
                case Model3D.LOAD_MD5_MASH:
                    ObjectMash.render(camera, vpMatrix, projMatrix, vMatrix, null);
                    break;
            }

            if (canCollision){
                mCollCube.render(camera, vpMatrix, projMatrix, vMatrix, null);
                mCollBounding = mCollCube.getGeometry().getBoundingBox();
                mCollBounding.transform(mCollCube.getModelViewMatrix());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setCollisionBox(){
        Material simple = new Material();
        mCollCube = new Cube(1);
        mCollCube.setMaterial(simple);
        mCollCube.setColor(CollBoxColor);
        mCollCube.setPosition(coll_pos_x, coll_pos_y, coll_pos_z);
        mCollCube.setScaleX(coll_scale_x);
        mCollCube.setScaleY(coll_scale_y);
        mCollCube.setScaleZ(coll_scale_z);
        mCollCube.setDepthMaskEnabled(false);
        mCollCube.setBlendingEnabled(true);
        mCollCube.setBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_DST_ALPHA);
        mCollCube.setShowBoundingVolume(isShowBounding);
    }

    public void setVpMatrix(float[] vpMatrix) {
        this.vpMatrix = new Matrix4().setAll(vpMatrix);
    }
    public void setProjMatrix(float[] projMatrix) {
        this.projMatrix = new Matrix4().setAll(projMatrix);
    }
    public void setvMatrix(float[] vMatrix) {
        this.vMatrix = new Matrix4().setAll(vMatrix);
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

    public boolean getCanCollision() {
        return this.canCollision;
    }
    public void setCanCollision(boolean canCollision) {
        this.canCollision = canCollision;

    }
    public void setShowBounding(boolean isShowBounding) {
        this.isShowBounding = isShowBounding;
        if (isShowBounding)
            CollBoxColor =  0xff000000;
        else
            CollBoxColor =  0x00000000;
    }
    public IBoundingVolume getCollBounding() {
        return mCollBounding;
    }
    public boolean isCollision(IBoundingVolume other){
        return mCollBounding.intersectsWith(other);
    }

    public void setColl_scale_x(float coll_scale_x) {
        this.coll_scale_x = coll_scale_x;
    }
    public void setColl_scale_y(float coll_scale_y) {
        this.coll_scale_y = coll_scale_y;
    }
    public void setColl_scale_z(float coll_scale_z) {
        this.coll_scale_z = coll_scale_z;
    }
    public void setColl_pos_x(float coll_pos_x) {
        this.coll_pos_x = coll_pos_x;
    }
    public void setColl_pos_y(float coll_pos_y) {
        this.coll_pos_y = coll_pos_y;
    }
    public void setColl_pos_z(float coll_pos_z) {
        this.coll_pos_z = coll_pos_z;
    }
}
