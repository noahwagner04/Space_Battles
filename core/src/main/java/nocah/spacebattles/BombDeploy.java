package nocah.spacebattles;

import com.badlogic.gdx.math.Vector2;

public class BombDeploy extends Ability {

    public BombDeploy(Player player, SpaceBattles game) {
        super(player, game);
        cooldown = 10f;
        interval = Bomb.EXPLODE_TIME;
    }

    @Override
    public void onActivate() {
        Vector2 spawn = player.getCenter();
        game.bombs.add(new Bomb(player, game, spawn.x, spawn.y));
    }

    @Override
    public void onDeactivate() {

    }
}
