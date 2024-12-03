package nocah.spacebattles;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Projectile extends Sprite {
    private Vector2 velocity;
    private float speed;
    public float damageAmount = 0;
    public Damageable target = null;
    private int id;


    // 0-3, indicates what team fired this bullet
    public int team;

    public Projectile(int id, TextureRegion texture, float x, float y, float speed, float angle) {
        super(texture);
        setPosition(x, y);
        this.speed = speed;
        velocity = new Vector2(speed, 0).rotateDeg(angle);
        this.id = id;
    }

    public void setTarget(Damageable target) {
        this.target = target;
    }

    public void update(float delta) {
        setPosition(getX() + velocity.x * delta, getY() + velocity.y * delta);
    }

    public boolean checkCollides(TiledMap tiledMap) {
        TiledMapTileLayer layer = (TiledMapTileLayer) tiledMap.getLayers().get(0);

        if (layer == null) return false;

        int tileX = (int)getX();
        int tileY = (int)getY();

        TiledMapTileLayer.Cell cell = layer.getCell(tileX, tileY);

        if (cell == null) return false;

        MapProperties properties = cell.getTile().getProperties();
        return properties.containsKey("collides");
    }

    public boolean checkBounds(Rectangle worldBounds) {
        Rectangle myRect = getBoundingRectangle();

        return myRect.x + myRect.width < worldBounds.x ||
            myRect.x > worldBounds.x + worldBounds.width ||
            myRect.y + myRect.height < worldBounds.y ||
            myRect.y > worldBounds.y + worldBounds.height;
    }

    public Vector2 getCenter() {
        return new Vector2(getX() + getOriginX(), getY() + getOriginY());
    }

    public int getID() {
        return id;
    }
}
