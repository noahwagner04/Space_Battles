package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import nocah.spacebattles.netevents.MoveEvent;

public class Player extends Sprite {
    private SpaceBattles game;

    public Vector2 velocity = new Vector2(0, 0);
    private float maxSpeed = 6;
    private float acceleration = 12;
    private float friction = 4;

    public float rotVelocity = 0;
    private float maxRotSpeed = 270;
    private float rotAcceleration = 360 * 4;
    private float rotFriction = 360 * 2;

    private float size = 1;
    private ParticleEffect effect;

    private float bulletDamage = 1;
    private float bulletSpeed = 1;
    private float bulletCoolDown = 1f;
    private float shootTimer = 0;
    private float shootKnockBack = 3;

    private float asteroidRepulsion = 3;

    public byte thrustAnimationState = 2;


    public Player(SpaceBattles game) {
        super(game.getEntity(SpaceBattles.RSC_TRIANGLE_IMG));
        this.game = game;
        setSize(size, size);
        setOriginCenter();
        setupParticleEffect(game);
    }

    public void update(float delta) {
        handleRotation(delta);
        handleThrust(delta);
        updatePosition(delta);
        updateParticleEffect(delta);

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && shootTimer > bulletCoolDown) {
            shootTimer = 0;
            fireBullet();
        }
        shootTimer += delta;
    }

    public void updateRemotePlayer(float delta) {
        float x = getX();
        float y = getY();
        float r = getRotation();

        x += velocity.x * delta;
        y += velocity.y * delta;
        r += rotVelocity * delta;

        setX(x);
        setY(y);
        setRotation(r);

        if (thrustAnimationState == 0 && effect.isComplete()) {
            effect.start();
        } else if (thrustAnimationState == 1) {
            effect.start();
        } else if (thrustAnimationState == 2) {
            effect.allowCompletion();
        }

        updateParticleEffect(delta);
    }

    @Override
    public void draw(Batch batch) {
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

    public void collide(TiledMap tiledMap) {
        float playerX = getX() + getOriginX();
        float playerY = getY() + getOriginY();
        float radius = getInCircleRadius();
        float tileSize = 1;

        int leftTile = (int) ((playerX - radius) / tileSize);
        int rightTile = (int) ((playerX + radius) / tileSize);
        int topTile = (int) ((playerY - radius) / tileSize);
        int bottomTile = (int) ((playerY + radius) / tileSize);

        TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get(0);
        if (layer == null) return;

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

    public void collide(Circle c) {
        float dst = getCenter().dst(c.x, c.y);
        if (c.radius + getInCircleRadius() < dst) return;
        Vector2 btw = getCenter().sub(c.x, c.y);
        setCenter(c.x + btw.x, c.y + btw.y);
        velocity = getCenter().sub(c.x, c.y).setLength(asteroidRepulsion);
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
            thrustAnimationState = 0;
            velocity.add(direction.scl(acceleration * delta));
            if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                thrustAnimationState = 1;
                effect.start();
            }
        } else {
            thrustAnimationState = 2;
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
        Vector2 origin = getCenter();
        Vector2 offset = getHeadingDir().scl(-size/4);
        effect.setPosition(origin.x + offset.x, origin.y + offset.y);

        effect.getEmitters().forEach(emitter -> {
            emitter.getAngle().setHigh(getRotation() - 90);
            emitter.getAngle().setLow(getRotation() - 90);
        });
    }

    private void handleTileCollision(int tileX, int tileY, float tileSize, float playerX, float playerY, float radius) {
        float tileCenterX = tileX * tileSize + tileSize / 2;
        float tileCenterY = tileY * tileSize + tileSize / 2;

        float closestX = MathUtils.clamp(playerX, tileCenterX - tileSize / 2, tileCenterX + tileSize / 2);
        float closestY = MathUtils.clamp(playerY, tileCenterY - tileSize / 2, tileCenterY + tileSize / 2);

        float dx = playerX - closestX;
        float dy = playerY - closestY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance < radius) {
            // add small delta to over-correct
            float overlap = radius - distance + 1e-5f;
            float nx = dx / distance;
            float ny = dy / distance;

            translate(nx * overlap, ny * overlap);
            if (Math.abs(nx) > Math.abs(ny)) velocity.x = 0;
            else velocity.y = 0;
        }
    }

    private void fireBullet() {
        TextureRegion tex = game.getEntity(SpaceBattles.RSC_SQUARE_IMG);
        Vector2 heading = getHeadingDir();
        Vector2 startPos = getCenter().add(heading.cpy().scl(size/2));
        Projectile proj = new Projectile(tex, startPos.x, startPos.y, 10, getRotation() + 90);
        proj.setSize(0.15f, 0.15f);
        proj.setOriginCenter();
        proj.translate(-proj.getOriginX(), -proj.getOriginY());
        game.projectiles.add(proj);

        velocity.sub(heading.scl(shootKnockBack));
    }

    public Vector2 getCenter() {
        return new Vector2(getX() + getOriginX(), getY() + getOriginY());
    }

    private Vector2 getHeadingDir() {
        return new Vector2(0, 1).rotateDeg(getRotation());
    }

    private float getInCircleRadius() {
        return size * 0.288675f; // Radius for an equilateral triangle
    }

    public void sendPlayerMoveEvent() {
        game.client.sendEvent(new MoveEvent(game.id,
            getX(),
            getY(),
            getRotation(),
            velocity.x,
            velocity.y,
            rotVelocity,
            thrustAnimationState
        ));
    }

    public Circle getCirle() {
        return new Circle(getX() + getOriginX(), getY() + getOriginY(), getInCircleRadius());
    }
}
