package nocah.spacebattles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;

public class Asteroid extends Sprite implements Damageable {
    private Vector2 velocity;
    private float rotationSpeed;
    private float size;
    private float health;

    public Asteroid(TextureRegion texture, Rectangle worldBounds) {
        super(texture);
        randomizeAttributes();
        randomizePosition(worldBounds);
    }

    public void randomizeAttributes() {
        size = MathUtils.random(1.5f, 4);
        health = size * size * 20;
        setSize(size, size);
        setOriginCenter();

        Color tint = new Color(0.7f, 0.6f, 0.5f, 1);
        float brightness = MathUtils.random(0.5f, 1);
        setColor(
            tint.r * brightness,
            tint.g * brightness,
            tint.b * brightness,
            1
        );

        velocity = new Vector2(MathUtils.random(), 0).rotateDeg(MathUtils.random(0, 360));
        this.rotationSpeed = MathUtils.random() * 90;
    }

    public void randomizePosition(Rectangle worldBounds) {
        float x = MathUtils.random(worldBounds.x, worldBounds.width);
        float y = MathUtils.random(worldBounds.y, worldBounds.height);
        setPosition(x, y);
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

    @Override
    public float getHealth() {
        return health;
    }

    @Override
    public boolean damage(float amount) {
        health -= Math.max(amount, 0);
        if (health <= 0) {
            randomizeAttributes();
            return true;
        }
        return false;
    }

    @Override
    public void heal(float amount) { }

    @Override
    public Shape2D getDamageArea() {
        return new Circle(getX() + getOriginX(), getY() + getOriginY(), size / 3f);
    }
}
