package nocah.spacebattles;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class LobbyScreen extends ScreenAdapter {
    private SpaceBattles game;
    private HUD hud;
    private Player player;
    private boolean is_server = false;
    private Server server;
    private Client client;

    public LobbyScreen(SpaceBattles game) {
        this.game = game;
        this.hud = new HUD(new BitmapFont());
        this.player = new Player(game);

        hud.registerAction("server", new HUDActionCommand() {
            static final String help = "creates server to listen for clients";

            @Override
            public String execute(String[] cmd) {
                try {
                    is_server = true;
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
                    client.sendMessage(cmd[1]);
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
    }

    @Override
    public void render(float delta) {
        update(delta);
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        game.batch.begin();
        player.draw(game.batch);
        hud.draw(game.batch);
        game.batch.end();
    }
}
