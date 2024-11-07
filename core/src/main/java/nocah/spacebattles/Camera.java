package nocah.spacebattles;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Camera {
    private OrthographicCamera orthCamera;
    public float followSpeed = 3;

    public Camera(float viewportWidth, float viewportHeight) {
        orthCamera = new OrthographicCamera(viewportWidth, viewportHeight);
        orthCamera.position.set(viewportWidth / 2f, viewportHeight / 2f, 0);
    }

    public void follow(Vector2 target, float delta) {
        Vector3 playerPos = new Vector3(target, 0);
        Vector3 cameraPos = orthCamera.position.cpy().lerp(playerPos, Math.max(Math.min(followSpeed * delta, 1), 0));
        orthCamera.position.set(cameraPos);
        orthCamera.update();
    }

    public Matrix4 getProjMat() {
        return orthCamera.combined.cpy();
    }
}
