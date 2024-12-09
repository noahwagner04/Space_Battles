package nocah.spacebattles;

public class RapidFire extends Ability {
    private float bulletCoolDownCache;
    private float shootKnockBackCache;

    public RapidFire(Player player, SpaceBattles game, byte abilityNum) {
        super(player, game);
        cooldown = 10;
        interval = 3;
        abilityID = Ability.RAPID_FIRE;
        this.abilityNum = abilityNum;
    }

    @Override
    public void onActivate() {
        player.fireGun = true;
        bulletCoolDownCache = player.bulletCoolDown;
        player.bulletCoolDown = 0.1f;

        shootKnockBackCache = player.shootKnockBack;
        player.shootKnockBack = 0.5f;
    }

    @Override
    public void onDeactivate() {
        player.fireGun = false;
        player.bulletCoolDown = player.bulletCoolDown - 0.1f + bulletCoolDownCache;
        player.shootKnockBack = player.shootKnockBack - 0.5f + shootKnockBackCache;
    }
}
