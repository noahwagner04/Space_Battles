package nocah.spacebattles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.*;
import nocah.spacebattles.netevents.MoveEvent;

public class Minion extends Sprite implements Damageable {
    SpaceBattles game;
    Player playerLeader;
    private final int team;
    private boolean dead;

    public Vector2 velocity = new Vector2();
    private float maxSpeed = 5f;
    private float acceleration = 15f;
    private float friction = 10f;
    private float repulse = 20f;
    private float repulseDist = 0.5f;
    private float rotationSpeed = 10f;
    private float collisionBounce = 5f;

    private float minFollowDist = 2f;
    private float maxFollowDist = 10f;

    private float shootRange = 8f;
    private float bulletDamage = 8f;
    private float bulletSpeed = 3f;
    private float shootKnockBack = 0.75f;
    private float shootInterval = 2f;
    private float shootTimer = 0;

    private float maxHealth = 20f;
    private float health = maxHealth;

    private float size = 0.6f;

    public Minion(SpaceBattles game, int team) {
        super(game.getEntity(SpaceBattles.RSC_TRIANGLE_IMG));
        setSize(size, size);
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

        if (shootTimer < shootInterval) {
            shootTimer += delta;
            return;
        }

        shootTimer = 0;

        Vector2 target = null;
        float shortestDst = Float.MAX_VALUE;

        for (Player p : game.players) {
            if (p == null || p == game.players[team] || p.isSpectating()) continue;
            float dst = p.getCenter().dst(getCenter());
            if (dst < shootRange && dst < shortestDst) {
                target = p.getCenter();
                shortestDst = dst;
            }
        }

        if (target != null) {
            shootAt(target);
            return;
        }

        shortestDst = Float.MAX_VALUE;
        for (PlayerBase b : game.bases) {
            if (b == null || b == game.bases[team] || b.isDestroyed()) continue;
            float dst = b.getCenter().dst(getCenter());
            if (dst < shootRange && dst < shortestDst) {
                target = b.getCenter();
                shortestDst = dst;
            }
        }

        if (target != null) {
            shootAt(target);
            return;
        }

        shortestDst = Float.MAX_VALUE;
        for (Minion[] ms : game.minions) {
            if (ms == null || ms == game.minions[team]) continue;
            for (Minion m : ms) {
                if (m == null || m == this || m.isDead()) continue;
                float dst = m.getCenter().dst(getCenter());
                if (dst < shootRange && dst < shortestDst) {
                    target = m.getCenter();
                    shortestDst = dst;
                }
            }
        }

        if (target != null) {
            shootAt(target);
            return;
        }

        shortestDst = Float.MAX_VALUE;
        for (Asteroid a : game.asteroids) {
            float dst = a.getCenter().dst(getCenter());
            if (dst < shootRange && dst < shortestDst) {
                target = a.getCenter();
                shortestDst = dst;
            }
        }

        if (target != null) {
            shootAt(target);
            return;
        }
    }

    private void shootAt(Vector2 target) {
        TextureRegion tex = game.getEntity(SpaceBattles.RSC_SQUARE_IMG);
        Vector2 heading = target.sub(getCenter());
        Vector2 startPos = getCenter().add(heading.cpy().setLength(size/2));
        Projectile proj = new Projectile(-1, tex, startPos.x, startPos.y, bulletSpeed, heading.angleDeg());
        proj.setSize(0.15f, 0.15f);
        proj.setOriginCenter();
        proj.translate(-proj.getOriginX(), -proj.getOriginY());
        proj.damageAmount = bulletDamage;
        proj.team = team;
        game.projectiles.add(proj);

        velocity.sub(heading.scl(shootKnockBack));
    }

    public void updateRemoteMinion(float delta) {

        float x = getX();
        float y = getY();
        float r = getRotation();

        x += velocity.x * delta;
        y += velocity.y * delta;

        setX(x);
        setY(y);
        setRotation(r);
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
            Vector2 knockBack = center.sub(tileCenterX, tileCenterY).setLength(collisionBounce);
            velocity.add(knockBack);
        }
    }

    public void collide(Circle c) {
        Vector2 center = getCenter();
        if (!c.contains(center)) return;
        Vector2 knockback = center.sub(c.x, c.y).setLength(collisionBounce);
        velocity.add(knockback);
    }

    public boolean checkBounds(Rectangle worldBounds) {
        Vector2 center = getCenter();

        return center.x < worldBounds.x ||
            center.x > worldBounds.x + worldBounds.width ||
            center.y < worldBounds.y ||
            center.y > worldBounds.y + worldBounds.height;
    }

    public Vector2 getCenter() {
        return new Vector2(getX() + getOriginX(), getY() + getOriginY());
    }

    public boolean isDead() {
        return dead;
    }

    public void destroy() {
        damage(health + 1);
    }

    @Override
    public void draw(Batch batch) {
        if (dead) return;
        super.draw(batch);
    }

    @Override
    public float getHealth() {
        return health;
    }

    @Override
    public boolean damage(float amount) {
        health -= Math.max(amount, 0);
        if (health <= 0) {
            game.bases[team].minionCount--;
            dead = true;
            return true;
        }
        return false;
    }

    @Override
    public void heal(float amount) {
        health += Math.max(amount, 0);
    }

    @Override
    public Shape2D getDamageArea() {
        Vector2 center = getCenter();
        return new Circle(center.x, center.y, size);
    }

    public void revive() {
        health = maxHealth;
        dead = false;
    }

    public void sendMinionMoveEvent(byte teamID, byte minionID) {
        game.sendEvent(new MoveEvent(teamID,
            minionID,
            getX(),
            getY(),
            getRotation(),
            velocity.x,
            velocity.y,
            10f,
            (byte)0
        ));
    }
}
