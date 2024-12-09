package nocah.spacebattles;

import com.badlogic.gdx.math.Vector2;
import nocah.spacebattles.netevents.ShootEvent;

public class BombDeploy extends Ability {

    public BombDeploy(Player player, SpaceBattles game, byte abilityNum) {
        super(player, game);
        cooldown = 10f;
        interval = Bomb.EXPLODE_TIME;
        abilityID = Ability.BOMB;
        this.abilityNum = abilityNum;
    }

    @Override
    public void onActivate() {
        if (game.server != null) {
            Vector2 spawn = player.getCenter();
            byte bombID = game.getBombID();
            game.bombs.add(new Bomb(player, game, spawn.x, spawn.y, bombID));
            game.sendEvent(new ShootEvent(player.id, (byte) -1, bombID, -1, 0));
        }
    }

    @Override
    public void onDeactivate() {

    }
}
