package nocah.spacebattles;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import nocah.spacebattles.netevents.MoveEvent;
import nocah.spacebattles.netevents.SpawnEvent;

public class LobbyScreen extends ScreenAdapter {
    private SpaceBattles game;
    private Camera camera;

    private Rectangle lobbyBounds = new Rectangle(-6, -6, 12, 12);

    public LobbyScreen(SpaceBattles game) {
        this.game = game;
        camera = new Camera(lobbyBounds.width, lobbyBounds.height);

    }

    @Override
    public void show() {
        System.out.println("Show LobbyScreen");
        game.client.sendEvent(new SpawnEvent(game.id));
    }

    public void update(float delta) {

        game.handleNetworkEvents();

        Player thisPlayer = game.players[game.id];
        if (thisPlayer != null) {
            thisPlayer.update(delta);
            thisPlayer.constrain(lobbyBounds);
            game.client.sendEvent(new MoveEvent(game.id,
                thisPlayer.getX(),
                thisPlayer.getY(),
                thisPlayer.getRotation()
            ));
        }

        if (game.gameStarted) {
            game.setScreen(new ArenaScreen(game));
        }
        game.updateProjectiles(delta, null, lobbyBounds);
    }

    @Override
    public void render(float delta) {
        update(delta);
        game.startWorldDraw(camera.getProjMat());
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        game.drawPlayers();
        game.drawProjectiles();
        game.endWorldDraw();

        game.batch.begin();
        game.hud.draw(game.batch);
        game.batch.end();
    }
}
