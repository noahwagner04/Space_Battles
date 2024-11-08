package nocah.spacebattles;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.ScreenUtils;
import nocah.spacebattles.netevents.ChatEvent;

public class SpaceBattles extends Game {
    public static final String RSC_LIBGDX_IMG = "libgdx.png";
    public static final String RSC_SQUARE_IMG = "square.png";
    public static final String RSC_CIRCLE_IMG = "circle.png";
    public static final String RSC_TRIANGLE_IMG = "triangle.png";

    Server server;
    Client client;
    private String name;


    public HUD hud;


    SpriteBatch batch;
    AssetManager am;

    float res = 0.35f;
    FrameBuffer frameBuffer;

    @Override
    public void create() {
        am = new AssetManager();
        batch = new SpriteBatch();
        hud = new HUD(new BitmapFont());
        int fb_w = (int)(Gdx.graphics.getWidth() * res);
        int fb_h = (int)(Gdx.graphics.getHeight() * res);
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, fb_w, fb_h, false);

        am.load(RSC_LIBGDX_IMG, Texture.class);
        am.load(RSC_SQUARE_IMG, Texture.class);
        am.load(RSC_CIRCLE_IMG, Texture.class);
        am.load(RSC_TRIANGLE_IMG, Texture.class);

        setScreen(new LoadScreen(this));

        name = "default_name";

        hud.registerAction("server", new HUDActionCommand() {
            static final String help = "creates server to listen for clients";

            @Override
            public String execute(String[] cmd) {
                try {
                    server = new Server();
                    server.startServer();
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("join", new HUDActionCommand() {
            static final String help = "used to join server: join <server ip>";

            @Override
            public String execute(String[] cmd) {
                try {
                    client = new Client(cmd[1]);
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("send", new HUDActionCommand() {
            static final String help = "used to send string to server: send <string>";

            @Override
            public String execute(String[] cmd) {
                try {
                    client.sendEvent(new ChatEvent(name, cmd[1]));
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("name", new HUDActionCommand() {
            static final String help = "used to set name: name <string>";

            @Override
            public String execute(String[] cmd) {
                try {
                    name = cmd[1];
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });
    }

    public void startWorldDraw(Matrix4 proj) {
        batch.setProjectionMatrix(proj.cpy());
        frameBuffer.begin();
        batch.begin();
    }

    public void startWorldDraw() {
        frameBuffer.begin();
        batch.begin();
    }

    public void endWorldDraw() {
        batch.end();
        frameBuffer.end();

        ScreenUtils.clear(0f, 0f, 0f, 1f);

        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        batch.begin();
        drawFrameBuffer();
        batch.end();
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
