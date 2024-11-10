package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import nocah.spacebattles.netevents.ChatEvent;
import nocah.spacebattles.netevents.MoveEvent;
import nocah.spacebattles.netevents.NetEvent;
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
    }

    @Override
    public void render(float delta) {
        update(delta);
        game.startWorldDraw(camera.getProjMat());
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        for (Player player: game.players) {
            if (player == null) continue;
            player.draw(game.batch);
        }
        game.endWorldDraw();

        game.batch.begin();
        game.hud.draw(game.batch);
        game.batch.end();
    }
}
