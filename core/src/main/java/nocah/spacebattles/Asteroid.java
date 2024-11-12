package nocah.spacebattles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Asteroid extends Sprite {
    private Vector2 velocity;
    private float rotationSpeed;
    private float size;

    public Asteroid(TextureRegion texture, float size, float x, float y) {
        super(texture);
        this.size = size;
        Color tint = new Color(0.7f, 0.6f, 0.5f, 1);
        float brightness = MathUtils.random(0.5f, 1);
        setColor(
            tint.r * brightness,
            tint.g * brightness,
            tint.b * brightness,
            1
        );
        setSize(size, size);
        setPosition(x, y);
        setOriginCenter();
        velocity = new Vector2(MathUtils.random(), 0).rotateDeg(MathUtils.random(0, 360));
        this.rotationSpeed = MathUtils.random() * 90;
    }

    public void setSpeed(float speed) {
        velocity.setLength(speed);
    }

    public void setRotSpeed(float speed) {
        rotationSpeed = speed;
    }

    public void update(float delta) {
        translate(velocity.x * delta, velocity.y * delta);
        rotate(rotationSpeed * delta);
    }

    public void bounceOffBounds(Rectangle bounds) {
        if (getX() < bounds.x) {
            setX(bounds.x);
            velocity.x = -velocity.x;
        } else if (getX() + getWidth() > bounds.x + bounds.width) {
            setX(bounds.x + bounds.width - getWidth());
            velocity.x = -velocity.x;
        }

        if (getY() < bounds.y) {
            setY(bounds.y);
            velocity.y = -velocity.y;
        } else if (getY() + getHeight() > bounds.y + bounds.height) {
            setY(bounds.y + bounds.height - getHeight());
            velocity.y = -velocity.y;
        }
    }
}
