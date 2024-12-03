package nocah.spacebattles;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;

public class Minion extends Sprite implements Damageable {
    SpaceBattles game;
    Player playerLeader;
    private final int team;
    private boolean dead;

    private Vector2 velocity = new Vector2();
    private float maxSpeed = 5f;
    private float acceleration = 15f;
    private float friction = 10f;
    private float repulse = 20f;
    private float repulseDist = 0.5f;
    private float rotationSpeed = 10f;

    private float minFollowDist = 2f;
    private float maxFollowDist = 10f;

    public Minion(SpaceBattles game, int team) {
        super(game.getEntity(SpaceBattles.RSC_TRIANGLE_IMG));
        setSize(0.6f, 0.6f);
        setOriginCenter();
        this.game = game;
        this.team = team;
        playerLeader = game.players[team];
    }

    public void update(float delta) {
        if (dead) return;

        // if dst to player is in range x to y, follow (donut follow range)
        Vector2 toPlayer = playerLeader.getCenter().sub(getCenter());
        boolean applyFriction = false;

        if (toPlayer.len() > minFollowDist &&
            toPlayer.len() < maxFollowDist &&
            !playerLeader.isSpectating()) {
            velocity.add(toPlayer.setLength(acceleration).scl(delta));
        } else {
            applyFriction = true;
        }

        for (Minion m : game.minions[team]) {
            if (m == null || m == this) continue;
            Vector2 toM = m.getCenter().sub(getCenter());
            if (toM.len() > repulseDist) continue;
            velocity.sub(toM.setLength(repulse).scl(delta));
            applyFriction = false;
        }

        velocity.clamp(0, maxSpeed);

        if (applyFriction) {
            if (velocity.len() < friction * delta) velocity.setLength(0);
            else velocity.sub(velocity.cpy().setLength(friction * delta));
        }

        translateX(velocity.x * delta);
        translateY(velocity.y * delta);

        if (velocity.len2() > 0) {
            float rot = getRotation();
            float targetRot = velocity.angleDeg() - 90;
            float newRot = MathUtils.lerpAngleDeg(rot, targetRot, delta * rotationSpeed);
            setRotation(newRot);
        }

        // do the following in order (prioritize shooting players, ..., lastly asteroid)
        // shoot nearest player enemy
        // shoot nearest player base
        // shoot nearest enemy minions
        // shoot nearest asteroid (within range z)

        // possible boid like herding behavior with other minions
    }

    public void collide(TiledMap map) {
        // Get the position of the minion's center
        Vector2 center = getCenter();

        int tileX = (int) Math.floor(center.x);
        int tileY = (int) Math.floor(center.y);

        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);
        if (layer == null) return;

        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
        if (cell != null) {
            float tileCenterX = tileX + 0.5f;
            float tileCenterY = tileY + 0.5f;
            Vector2 knockback = center.sub(tileCenterX, tileCenterY).setLength(repulse);
            velocity.add(knockback);
        }
    }

    public void collide(Circle c) {
        Vector2 center = getCenter();
        if (!c.contains(center)) return;
        Vector2 knockback = center.sub(c.x, c.y).setLength(repulse);
        velocity.add(knockback);
    }

    public Vector2 getCenter() {
        return new Vector2(getX() + getOriginX(), getY() + getOriginY());
    }

    public boolean isDead() {
        return dead;
    }

    @Override
    public float getHealth() {
        return 0;
    }

    @Override
    public boolean damage(float amount) {
        return false;
    }

    @Override
    public void heal(float amount) {

    }

    @Override
    public Shape2D getDamageArea() {
        return null;
    }
}
