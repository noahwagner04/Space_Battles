package nocah.spacebattles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;

public class PlayerBase extends Sprite implements Damageable {
    SpaceBattles game;

    private float maxHealth = 1000;
    private float health = maxHealth;
    private float healRate = 2;

    public Vector2 spawnPoint;

    private boolean isDestroyed = false;

    private boolean respawnQueued = false;
    private float respawnTimer = 0;
    private float respawnInterval = 5;

    private int team;

    public PlayerBase(SpaceBattles game, int team, float x, float y) {
        super(game.getEntity(SpaceBattles.RSC_CIRCLE_IMG));
        this.game = game;
        this.team = team;
        setSize(2, 2);
        setCenter(x, y);
        setOriginCenter();
        spawnPoint = new Vector2(x, y);
    }

    public void update(float delta) {
        if (isDestroyed) return;
        respawnTimer += delta;
        if (respawnQueued && respawnTimer > respawnInterval) {
            respawnQueued = false;
            respawnTimer = 0;
            game.players[team].respawn();
        }
        heal(healRate * delta);
    }

    @Override
    public void draw(Batch batch) {
        if (isDestroyed) return;
        super.draw(batch);
    }

    public boolean getIsDestroyed() {
        return isDestroyed;
    }

    @Override
    public float getHealth() {
        return health;
    }

    @Override
    public boolean damage(float amount) {
        health -= Math.max(amount, 0);
        if (health <= 0) {
            isDestroyed = true;
            return true;
        }
        return false;
    }

    @Override
    public void heal(float amount) {
        health += Math.max(amount, 0);
        health = Math.min(health, maxHealth);
    }

    @Override
    public Shape2D getDamageArea() {
        return new Circle(getX() + getOriginX(), getY() + getOriginY(), getWidth() / 2);
    }

    public void setRespawnTimer() {
        if (isDestroyed) return;
        respawnQueued = true;
        respawnTimer = 0;
    }
}
