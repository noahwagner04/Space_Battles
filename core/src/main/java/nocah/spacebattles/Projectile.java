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

    public Projectile(TextureRegion texture, float x, float y, float speed, float angle) {
        super(texture);
        setPosition(x, y);
        this.speed = speed;
        velocity = new Vector2(speed, 0).rotateDeg(angle);
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

    public boolean checkCollides(Circle c) {
        float dst = new Vector2(c.x, c.y).dst(getX() + getOriginX(), getY() + getOriginY());
        return dst < c.radius + getWidth();
    }

    public boolean checkBounds(Rectangle worldBounds) {
        Rectangle myRect = getBoundingRectangle();

        return myRect.x + myRect.width < worldBounds.x ||
            myRect.x > worldBounds.x + worldBounds.width ||
            myRect.y + myRect.height < worldBounds.y ||
            myRect.y > worldBounds.y + worldBounds.height;
    }
}
