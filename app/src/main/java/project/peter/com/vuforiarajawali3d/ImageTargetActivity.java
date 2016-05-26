package project.peter.com.vuforiarajawali3d;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import project.peter.com.vuforiarajawali3d.Unit.BaseVuforiaActivity;
import project.peter.com.vuforiarajawali3d.Unit.CollisionCallback;
import project.peter.com.vuforiarajawali3d.Unit.Model3D;

public class ImageTargetActivity extends BaseVuforiaActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        initAR();
        super.onCreate(savedInstanceState);
        setShowModels();
        addCustomView();

        this.setCollisionCallback(new CollisionCallback() {
            @Override
            public void ObjectCollision(int objId1, int objId2) {
                Log.d("ImageTargetActivity", "Object " + objId1 + " is Collision by Object " + String.valueOf(objId2));
                if (objId1==2||objId2==2){
                    Model3D temp = ImageTargetActivity.this.getModel3DArrayList().get(2);
                    temp.transitionAnimation(0,1,1000,2000);
                }
            }
        });

        Button bt_about = (Button)findViewById(R.id.bt_about);
        bt_about.setText("ABOUT");
        bt_about.setOnClickListener(this);
    }

    private void initAR(){
        // set mode
        this.setARMode(BaseVuforiaActivity.MODE_ImageTarget);

        // set local target library
        ArrayList<String> dataString = new ArrayList<>();
        dataString.add("Watch.xml");
        this.setDatasetStrings(dataString);

        // set max targets will show in same time
        this.setMAX_TARGETS_COUNT(4);
    }

    private void setShowModels(){
        // set show models
        ArrayList<Model3D> arrayList = new ArrayList<>();

        Model3D tempM3D;

        // target 1 , id : 0
        // MTL OBJ
        tempM3D = new Model3D(this, R.raw.roadcar_obj);
        tempM3D.setObj_scale(0.2f);
        tempM3D.setObj_translate_x(-20.0f);
        tempM3D.setObj_translate_y(-20.0f);
        tempM3D.setObj_rotate_angle(90.0f);

        tempM3D.setCanCollision(true);
        tempM3D.setShowBounding(true);
        tempM3D.setColl_pos_x(200.0f);
        tempM3D.setColl_pos_y(20.0f);
        tempM3D.setColl_pos_z(-125.0f);
        tempM3D.setColl_scale_x(800.0f);
        tempM3D.setColl_scale_y(400.0f);
        tempM3D.setColl_scale_z(500.0f);

        arrayList.add(tempM3D);

        // target 2 , id : 1
        tempM3D = new Model3D(this, R.raw.watch_obj);
        tempM3D.setObj_scale(10.0f);
        tempM3D.setObj_translate_x(0.0f);
        tempM3D.setObj_translate_y(0.0f);
        tempM3D.setObj_rotate_angle(90.0f);

//        tempM3D.setCanCollision(true);
//        tempM3D.setShowBounding(false);
//        tempM3D.setColl_pos_y(4.0f);
//        tempM3D.setColl_scale_x(4.0f);
//        tempM3D.setColl_scale_y(5.0f);
//        tempM3D.setColl_scale_z(5.0f);

        arrayList.add(tempM3D);

        // target 3 , id : 2
        tempM3D = new Model3D(this, R.raw.ingrid_mesh);
        tempM3D.setMODE(Model3D.LOAD_MD5_MASH);
        tempM3D.addAnims(R.raw.ingrid_idle);
        tempM3D.addAnims(R.raw.ingrid_arm_stretch);
        tempM3D.addAnims(R.raw.ingrid_bend);
        tempM3D.addAnims(R.raw.ingrid_walk);
        tempM3D.setObj_scale(30.0f);
        tempM3D.setObj_rotate_angle(90.0f);

        tempM3D.setCanCollision(true);
        tempM3D.setShowBounding(true);
        tempM3D.setColl_pos_y(0.1f);
        tempM3D.setColl_scale_x(1.5f);
        tempM3D.setColl_scale_y(6.0f);

        arrayList.add(tempM3D);

        // target 4 , id : 3
        tempM3D = new Model3D(this, R.raw.music_viedo);
        tempM3D.setMODE(Model3D.LOAD_VEDIO_PLANE);
        tempM3D.setObj_scale(10.0f);
        tempM3D.setObj_translate_x(0.0f);
        tempM3D.setObj_translate_y(0.0f);
        tempM3D.setObj_rotate_angle(180.0f);
        tempM3D.setObj_rotate_y(90.0f);

        arrayList.add(tempM3D);

        this.setModel3DArrayList(arrayList);
    }

    private void addCustomView(){
        // add custom view
        View root = findViewById(android.R.id.content);
        View.inflate(this, R.layout.sub_custom, (ViewGroup) root);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this, "This is ImageTarget Sample.", Toast.LENGTH_SHORT).show();
    }
}
