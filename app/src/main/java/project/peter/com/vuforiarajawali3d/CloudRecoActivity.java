package project.peter.com.vuforiarajawali3d;

import android.os.Bundle;

import project.peter.com.vuforiarajawali3d.Unit.BaseVuforiaActivity;

public class CloudRecoActivity extends BaseVuforiaActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // set mode
        this.setARMode(BaseVuforiaActivity.MODE_CloudReco);

        // set cloud target library key
        String AccessKey = "77c344b90a5ef0de73abe9c5c314331537a67a76";
        String SecretKey = "72a790f86362c917c9a5ba5a4abe5d6fa42adc9a";
        this.setCloudTargetsKey(AccessKey, SecretKey);

        // set show models


        super.onCreate(savedInstanceState);
    }
}
