package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
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
    private ParticleEffect effect;

    public Player(SpaceBattles game) {
        super(game.getEntity(SpaceBattles.RSC_TRIANGLE_IMG));
        setSize(size, size);
        setOriginCenter();
        setupParticleEffect(game);
    }

    public void update(float delta) {
        handleRotation(delta);
        handleThrust(delta);
        updatePosition(delta);
        updateParticleEffect(delta);
    }

    public void draw(SpriteBatch batch) {
        super.draw(batch);
        effect.draw(batch);
    }

    public void constrain(Rectangle bounds) {
        float x = getX() + getOriginX();
        float y = getY() + getOriginY();
        float r = getInCircleRadius();

        if (x - r < bounds.x) {
            setX(bounds.x + r - getOriginX());
            velocity.x = 0;
        } else if (x + r > bounds.x + bounds.width) {
            setX(bounds.x + bounds.width - r - getOriginX());
            velocity.x = 0;
        }

        if (y - r < bounds.y) {
            setY(bounds.y + r - getOriginY());
            velocity.y = 0;
        } else if (y + r > bounds.y + bounds.height) {
            setY(bounds.y + bounds.height - r - getOriginY());
            velocity.y = 0;
        }
    }

    public void collide(TiledMap tilemap) {
        float playerX = getX() + getOriginX();
        float playerY = getY() + getOriginY();
        float radius = getInCircleRadius();
        float tileSize = 1;

        int leftTile = (int) ((playerX - radius) / tileSize);
        int rightTile = (int) ((playerX + radius) / tileSize);
        int topTile = (int) ((playerY - radius) / tileSize);
        int bottomTile = (int) ((playerY + radius) / tileSize);

        TiledMapTileLayer layer = (TiledMapTileLayer) tilemap.getLayers().get(0);
        for (int tileY = topTile; tileY <= bottomTile; tileY++) {
            for (int tileX = leftTile; tileX <= rightTile; tileX++) {
                TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
                if (cell == null) continue;
                boolean collides = cell.getTile().getProperties().get("collides", Boolean.class);
                if (!collides) continue;
                handleTileCollision(tileX, tileY, tileSize, playerX, playerY, radius);
            }
        }
    }

    private void setupParticleEffect(SpaceBattles game) {
        effect = new ParticleEffect();
        effect.load(
            Gdx.files.internal("particles/Thruster.p"),
            game.am.get(SpaceBattles.RSC_PARTICLE_ATLAS, TextureAtlas.class)
        );
        effect.scaleEffect(0.03f);
    }

    private void handleRotation(float delta) {
        boolean applyRotFriction = true;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            rotVelocity += rotAcceleration * delta;
            applyRotFriction = false;
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            rotVelocity -= rotAcceleration * delta;
            applyRotFriction = false;
        }

        if (Math.abs(rotVelocity) > maxRotSpeed) {
            rotVelocity = maxRotSpeed * Math.signum(rotVelocity);
        }

        if (applyRotFriction) {
            rotVelocity = Math.abs(rotVelocity) < rotFriction * delta ? 0 :
                rotVelocity - Math.signum(rotVelocity) * rotFriction * delta;
        }

        rotate(rotVelocity * delta);
    }

    private void handleThrust(float delta) {
        Vector2 direction = new Vector2(0, 1).rotateDeg(getRotation());

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            velocity.add(direction.scl(acceleration * delta));
            if (Gdx.input.isKeyJustPressed(Input.Keys.W)) effect.start();
        } else {
            effect.allowCompletion();
            velocity.sub(velocity.cpy().nor().scl(friction * delta));
            if (velocity.len() < friction * delta) velocity.setLength(0);
        }

        velocity.clamp(0, maxSpeed);
    }

    private void updatePosition(float delta) {
        translateX(velocity.x * delta);
        translateY(velocity.y * delta);
    }

    private void updateParticleEffect(float delta) {
        effect.update(delta);
        Vector2 origin = new Vector2(getX() + getOriginX(), getY() + getOriginY());
        Vector2 offset = new Vector2(0, -size / 4).rotateDeg(getRotation());
        effect.setPosition(origin.x + offset.x, origin.y + offset.y);

        effect.getEmitters().forEach(emitter -> {
            emitter.getAngle().setHigh(getRotation() - 90);
            emitter.getAngle().setLow(getRotation() - 90);
        });
    }

    private void handleTileCollision(int tileX, int tileY, float tileSize, float playerX, float playerY, float radius) {
        float tileCenterX = tileX * tileSize + tileSize / 2;
        float tileCenterY = tileY * tileSize + tileSize / 2;

        float closestX = clamp(playerX, tileCenterX - tileSize / 2, tileCenterX + tileSize / 2);
        float closestY = clamp(playerY, tileCenterY - tileSize / 2, tileCenterY + tileSize / 2);

        float dx = playerX - closestX;
        float dy = playerY - closestY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance < radius) {
            float overlap = radius - distance;
            float nx = dx / distance;
            float ny = dy / distance;

            translate(nx * overlap, ny * overlap);
            if (Math.abs(nx) > Math.abs(ny)) velocity.x = 0;
            else velocity.y = 0;
        }
    }

    private float getInCircleRadius() {
        return size * 0.288675f; // Radius for an equilateral triangle
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
