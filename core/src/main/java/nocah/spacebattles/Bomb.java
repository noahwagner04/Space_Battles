package nocah.spacebattles;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Bomb extends Sprite {
    SpaceBattles game;
    Player player;

    public static float EXPLODE_TIME = 1f;

    private float timer = 0;

    private int bulletCount = 36;
    private float bulletSpeed = 15;
    private float bulletDamage = 50;

    public boolean hasDetonated = false;

    public Bomb(Player player, SpaceBattles game, float x, float y) {
        super(game.getEntity(SpaceBattles.RSC_CIRCLE_IMG));
        this.game = game;
        this.player = player;
        setSize(0.5f, 0.5f);
        setOriginCenter();
        setCenter(x, y);
    }

    public void update(float delta) {
        if (!hasDetonated && timer > EXPLODE_TIME) {
            explode();
            return;
        }
        timer += delta;
    }

    private void explode() {
        hasDetonated = true;
        for (int i = 0; i < bulletCount; i++) {
            TextureRegion tex = game.getEntity(SpaceBattles.RSC_SQUARE_IMG);
            Vector2 startPos = new Vector2(getX() + getOriginX(), getY() + getOriginY());
            Projectile proj = new Projectile(-1, tex, startPos.x, startPos.y, bulletSpeed, i / (float)bulletCount * 360);
            proj.setSize(0.15f, 0.15f);
            proj.setOriginCenter();
            proj.translate(-proj.getOriginX(), -proj.getOriginY());
            proj.damageAmount = bulletDamage;
            proj.team = player.id;
            game.projectiles.add(proj);
        }
    }
}
