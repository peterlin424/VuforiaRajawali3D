package project.peter.com.vuforiarajawali3d.Unit;

import org.rajawali3d.Camera;
import org.rajawali3d.math.Matrix4;
import org.rajawali3d.renderer.RajawaliRenderer;

/**
 * Created by linweijie on 6/20/16.
 */
public interface ObjectsCallback {
    void parse(RajawaliRenderer renderer);
    void render(Camera camera, Matrix4 vpMatrix, Matrix4 projMatrix, Matrix4 vMatrix);
}
