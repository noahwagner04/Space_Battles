package nocah.spacebattles;

public class ForceField extends Ability {
    public static float RADIUS = 0.75f;

    public ForceField(Player player, SpaceBattles game, byte abilityNum) {
        super(player, game);
        cooldown = 12f;
        interval = 4f;
        abilityID = FORCE_FIELD;
        this.abilityNum = abilityNum;

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
