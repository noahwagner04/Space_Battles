package nocah.spacebattles;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.utils.ScreenUtils;

public class SpaceBattles extends Game {
    public static final String RSC_LIBGDX_IMG = "libgdx.png";
    public static final String RSC_SQUARE_IMG = "square.png";
    public static final String RSC_CIRCLE_IMG = "circle.png";
    public static final String RSC_TRIANGLE_IMG = "triangle.png";

    Server server;
    Client client;

    SpriteBatch batch;
    AssetManager am;

    float res = 0.35f;
    FrameBuffer frameBuffer;

    @Override
    public void create() {
        am = new AssetManager();
        batch = new SpriteBatch();
        int fb_w = (int)(Gdx.graphics.getWidth() * res);
        int fb_h = (int)(Gdx.graphics.getHeight() * res);
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, fb_w, fb_h, false);

        am.load(RSC_LIBGDX_IMG, Texture.class);
        am.load(RSC_SQUARE_IMG, Texture.class);
        am.load(RSC_CIRCLE_IMG, Texture.class);
        am.load(RSC_TRIANGLE_IMG, Texture.class);

        setScreen(new LoadScreen(this));
    }

    public void frameBufferBegin() {
        frameBuffer.begin();
        batch.begin();
    }

    public void frameBufferEnd() {
        batch.end();
        frameBuffer.end();
    }

    public void drawFrameBuffer() {
        Texture tex = frameBuffer.getColorBufferTexture();
        tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        batch.draw(tex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1, 1);
    }

    @Override
    public void dispose() {
        batch.dispose();
        am.dispose();

        if (server != null) server.stop();
        if (client != null) client.stop();
    }
}
