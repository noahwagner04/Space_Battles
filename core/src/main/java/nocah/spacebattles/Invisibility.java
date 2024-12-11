package nocah.spacebattles;

import com.badlogic.gdx.audio.Sound;

public class Invisibility extends Ability {
    public static float RADIUS = 0.75f;
    private Sound invisibility;
    private Sound uninvisibility;

    public Invisibility(Player player, SpaceBattles game, byte abilityNum) {
        super(player, game);
        cooldown = 15f;
        interval = 5f;
        abilityID = Ability.INVISIBILITY;
        this.abilityNum = abilityNum;
        invisibility = game.am.get(SpaceBattles.RSC_INVISIBILITY_SOUND, Sound.class);
        uninvisibility = game.am.get(SpaceBattles.RSC_UNINVISIBILITY_SOUND, Sound.class);
    }

    @Override
    public void onActivate() {
        player.isInvisible = true;
        long id = invisibility.play();
        invisibility.setVolume(id, game.getVolume(player.getCenter(), 1f));
        player.setAlpha(0.3f);
    }

    @Override
    public void onDeactivate() {
        player.isInvisible = false;
        long id = uninvisibility.play();
        uninvisibility.setVolume(id, game.getVolume(player.getCenter(), 1f));
        player.setAlpha(1f);
    }
}
