package project.peter.com.vuforiarajawali3d;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import project.peter.com.vuforiarajawali3d.Unit.BaseVuforiaActivity;
import project.peter.com.vuforiarajawali3d.Unit.Model3D;

/**
 * Created by linweijie on 4/21/16.
 */
public class FrameMarkerActivity extends BaseVuforiaActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        initAR();
        super.onCreate(savedInstanceState);
        setShowModels();
        addCustomView();

        Button bt_about = (Button)findViewById(R.id.bt_about);
        bt_about.setText("ABOUT");
        bt_about.setOnClickListener(this);
    }

    private void initAR(){
        // set mode
        this.setARMode(BaseVuforiaActivity.MODE_FrameMarkers);

        // set local target library
        ArrayList<Integer> frameIds = new ArrayList<>();
        frameIds.add(3);
        frameIds.add(4);
        this.setMarkerDataSetArray(frameIds);

        // set max targets will show in same time
        this.setMAX_TARGETS_COUNT(2);
    }
    private void setShowModels(){
        // set show models
        ArrayList<Model3D> arrayList = new ArrayList<>();

        Model3D tempM3D = new Model3D(this, R.raw.watch_obj);
        tempM3D.addTexture(R.drawable.watch001);
        tempM3D.addTexture(R.drawable.watch002);
        tempM3D.setObj_scale(10.0f);
        tempM3D.setObj_translate_x(0.0f);
        tempM3D.setObj_translate_y(0.0f);
        tempM3D.setObj_rotate_angle(90.0f);
        arrayList.add(tempM3D);

        tempM3D = new Model3D(this, R.raw.roadcar_obj);
        tempM3D.addTexture(R.drawable.u1);
        tempM3D.addTexture(R.drawable.u2);
        tempM3D.setObj_scale(0.1f);
        tempM3D.setObj_translate_x(-20.0f);
        tempM3D.setObj_translate_y(-20.0f);
        tempM3D.setObj_rotate_angle(90.0f);
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
        Toast.makeText(this, "This is FrameMarker Sample.", Toast.LENGTH_SHORT).show();
    }
}
