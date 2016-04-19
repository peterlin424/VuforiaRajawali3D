package project.peter.com.vuforiarajawali3d;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import project.peter.com.vuforiarajawali3d.Unit.BaseVuforiaActivity;
import project.peter.com.vuforiarajawali3d.Unit.Model3D;
import project.peter.com.vuforiarajawali3d.Unit.VuforiaKeys;

public class CloudRecoActivity extends BaseVuforiaActivity implements View.OnClickListener{

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
        this.setARMode(BaseVuforiaActivity.MODE_CloudReco);

        // set cloud target library key
        this.setCloudTargetsKey(VuforiaKeys.AccessKey, VuforiaKeys.SecretKey);

        // set cloudReco target name list
        ArrayList<String> tempCDS = new ArrayList<>();
        tempCDS.add("Cloud");
        tempCDS.add("Cloud2");
        this.setCloudDataSet(tempCDS);
    }

    private void setShowModels(){
        // set show models
        ArrayList<Model3D> arrayList = new ArrayList<>();

        Model3D tempM3D = new Model3D(R.raw.watch_obj);
        tempM3D.addTexture(R.drawable.watch001);
        tempM3D.addTexture(R.drawable.watch002);
        tempM3D.setObj_scale(10.0f);
        tempM3D.setObj_translate_x(0.0f);
        tempM3D.setObj_translate_y(0.0f);
        tempM3D.setObj_rotate_angle(90.0f);
        arrayList.add(tempM3D);

        tempM3D = new Model3D(R.raw.roadcar_obj);
        tempM3D.addTexture(R.drawable.u1);
        tempM3D.addTexture(R.drawable.u2);
        arrayList.add(tempM3D);
        tempM3D.setObj_scale(0.5f);
        tempM3D.setObj_translate_x(-100.0f);
        tempM3D.setObj_translate_y(-80.0f);
        tempM3D.setObj_rotate_angle(90.0f);
        this.setModel3DArrayList(arrayList);
    }

    private void addCustomView(){
        // add custom view
        View root = findViewById(android.R.id.content);
        View.inflate(this, R.layout.sub_custom, (ViewGroup) root);
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(this, "This is CloudReco Sample.", Toast.LENGTH_SHORT).show();
    }
}
