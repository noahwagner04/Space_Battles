package nocah.spacebattles;

public class ForceField extends Ability {
    public static float RADIUS = 0.75f;

    public ForceField(Player player, SpaceBattles game) {
        super(player, game);
        cooldown = 12f;
        interval = 3f;
    }

    @Override
    public void onActivate() {
        player.isInvincible = true;
    }

    @Override
    public void onDeactivate() {
        player.isInvincible = false;
    }
}
