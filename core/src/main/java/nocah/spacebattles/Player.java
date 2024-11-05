package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class Player extends Sprite {
    private Vector2 velocity = new Vector2(0, 0);
    private float maxSpeed = 200;
    private float acceleration = 500;
    private float friction = 150;

    private float rotVelocity = 0;
    private float maxRotSpeed = 270;
    private float rotAcceleration = 360 * 4;
    private float rotFriction = 360 * 2;

    public Player(SpaceBattles game) {
        super(game.am.get(SpaceBattles.RSC_TRIANGLE_IMG, Texture.class));
        setCenter(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        setOriginCenter();
        setScale(0.1f);
    }

    public void update(float delta) {
        boolean applyRotFriction = true;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            rotVelocity += rotAcceleration * delta;
            applyRotFriction = false;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            rotVelocity -= rotAcceleration * delta;
            applyRotFriction = false;
        }

        if (Math.abs(rotVelocity) > maxRotSpeed) {
            rotVelocity = maxRotSpeed * Math.signum(rotVelocity);
        }

        if (applyRotFriction) {
            if (Math.abs(rotVelocity) < rotFriction * delta) {
                rotVelocity = 0;
            } else {
                rotVelocity = rotVelocity - Math.signum(rotVelocity) * rotFriction * delta;
            }
        }

        rotate(rotVelocity * delta);

        Vector2 direction = new Vector2(0, 1).rotateDeg(getRotation());

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            velocity.add(direction.scl(acceleration * delta));
        } else if (velocity.len() > friction * delta) {
            velocity.sub(velocity.cpy().nor().scl(friction * delta));
        } else {
            velocity.setLength(0);
        }
        velocity.clamp(0, maxSpeed);

        setX(getX() + velocity.x * delta);
        setY(getY() + velocity.y * delta);
    }
}
