package nocah.spacebattles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class StatusBar {
    public static final Color HP_B = new Color(0.3f, 0, 0, 1);
    public static final Color HP_F = new Color(0, 1, 0, 1);

    private TextureRegion blankTexture;
    private Color backgroundColor;
    private Color fillColor;
    private float x, y, width, height;
    private float minValue, maxValue, currentValue;

    public boolean noDrawOnFull = false;

    public StatusBar(SpaceBattles game, Color background, Color fill, float x, float y, float width, float height) {
        this.backgroundColor = background;
        this.fillColor = fill;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.minValue = 0;
        this.maxValue = 1;
        this.currentValue = 0.5f;

        blankTexture = game.getEntity(SpaceBattles.RSC_SQUARE_IMG);
    }

    public void setRange(float min, float max) {
        minValue = min;
        maxValue = max;
    }

    public void setValue(float value) {
        currentValue = Math.max(minValue, Math.min(maxValue, value));
    }

    public float getValue() {
        return currentValue;
    }

    public void draw(Batch batch) {
        if (noDrawOnFull && Math.abs(currentValue - maxValue) < 1e-5f) return;

        Color colorBefore = batch.getColor().cpy();

        batch.setColor(backgroundColor);
        batch.draw(blankTexture, x, y, width, height);

        float fillWidth = (currentValue - minValue) / (maxValue - minValue) * width;

        batch.setColor(fillColor);
        batch.draw(blankTexture, x, y, fillWidth, height);

        batch.setColor(colorBefore);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }
}
