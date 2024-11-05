package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class LobbyScreen extends ScreenAdapter {
    private SpaceBattles game;
    private HUD hud;
    private Player player;

    public LobbyScreen(SpaceBattles game) {
        this.game = game;
        hud = new HUD(new BitmapFont());
        player = new Player(game);

        hud.registerAction("server", new HUDActionCommand() {
            static final String help = "creates server to listen for clients";

            @Override
            public String execute(String[] cmd) {
                try {
                    game.server = new Server();
                    game.server.startServer();
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
                    game.client = new Client(cmd[1]);
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
                    game.client.sendMessage(cmd[1]);
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

    @Override
    public void show() {
        System.out.println("Show LobbyScreen");
    }

    public void update(float delta) {
        player.update(delta);
        player.constrain(new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
    }

    @Override
    public void render(float delta) {
        update(delta);
        game.frameBufferBegin();
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        player.draw(game.batch);
        game.frameBufferEnd();

        game.batch.begin();
        game.drawFrameBuffer();
        hud.draw(game.batch);
        game.batch.end();
    }
}
