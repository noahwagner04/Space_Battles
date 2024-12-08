package nocah.spacebattles;

public class Dash extends Ability {
    private float maxSpeedCache;
    private float accelerationCache;
    private float frictionCache;

    private float maxRotSpeedCache;
    private float rotAccelerationCache;
    private float rotFrictionCache;

    public Dash(Player player, SpaceBattles game) {
        super(player, game);
        cooldown = 5f;
        interval = 2f;
    }

    @Override
    public void onActivate() {
        player.effect.scaleEffect(1.5f);

        maxSpeedCache = player.maxSpeed;
        accelerationCache = player.acceleration;
        frictionCache = player.friction;

        maxRotSpeedCache = player.maxRotSpeed;
        rotAccelerationCache = player.rotAcceleration;
        rotFrictionCache = player.rotFriction;

        player.maxSpeed = 13;
        player.acceleration = 100;
        player.friction = 100;

        player.maxRotSpeed = 400;
        player.rotAcceleration = 360 * 10;
        player.rotFriction = 360 * 10;
    }

    @Override
    public void onDeactivate() {
        player.effect.scaleEffect(1 / 1.5f);
        player.maxSpeed = player.maxSpeed - 13 + maxSpeedCache;
        player.acceleration = player.acceleration - 100 + accelerationCache;
        player.friction = player.friction - 100 + frictionCache;

        player.maxRotSpeed = player.maxRotSpeed - 400 + maxRotSpeedCache;
        player.rotAcceleration = player.rotAcceleration - 360 * 10 + rotAccelerationCache;
        player.rotFriction = player.rotFriction - 360 * 10 + rotFrictionCache;
    }
}
