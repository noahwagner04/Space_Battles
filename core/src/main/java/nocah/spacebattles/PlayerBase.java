package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;

public class PlayerBase extends Sprite implements Damageable {
    SpaceBattles game;
    private Sound sound;
    private long soundID = -1;
    private Sound destroy;

    private float maxHealth = 4000;
    private float health = maxHealth;

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

    private TextureRegion pentagon;
    public float pentagonRotation = 0;

    private ParticleEffect explosionEffect;
    private ParticleEffect smokeEffect;
    private ParticleEffect sparksEffect;

    private boolean smokeStarted = false;
    private boolean sparksStarted = false;

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

        pentagon = game.getEntity(SpaceBattles.RSC_PENTAGON_IMG);

        sound = game.am.get(SpaceBattles.RSC_BASE_AMBIENT_SOUND, Sound.class);
        destroy = game.am.get(SpaceBattles.RSC_BASE_DESTROY_SOUND, Sound.class);

        explosionEffect = new ParticleEffect();
        explosionEffect.load(
            Gdx.files.internal("particles/explosion.p"),
            game.am.get(SpaceBattles.RSC_PARTICLE_ATLAS, TextureAtlas.class)
        );
        explosionEffect.scaleEffect(0.03f);

        smokeEffect = new ParticleEffect();
        smokeEffect.load(
            Gdx.files.internal("particles/smoke.p"),
            game.am.get(SpaceBattles.RSC_PARTICLE_ATLAS, TextureAtlas.class)
        );
        smokeEffect.scaleEffect(0.03f);

        sparksEffect = new ParticleEffect();
        sparksEffect.load(
            Gdx.files.internal("particles/sparks.p"),
            game.am.get(SpaceBattles.RSC_PARTICLE_ATLAS, TextureAtlas.class)
        );
        sparksEffect.scaleEffect(0.015f);
    }

    public void update(float delta) {
        if (destroyed) return;
        respawnTimer += delta;
        if (respawnQueued && respawnTimer > respawnInterval) {
            respawnQueued = false;
            respawnTimer = 0;
            game.players[team].respawn();
        }

        playSound();

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
        explosionEffect.draw(batch);
        if (destroyed) return;
        super.draw(batch);
        Color c = batch.getColor().cpy();
        batch.setColor(getColor());
        float size = getWidth() - 0.25f;
        Vector2 center = getCenter();
        batch.draw(
            pentagon,
            center.x - size / 2f,
            center.y - size / 2f,
            size / 2f,
            size / 2f,
            size,
            size,
            1,
            1,
            pentagonRotation
        );
        pentagonRotation += 90 * Gdx.graphics.getDeltaTime();
        batch.setColor(c);
        healthBar.draw(batch);
        smokeEffect.draw(batch);
        sparksEffect.draw(batch);
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

        if (health < 1500 && !smokeStarted) {
            Vector2 c = getCenter();
            smokeEffect.setPosition(c.x, c.y);
            smokeStarted = true;
        }

        if (health < 750 && !sparksStarted) {
            Vector2 c = getCenter();
            sparksEffect.setPosition(c.x, c.y);
            sparksStarted = true;
        }

        if (health <= 0) {
            destroyed = true;
            long destroyID = destroy.play();
            destroy.setVolume(destroyID, game.getVolume(getCenter(), 2f));
            smokeEffect.allowCompletion();
            sparksEffect.allowCompletion();
            Vector2 c = getCenter();
            explosionEffect.setPosition(c.x, c.y);
            explosionEffect.start();
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
        maxHealth += 400f;
        heal(800);
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

    private void playSound() {
        if (soundID == -1) {
            soundID = sound.loop();
            sound.setVolume(soundID, game.getVolume(getCenter(), 0.08f));
        } else {
            sound.setVolume(soundID, game.getVolume(getCenter(), 0.08f));
        }
    }

    public void updateParticleEffects(float delta) {
        if (smokeStarted) {
            smokeEffect.update(delta);
        }

        if (sparksStarted) {
            sparksEffect.update(delta);
        }
        explosionEffect.update(delta);
    }
}
