package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import nocah.spacebattles.netevents.MoveEvent;
import nocah.spacebattles.netevents.SpawnEvent;
import nocah.spacebattles.netevents.StartGameEvent;

public class LobbyScreen extends ScreenAdapter {
    private SpaceBattles game;
    private Camera camera;
    private Stage stage;

    private Rectangle lobbyBounds = new Rectangle(-6, -6, 12, 12);

    public LobbyScreen(SpaceBattles game) {
        this.game = game;
        camera = new Camera(lobbyBounds.width, lobbyBounds.height);
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        table.bottom();
        stage.addActor(table);

        TextButton startButton = new TextButton("Start Game", SpaceBattles.skin);
        startButton.setSize(100, 25);
        startButton.getLabel().setFontScale(1.5f);
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (game.server != null) {
                    game.sendEvent(new StartGameEvent());
                    game.gameStarted = true;
                }
            }
        });
        table.add(startButton).pad(20).width(300).height(80);
        game.sendEvent(new SpawnEvent(game.id));
    }

    public void update(float delta) {
        game.posTimer += delta;
        game.handleNetworkEvents();

        Player thisPlayer = game.players[game.id];
        if (thisPlayer != null) {
            game.updateMainPlayer(delta, null, lobbyBounds);
        }

        game.updateRemotePlayers(delta);


        if (game.gameStarted) {
            game.setScreen(new ArenaScreen(game));
            if (game.server != null) game.server.stopListening();
        }
        game.updateProjectiles(delta, null, lobbyBounds);
    }

    @Override
    public void render(float delta) {
        update(delta);

        game.startWorldDraw(camera.getProjMat());
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        game.drawSprites(game.players);
        game.drawSprites(game.projectiles);
        game.endWorldDraw();

        if (game.server != null) {
            stage.act(delta);
            stage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide() {
        stage.dispose();
    }
}
