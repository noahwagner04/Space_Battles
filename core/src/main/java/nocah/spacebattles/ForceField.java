package nocah.spacebattles;

import com.badlogic.gdx.audio.Sound;

public class ForceField extends Ability {
    public static float RADIUS = 0.75f;
    private Sound forceField;

    public ForceField(Player player, SpaceBattles game, byte abilityNum) {
        super(player, game);
        cooldown = 12f;
        interval = 4f;
        abilityID = FORCE_FIELD;
        this.abilityNum = abilityNum;
        forceField = game.am.get(SpaceBattles.RSC_FORCE_FIELD_SOUND, Sound.class);
    }

    @Override
    public void onActivate() {
        player.isInvincible = true;
        long id = forceField.play();
        forceField.setVolume(id, game.getVolume(player.getCenter(), 1f));
    }

    @Override
    public void onDeactivate() {
        player.isInvincible = false;
    }
}
