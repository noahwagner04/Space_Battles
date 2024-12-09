package nocah.spacebattles;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import nocah.spacebattles.netevents.ShootEvent;

public class Bomb extends Sprite {
    SpaceBattles game;
    Player player;

    public static float EXPLODE_TIME = 1f;
    public int id;

    private float timer = 0;

    private int bulletCount = 36;
    public float bulletSpeed = 15;
    private float bulletDamage = 50;

    public boolean hasDetonated = false;

    public Bomb(Player player, SpaceBattles game, float x, float y, int id) {
        super(game.getEntity(SpaceBattles.RSC_CIRCLE_IMG));
        this.game = game;
        this.player = player;
        this.id = id;
        setSize(0.5f, 0.5f);
        setOriginCenter();
        setCenter(x, y);
        setColor(SpaceBattles.PLAYER_COLORS[player.id]);
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
            int bulletID = game.getBulletID();
            float rot = i / (float)bulletCount * 360;
            Projectile proj = new Projectile(bulletID, tex, startPos.x, startPos.y, bulletSpeed, rot);
            game.sendEvent(new ShootEvent(player.id, (byte) -1, (byte)id, bulletID, rot));
            proj.setSize(0.15f, 0.15f);
            proj.setOriginCenter();
            proj.translate(-proj.getOriginX(), -proj.getOriginY());
            proj.damageAmount = bulletDamage;
            proj.team = player.id;
            proj.setColor(SpaceBattles.PLAYER_COLORS[player.id]);
            game.projectiles.add(proj);
        }
    }
}
