package nocah.spacebattles;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class SpaceBattles extends Game {
    public static final String RSC_LIBGDX_IMG = "libgdx.png";
    public static final String RSC_SQUARE_IMG = "square.png";
    public static final String RSC_CIRCLE_IMG = "circle.png";
    public static final String RSC_TRIANGLE_IMG = "triangle.png";

    SpriteBatch batch;
    AssetManager am;

    @Override
    public void create() {
        am = new AssetManager();
        batch = new SpriteBatch();

        am.load(RSC_LIBGDX_IMG, Texture.class);
        am.load(RSC_SQUARE_IMG, Texture.class);
        am.load(RSC_CIRCLE_IMG, Texture.class);
        am.load(RSC_TRIANGLE_IMG, Texture.class);

        setScreen(new LoadScreen(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
        am.dispose();
    }
}
