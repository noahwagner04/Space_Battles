package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;

public class Tilemap {
    private int width;
    private int height;
    private int[] tiles;

    private Texture atlas;
    private TextureRegion[][] tileRegions;
    private HashMap<Integer, Tile> tileLookUp = new HashMap<>();

    public final int INVALID = -1;

    public Rectangle bounds;

    public Tilemap(int width, int height, String filePath, Texture atlas, int tileWidth, int tileHeight) {
        this.width = width;
        this.height = height;

        bounds = new Rectangle(0, 0, width, height);

        tiles = new int[width * height];

        loadFromFile(filePath);

        this.atlas = atlas;
        tileRegions = TextureRegion.split(
            this.atlas,
            tileWidth,
            tileHeight
        );
    }

    private boolean loadFromFile(String filePath) {
        FileHandle file = Gdx.files.internal(filePath);
        String fileContent = file.readString().replaceAll("\\s+","");

        if (fileContent.length() != width * height) {
            return false;
        }

        for (int i = 0; i < width * height; i++) {
            tiles[i] = fileContent.charAt(i);
        }
        return true;
    }

    public void render(SpriteBatch batch) {
        float tileWidth = getTileWidth();
        float tileHeight = getTileHeight();

        for (int i = 0; i < tiles.length; i++) {
            Vector2 screenPos = transformToPixelCoords(i);
            Tile tile = tileLookUp.get(tiles[i]);
            int row = tile == null ? 0 : tile.texRow;
            int col = tile == null ? 0 : tile.texColumn;
            TextureRegion tr = tileRegions[row][col];
            batch.setColor(tile.tint);
            batch.draw(tr, screenPos.x, screenPos.y, tileWidth, tileHeight);
        }
        batch.setColor(1, 1, 1, 1);
    }

    private Vector2 transformToPixelCoords(int i) {
        int x = i % width;
        int y = (height - 1) - i / width;
        float tw = bounds.width / width;
        float th = bounds.height / height;

        float px = bounds.x + x * tw;
        float py = bounds.y + y * th;
        return new Vector2(px, py);
    }

    public void addTile(int key, Tile t) {
        tileLookUp.put(key, t);
    }

    public int getTileType(float x, float y) {
        if (x < 0 || x >= width) {
            return INVALID;
        }

        if (y < 0 || y >= height) {
            return INVALID;
        }

        int i = (int)x + (int)(height - 1 - y) * width;
        return tiles[i];
    }

    public float getTileWidth() {
        return bounds.width / width;
    }

    public float getTileHeight() {
        return bounds.height / height;
    }
}

class Tile {
    int texRow;
    int texColumn;
    Color tint;
    public Tile(int texRow, int texColumn) {
        this.texRow = texRow;
        this.texColumn = texColumn;
        this.tint = new Color(1, 1, 1, 1);
    }

    public Tile(int texRow, int texColumn, Color tint) {
        this.texRow = texRow;
        this.texColumn = texColumn;
        this.tint = tint;
    }
}
