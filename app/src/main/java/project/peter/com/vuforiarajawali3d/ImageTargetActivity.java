package project.peter.com.vuforiarajawali3d;

import android.os.Bundle;

import java.util.ArrayList;

import project.peter.com.vuforiarajawali3d.Unit.BaseVuforiaActivity;

public class ImageTargetActivity extends BaseVuforiaActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set mode
        this.setARMode(BaseVuforiaActivity.MODE_ImageTarget);

        // set local target library
        ArrayList<String> dataString = new ArrayList<>();
        dataString.add("Watch.xml");
        this.setDatasetStrings(dataString);

        // set show models


        super.onCreate(savedInstanceState);
    }
}
