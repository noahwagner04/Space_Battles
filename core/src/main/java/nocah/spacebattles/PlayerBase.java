package nocah.spacebattles;

import com.badlogic.gdx.graphics.Color;
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

    private boolean destroyed = false;

    private boolean respawnQueued = false;
    private float respawnTimer = 0;
    private float respawnInterval = 8;

    private int maxMinionCount = 5;
    public int minionCount = 0;
    private float minionSpawnTimer = 0;
    private float minionSpawnInterval = 5;
    public int minionLevel = 0;

    private StatusBar healthBar;

    private int team;

    public PlayerBase(SpaceBattles game, int team, float x, float y) {
        super(game.getEntity(SpaceBattles.RSC_CIRCLE_IMG));
        this.game = game;
        this.team = team;
        setSize(2, 2);
        setCenter(x, y);
        setOriginCenter();
        spawnPoint = new Vector2(x, y);
        setColor(SpaceBattles.PLAYER_COLORS[team]);

        healthBar = new StatusBar(game, StatusBar.HP_B, StatusBar.HP_F, getX(), getY() - 0.25f, 2, 0.1f);
        healthBar.setRange(0, maxHealth);
        healthBar.setValue(health);
        healthBar.noDrawOnFull = true;
    }

    public void update(float delta) {
        if (destroyed) return;
        respawnTimer += delta;
        if (respawnQueued && respawnTimer > respawnInterval) {
            respawnQueued = false;
            respawnTimer = 0;
            game.players[team].respawn();
        }
        heal(healRate * delta);

        if (game.server == null) return;
        if (minionSpawnTimer > minionSpawnInterval) {
            spawnMinion();
            minionSpawnTimer = 0;
        } else if (minionCount < maxMinionCount) {
            minionSpawnTimer += delta;
        }
    }

    private void spawnMinion() {
        Minion m = game.getNextMinion(team);
        minionCount++;
        Vector2 pos = new Vector2(1.5f, 0).rotateDeg(360f / maxMinionCount * minionCount);
        pos.add(getCenter());
        m.setCenter(pos.x, pos.y);
        m.revive();
        m.setStats(minionLevel);
    }

    public Vector2 getCenter() {
        return new Vector2(getX() + getOriginX(), getY() + getOriginY());
    }

    @Override
    public void draw(Batch batch) {
        if (destroyed) return;
        super.draw(batch);
        healthBar.draw(batch);
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public float getHealth() {
        return health;
    }

    @Override
    public boolean damage(float amount) {
        health -= Math.max(amount, 0);
        healthBar.setValue(health);
        if (health <= 0) {
            destroyed = true;
            return true;
        }
        return false;
    }

    @Override
    public void heal(float amount) {
        health += Math.max(amount, 0);
        health = Math.min(health, maxHealth);
        healthBar.setValue(health);
    }

    @Override
    public Shape2D getDamageArea() {
        return new Circle(getX() + getOriginX(), getY() + getOriginY(), getWidth() / 2);
    }

    public void setRespawnTimer() {
        if (destroyed) return;
        respawnQueued = true;
        respawnTimer = 0;
    }

    public void upgradeDefence() {
        maxHealth += 100f;
        health += 100f;
    }

    public void upgradeMinions() {
        minionLevel++;
        minionSpawnInterval -= minionSpawnTimer * 0.05f;
        if (maxMinionCount < SpaceBattles.MAX_MINIONS && minionLevel % 3 == 0) {
            maxMinionCount++;
        }
        for(Minion m : game.minions[team]) {
            if (m == null) continue;
            m.setStats(minionLevel);
        }
    }
}
