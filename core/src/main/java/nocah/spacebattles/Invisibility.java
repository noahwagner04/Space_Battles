package nocah.spacebattles;

public class Invisibility extends Ability {
    public static float RADIUS = 0.75f;

    public Invisibility(Player player, SpaceBattles game) {
        super(player, game);
        cooldown = 15f;
        interval = 5f;
    }

    @Override
    public void onActivate() {
        player.isInvisible = true;
        player.setAlpha(0.3f);
    }

    @Override
    public void onDeactivate() {
        player.isInvisible = false;
        player.setAlpha(1f);
    }
}
