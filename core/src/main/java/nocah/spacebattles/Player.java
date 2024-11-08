package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player extends Sprite {
    private Vector2 velocity = new Vector2(0, 0);
    private float maxSpeed = 6;
    private float acceleration = 12;
    private float friction = 4;

    private float rotVelocity = 0;
    private float maxRotSpeed = 270;
    private float rotAcceleration = 360 * 4;
    private float rotFriction = 360 * 2;

    private float size = 1;

    public Player(SpaceBattles game) {
        super(game.am.get(SpaceBattles.RSC_TRIANGLE_IMG, Texture.class));
        setSize(size,size);
        setOriginCenter();
    }

    public void update(float delta) {
        // rotation controls
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

        // thrust controls
        Vector2 direction = new Vector2(0, 1).rotateDeg(getRotation());

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            velocity.add(direction.scl(acceleration * delta));
        } else if (velocity.len() > friction * delta) {
            velocity.sub(velocity.cpy().nor().scl(friction * delta));
        } else {
            velocity.setLength(0);
        }
        velocity.clamp(0, maxSpeed);

        translateX(velocity.x * delta);
        translateY(velocity.y * delta);
    }

    public void constrain(Rectangle b) {
        float x = getX() + getOriginX();
        float y = getY() + getOriginY();
        float r = getInCircleRadius();

        if (x - r < b.x) {
            setX(b.x + r - getOriginX());
            velocity.x = 0;
        } else if (x + r > b.x + b.width) {
            setX(b.x + b.width - r - getOriginX());
            velocity.x = 0;
        }

        if (y - r < b.y) {
            setY(b.y + r - getOriginY());
            velocity.y = 0;
        } else if (y + r > b.y + b.height) {
            setY(b.y + b.height - r - getOriginY());
            velocity.y = 0;
        }
    }

    public void collide(Tilemap tilemap) {
        float playerX = getX() + getOriginX();
        float playerY = getY() + getOriginY();
        float radius = getInCircleRadius();

        float tileSize = tilemap.getTileWidth();

        int leftTile = (int) ((playerX - radius) / tileSize);
        int rightTile = (int) ((playerX + radius) / tileSize);
        int topTile = (int) ((playerY - radius) / tileSize);
        int bottomTile = (int) ((playerY + radius) / tileSize);

        for (int tileY = topTile; tileY <= bottomTile; tileY++) {
            for (int tileX = leftTile; tileX <= rightTile; tileX++) {
                if (tilemap.getTileType(tileX, tileY) != '#') {
                    continue;
                }
                float tileCenterX = tileX * tileSize + tileSize / 2;
                float tileCenterY = tileY * tileSize + tileSize / 2;

                float closestX = clamp(playerX, tileCenterX - tileSize / 2, tileCenterX + tileSize / 2);
                float closestY = clamp(playerY, tileCenterY - tileSize / 2, tileCenterY + tileSize / 2);

                float dx = playerX - closestX;
                float dy = playerY - closestY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                if (distance >= radius) {
                    continue;
                }

                float overlap = radius - distance;

                float nx = dx / distance;
                float ny = dy / distance;
                translate(nx * overlap, ny * overlap);

                if (Math.abs(nx) > Math.abs(ny)) {
                    velocity.x = 0;
                } else {
                    velocity.y = 0;
                }
            }
        }
    }

    private float getInCircleRadius() {
        return size * 0.288675f;
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
