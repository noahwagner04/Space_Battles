package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

public class Asteroid extends Sprite implements Damageable {
    private Vector2 velocity;
    private float rotationSpeed;
    private float size;
    private float health;
    private SpaceBattles game;
    private Rectangle spawnArea;
    public float xp;

    private Sound destroy;

    private StatusBar healthBar;

    private ParticleEffect crumbleEffect;

    public Asteroid(SpaceBattles game, Rectangle spawnArea) {
        super(game.getEntity(SpaceBattles.RSC_ASTEROID_IMGS[SpaceBattles.random.nextInt(2)]));
        this.spawnArea = spawnArea;
        this.game = game;
        healthBar = new StatusBar(game, StatusBar.HP_B, StatusBar.HP_F, getX(), getY() - 0.25f, 0, 0);
        healthBar.noDrawOnFull = true;
        destroy = game.am.get(SpaceBattles.RSC_ASTEROID_DESTROY_SOUND, Sound.class);

        crumbleEffect = new ParticleEffect();
        crumbleEffect.load(
            Gdx.files.internal("particles/asteroidExplosion.p"),
            game.am.get(SpaceBattles.RSC_PARTICLE_ATLAS, TextureAtlas.class)
        );

        randomizeAttributes();
        randomizePosition();
    }

    public void randomizeAttributes() {
        size = SpaceBattles.random.nextFloat(1.5f, 4);
        health = size * size * 20;

        setSize(size, size);
        setOriginCenter();
        xp = size * size * 0.6f;

        Color tint = new Color(0.7f, 0.6f, 0.5f, 1);
        float brightness = SpaceBattles.random.nextFloat(0.5f, 1);

        if (size > 3.9) {
            xp *= 6;
            health *= 3;
            tint.g += 3f;
            brightness = 1;
        }
        healthBar.setRange(0, health);
        healthBar.setValue(health);
        healthBar.setSize(size, 0.1f);

        setColor(
            tint.r * brightness,
            tint.g * brightness,
            tint.b * brightness,
            1
        );

        velocity = new Vector2(SpaceBattles.random.nextFloat(), 0);
        velocity.rotateDeg(SpaceBattles.random.nextFloat(360));
        this.rotationSpeed = SpaceBattles.random.nextFloat() * 90;
    }

    public void randomizePosition() {
        float x = SpaceBattles.random.nextFloat(spawnArea.x, spawnArea.width);
        float y = SpaceBattles.random.nextFloat(spawnArea.y, spawnArea.height);
        setCenter(x, y);
        healthBar.setPosition(getX(), getY() - 0.25f);
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
        healthBar.setPosition(getX(), getY() - 0.25f);
        crumbleEffect.update(delta);
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

    public Vector2 getCenter() {
        return new Vector2(getX() + getOriginX(), getY() + getOriginY());
    }

    private void playCrumbleEffect() {
        Color tint = getColor().cpy();

        crumbleEffect.reset(true);
        crumbleEffect.scaleEffect(size / 100);
        Array<ParticleEmitter> crumbleEmitters = crumbleEffect.getEmitters();
        ParticleEmitter.GradientColorValue tint1 = crumbleEmitters.get(0).getTint();
        ParticleEmitter.GradientColorValue tint2 = crumbleEmitters.get(1).getTint();

        float[] tintArray = {tint.r, tint.g, tint.b};

        tint1.setColors(tintArray);
        tint2.setColors(tintArray);

        Vector2 c = getCenter();
        crumbleEffect.setPosition(c.x, c.y);
        crumbleEffect.start();
    }

    @Override
    public void draw(Batch batch) {
        super.draw(batch);
        healthBar.draw(batch);
        crumbleEffect.draw(batch);
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
            playCrumbleEffect();
        }
        return health <= 0;
    }

    @Override
    public void heal(float amount) { }

    @Override
    public Shape2D getDamageArea() {
        return new Circle(getX() + getOriginX(), getY() + getOriginY(), size / 3f);
    }

    public void playDestroy() {
        long id = destroy.play();
        destroy.setVolume(id, game.getVolume(getCenter(), 1f));
    }
}
