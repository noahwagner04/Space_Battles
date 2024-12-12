package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.*;
import nocah.spacebattles.netevents.MoveEvent;
import nocah.spacebattles.netevents.ShootEvent;

public class Player extends Sprite implements Damageable {
    public static final byte DEFENCE = 0;
    public static final byte ATTACK = 1;
    public static final byte SPEED = 2;
    public static final byte BASE_DEFENCE = 3;
    public static final byte MINIONS = 4;

    private SpaceBattles game;
    private Sound thruster;
    private long thrusterID = -1;
    private Sound shoot;
    private Sound death;
    private long deathID;
    private Sound damage;
    private long damageID;
    private Sound levelUp;
    public byte id;

    public Vector2 velocity = new Vector2(0, 0);
    public float maxSpeed = 5;
    public float acceleration = 10;
    public float friction = 4;

    public float rotVelocity = 0;
    public float maxRotSpeed = 200;
    public float rotAcceleration = 360 * 4;
    public float rotFriction = 360 * 2;

    private float size = 1;
    public ParticleEffect thrusterEffect;
    private ParticleEffect explosionEffect;
    private ParticleEffect levelUpEffect;

    private float bulletDamage = 30;
    private float bulletSpeed = 10;
    public float bulletCoolDown = 1f;
    private float shootTimer = 0;
    public float shootKnockBack = 2f;
    public boolean fireGun = false;

    public byte thrustAnimationState = 2;

    private float health = 80;
    private float maxHealth = 80;

    public boolean isInvincible = false;
    public boolean isInvisible = false;

    private float experience = 0;
    private int level = 0;
    public int statPoints = 0;

    public Ability ability1;
    public Ability ability2;
    public boolean unlockAbility1 = false;
    public boolean unlockAbility2 = false;

    private StatusBar healthBar;
    private StatusBar ability1Bar;
    private StatusBar ability2Bar;

    private boolean spectating = false;

    public Player(SpaceBattles game, byte id) {
        super(game.getEntity(SpaceBattles.RSC_SHIP_IMG));
        this.game = game;
        this.id = id;
        setSize(size, size);
        setOriginCenter();
        setupParticleEffect(game);
        setCenter(0, 0);
        setColor(SpaceBattles.PLAYER_COLORS[id]);

        healthBar = new StatusBar(game, StatusBar.HP_B, StatusBar.HP_F, getX(), getY() - 0.25f, size, size / 10f);
        healthBar.setRange(0, maxHealth);
        healthBar.setValue(health);
        healthBar.noDrawOnFull = true;

        ability1Bar = new StatusBar(game, Color.GRAY.cpy(), Color.WHITE.cpy(), getX(), getY() - 0.3f, size, 0.1f);
        ability1Bar.setValue(0);

        ability2Bar = new StatusBar(game, Color.GRAY.cpy(), Color.WHITE.cpy(), getX(), getY() - 0.35f, size, 0.1f);
        ability2Bar.setValue(0);

        thruster = game.am.get(SpaceBattles.RSC_SHIP_THRUSTER_SOUND, Sound.class);
        shoot = game.am.get(SpaceBattles.RSC_PLAYER_SHOOT_SOUND, Sound.class);
        death = game.am.get(SpaceBattles.RSC_PLAYER_DEATH_SOUND, Sound.class);
        damage = game.am.get(SpaceBattles.RSC_PLAYER_DAMAGE_SOUND, Sound.class);
        levelUp = game.am.get(SpaceBattles.RSC_LEVEL_UP_SOUND, Sound.class);
    }

    public void gainExperience(float xp) {
        if (level >= 10 || !game.gameStarted) return;
        experience += xp;
        while (experience >= xpThreshold(level)) {
            levelUp();
        }
    }

    private void levelUp() {
        experience -= xpThreshold(level);
        level++;
        statPoints += (int)Math.max(level * 0.5, 1);
        if (id == game.id) {
            levelUp.play();
            levelUpEffect.start();
        }
        if (level >= 10) {
            System.out.println("Max Level 10! (second ability unlock)");
            unlockAbility2 = true;
        } else if (level == 5){
            System.out.println("Level 5! (first ability unlock)");
            unlockAbility1 = true;
        } else {
            System.out.println("Level " + level + "!");
        }
    }

    public void upgradeStat(int s, boolean hudCommand) {
        if (statPoints <= 0 && id == game.id && !hudCommand) {
            statPoints = 0;
            System.out.println("No stat points to spend!");
            return;
        }

        if (!hudCommand) statPoints--;

        switch (s) {
            case DEFENCE:
                health += 15;
                maxHealth += 15;
                healthBar.setRange(0, maxHealth);
                healthBar.setValue(health);
                shootKnockBack -= shootKnockBack * 0.05f;
                break;
            case SPEED:
                maxSpeed += 0.15f;
                acceleration += 1.5f;
                friction += 0.8f;
                maxRotSpeed += 6;
                rotAcceleration += 150;
                rotFriction += 80;
                break;
            case ATTACK:
                bulletDamage += 2;
                bulletSpeed += 0.25f;
                bulletCoolDown -= bulletCoolDown * 0.08f;
                bulletCoolDown = Math.max(bulletCoolDown, 0.45f);
                break;
            case BASE_DEFENCE:
                game.bases[id].upgradeDefence();
                break;
            case MINIONS:
                game.bases[id].upgradeMinions();
                break;
        }
    }

    public float xpThreshold(float level) {
        return (float)(10 * Math.pow(1.25f, level - 1));
    }

    public void update(float delta) {
        if (spectating) {
            updateSpectator(delta);
            updateParticleEffect(delta);
            return;
        }

        handleRotation(delta);
        handleThrust(delta);
        updatePosition(delta);
        updateParticleEffect(delta);
        healthBar.setPosition(getX(), getY() - 0.25f);
        ability1Bar.setPosition(getX(), getY() - 0.38f);
        ability2Bar.setPosition(getX(), getY() - 0.5f);

        if (ability1 != null) {
            ability1.update(delta);
        }

        if (ability2 != null) {
            ability2.update(delta);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.Q) && ability1 != null) {
            ability1.activate();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.E) && ability2 != null) {
            ability2.activate();
        }

        if ((Gdx.input.isKeyPressed(Input.Keys.SPACE) || fireGun) && shootTimer > bulletCoolDown) {
            shootTimer = 0;
            shoot.play();
            if(game.server != null) {
                int bullet_id = game.getBulletID();
                game.sendEvent(new ShootEvent(game.id, (byte)-1, (byte) -1, bullet_id, 0));
                fireBullet(bullet_id);
            } else {
                game.sendEvent(new ShootEvent(game.id, (byte)-1, (byte) -1, 0, 0));
            }
        }
        shootTimer += delta;
    }

    private void updateSpectator(float delta) {
        Vector2 deltaPos = new Vector2(0, 0);
        float speed = 15;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            deltaPos.y += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            deltaPos.y -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            deltaPos.x -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            deltaPos.x += 1;
        }
        deltaPos.setLength(speed).scl(delta);
        translate(deltaPos.x, deltaPos.y);
    }

    public void updateRemotePlayer(float delta) {
        if (spectating) {
            updateParticleEffect(delta);
            return;
        }

        float x = getX();
        float y = getY();
        float r = getRotation();

        x += velocity.x * delta;
        y += velocity.y * delta;
        r += rotVelocity * delta;

        setX(x);
        setY(y);
        setRotation(r);
        float volume = game.getVolume(getCenter(), 0.05f);
        if (thrusterID != -1) thruster.setVolume(thrusterID, volume);
        if (thrustAnimationState == 0 && thrusterEffect.isComplete()) {
            thrusterEffect.start();
            if (thrusterID == -1) {
                thruster.setVolume(thrusterID, volume);
                thrusterID = thruster.loop();
            } else {
                thruster.setVolume(thrusterID, volume);
                thruster.resume(thrusterID);
            }

        } else if (thrustAnimationState == 1) {
            thrusterEffect.start();
            thruster.resume(thrusterID);
        } else if (thrustAnimationState == 2) {
            thrusterEffect.allowCompletion();
            thruster.pause(thrusterID);
        }

        updateParticleEffect(delta);
        healthBar.setPosition(getX(), getY() - 0.25f);
    }

    @Override
    public void draw(Batch batch) {
        explosionEffect.draw(batch);
        if (isSpectating()) return;
        if (this != game.players[game.id] && isInvisible) return;
        if (!isInvisible) thrusterEffect.draw(batch);
        healthBar.draw(batch);
        super.draw(batch);
        levelUpEffect.draw(batch);

        if (ability1 != null && this == game.players[game.id]) {
            ability1Bar.setValue(ability1.time / ability1.cooldown);
            ability1Bar.draw(batch);
        }

        if (ability2 != null && this == game.players[game.id]) {
            ability2Bar.setValue(ability2.time / ability2.cooldown);
            ability2Bar.draw(batch);
        }

        if (isInvincible) {
            float radius = ForceField.RADIUS;
            Vector2 pos = getCenter().sub(radius, radius);
            batch.setColor(0.5f, 1, 1, 0.25f);
            batch.draw(game.getEntity(SpaceBattles.RSC_CIRCLE_IMG), pos.x, pos.y, radius * 2, radius * 2);
            batch.setColor(1, 1, 1, 1);
        }
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
        int topTile = (int) ((playerY + radius) / tileSize);
        int bottomTile = (int) ((playerY - radius) / tileSize);

        TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get(0);
        if (layer == null) return;

        for (int tileY = bottomTile; tileY <= topTile; tileY++) {
            for (int tileX = leftTile; tileX <= rightTile; tileX++) {
                TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);
                if (cell == null) continue;
                boolean collides = cell.getTile().getProperties().get("collides", Boolean.class);
                if (!collides) continue;
                handleTileCollision(tileX, tileY, tileSize, playerX, playerY, radius);
            }
        }
    }

    public void collide(Circle c, float bounce) {
        if (!getInCircle().overlaps(c)) return;
        Vector2 btw = getCenter().sub(c.x, c.y);
        setCenter(c.x + btw.x, c.y + btw.y);
        velocity = getCenter().sub(c.x, c.y).setLength(bounce);
    }

    private void setupParticleEffect(SpaceBattles game) {
        thrusterEffect = new ParticleEffect();
        thrusterEffect.load(
            Gdx.files.internal("particles/Thruster.p"),
            game.am.get(SpaceBattles.RSC_PARTICLE_ATLAS, TextureAtlas.class)
        );
        thrusterEffect.scaleEffect(0.03f);

        explosionEffect = new ParticleEffect();
        explosionEffect.load(
            Gdx.files.internal("particles/explosion.p"),
            game.am.get(SpaceBattles.RSC_PARTICLE_ATLAS, TextureAtlas.class)
        );
        explosionEffect.scaleEffect(0.015f);

        levelUpEffect = new ParticleEffect();
        levelUpEffect.load(
            Gdx.files.internal("particles/levelUp.p"),
            game.am.get(SpaceBattles.RSC_PARTICLE_ATLAS, TextureAtlas.class)
        );
        levelUpEffect.scaleEffect(0.015f);
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
                if (thrusterID == -1) {
                    thrusterID = thruster.loop();
                    thruster.setVolume(thrusterID,0.075f);
                } else {
                    thruster.resume(thrusterID);
                }
                thrustAnimationState = 1;
                thrusterEffect.start();
            }
        } else {
            thrustAnimationState = 2;
            thrusterEffect.allowCompletion();
            thruster.pause(thrusterID);
            if (velocity.len() < friction * delta) velocity.setLength(0);
            else velocity.sub(velocity.cpy().setLength(friction * delta));
        }

        velocity.clamp(0, maxSpeed);
    }

    private void updatePosition(float delta) {
        translateX(velocity.x * delta);
        translateY(velocity.y * delta);
    }

    private void updateParticleEffect(float delta) {
        thrusterEffect.update(delta);
        explosionEffect.update(delta);
        levelUpEffect.update(delta);
        if (spectating) return;

        Vector2 origin = getCenter();
        Vector2 offset = getHeadingDir().scl(-size/4);
        thrusterEffect.setPosition(origin.x + offset.x, origin.y + offset.y);

        thrusterEffect.getEmitters().forEach(emitter -> {
            emitter.getAngle().setHigh(getRotation() - 90);
            emitter.getAngle().setLow(getRotation() - 90);
        });

        levelUpEffect.setPosition(origin.x, origin.y);
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
            float overlap = radius - distance + 1e-5f;
            float nx = dx / distance;
            float ny = dy / distance;

            if (Float.isNaN(nx) || Float.isNaN(ny)) return;

            translate(nx * overlap, ny * overlap);
            if (Math.abs(nx) > Math.abs(ny)) velocity.x = 0;
            else velocity.y = 0;
        }
    }

    public void fireBullet(int bulletID) {
        TextureRegion tex = game.getEntity(SpaceBattles.RSC_SQUARE_IMG);
        Vector2 heading = getHeadingDir();
        Vector2 startPos = getCenter().add(heading.cpy().scl(size/2));
        Projectile proj = new Projectile(game, bulletID, tex, startPos.x, startPos.y, bulletSpeed, getRotation() + 90);
        proj.setSize(0.15f, 0.15f);
        proj.setOriginCenter();
        proj.translate(-proj.getOriginX(), -proj.getOriginY());
        proj.damageAmount = bulletDamage;
        proj.team = id;
        proj.setColor(SpaceBattles.PLAYER_COLORS[id]);
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
        game.sendEvent(new MoveEvent(game.id,
            (byte)-1,
            getX(),
            getY(),
            getRotation(),
            velocity.x,
            velocity.y,
            rotVelocity,
            thrustAnimationState
        ));
    }

    @Override
    public float getHealth() {
        return health;
    }

    @Override
    public boolean damage(float amount) {
        if (isInvincible) return false;
        health -= Math.max(amount, 0);
        healthBar.setValue(health);
        if (health > 0) {
            damageID = damage.play();
            damage.setVolume(damageID, game.getVolume(getCenter(), 0.3f));
        }
        if (health <= 0) {
            deathID = death.play();
            death.setVolume(deathID, game.getVolume(getCenter(), 1f));
            Vector2 c = getCenter();
            explosionEffect.setPosition(c.x, c.y);
            explosionEffect.start();
            if (!game.gameStarted) {
                respawn();
                return true;
            }
            setSpectating(true);
            game.bases[id].setRespawnTimer();
            if (thrustAnimationState != 2) thruster.pause(thrusterID);
            return true;
        }
        return false;
    }

    public void respawn() {
        setSpectating(false);
        health = maxHealth;
        healthBar.setValue(health);
        velocity = new Vector2(0, 0);
        rotVelocity = 0;
        setRotation(0);
        Vector2 spawn = new Vector2(0, 0);
        if (game.gameStarted) {
            spawn = game.bases[id].spawnPoint;
        }
        setCenter(spawn.x, spawn.y);

        if (ability1 != null) {
            ability1.reset();
        }

        if (ability2 != null) {
            ability2.reset();
        }
    }

    public void setSpectating(boolean isSpectator) {
        if (isSpectator) {
            thrusterEffect.allowCompletion();
        }
        this.spectating = isSpectator;
    }

    public boolean isSpectating() {
        return spectating;
    }

    @Override
    public void heal(float amount) {
        health += Math.max(amount, 0);
        health = Math.min(health, maxHealth);
    }

    @Override
    public Shape2D getDamageArea() {
        return getOutCircle();
    }

    public Circle getInCircle() {
        return new Circle(getCenter(), getInCircleRadius());
    }

    public Circle getOutCircle() {
        return new Circle(getCenter(), size / 2);
    }

    public int getLevel() {
        return level;
    }

    public int getStatPoints() {
        return statPoints;
    }

    public void setAbility(int aNum, byte aID){
        switch (aID) {
            case Ability.BOMB:
                if (aNum == 1) ability1 = new BombDeploy(this, game, (byte) 1);
                else ability2 = new BombDeploy(this, game, (byte) 2);
                break;
            case Ability.DASH:
                if (aNum == 1) ability1 = new Dash(this, game, (byte) 1);
                else ability2 = new Dash(this, game, (byte) 2);
                break;
            case Ability.FORCE_FIELD:
                if (aNum == 1) ability1 = new ForceField(this, game, (byte) 1);
                else ability2 = new ForceField(this, game, (byte) 2);
                break;
            case Ability.INVISIBILITY:
                if (aNum == 1) ability1 = new Invisibility(this, game, (byte) 1);
                else ability2 = new Invisibility(this, game, (byte) 2);
                break;
            case Ability.RAPID_FIRE:
                if (aNum == 1) ability1 = new RapidFire(this, game, (byte) 1);
                else ability2 = new RapidFire(this, game, (byte) 2);
                break;
        }
    }

    public float getExperience() {
        return experience;
    }

    public void playShoot() {
        long shootID = shoot.play();
        shoot.setVolume(shootID, game.getVolume(getCenter(), 0.3f));
    }
}
