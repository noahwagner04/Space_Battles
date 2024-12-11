package nocah.spacebattles;

import com.badlogic.gdx.audio.Sound;

public class Dash extends Ability {
    private float maxSpeedCache;
    private float accelerationCache;
    private float frictionCache;

    private float maxRotSpeedCache;
    private float rotAccelerationCache;
    private float rotFrictionCache;

    private Sound dash;

    public Dash(Player player, SpaceBattles game, byte abilityNum) {
        super(player, game);
        cooldown = 5f;
        interval = 2f;
        abilityID = Ability.DASH;
        this.abilityNum = abilityNum;
        dash = game.am.get(SpaceBattles.RSC_DASH_SOUND, Sound.class);
    }

    @Override
    public void onActivate() {
        player.thrusterEffect.scaleEffect(1.5f);
        long dashID = dash.play();
        dash.setVolume(dashID, game.getVolume(player.getCenter(), 1f));
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
        player.thrusterEffect.scaleEffect(1 / 1.5f);
        player.maxSpeed = player.maxSpeed - 13 + maxSpeedCache;
        player.acceleration = player.acceleration - 100 + accelerationCache;
        player.friction = player.friction - 100 + frictionCache;

        player.maxRotSpeed = player.maxRotSpeed - 400 + maxRotSpeedCache;
        player.rotAcceleration = player.rotAcceleration - 360 * 10 + rotAccelerationCache;
        player.rotFriction = player.rotFriction - 360 * 10 + rotFrictionCache;
    }
}
