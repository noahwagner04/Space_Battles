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

public class LobbyScreen extends ScreenAdapter {
    private SpaceBattles game;
    private Player player;
    private Camera camera;

    private Rectangle lobbyBounds = new Rectangle(-6, -6, 12, 12);

    public LobbyScreen(SpaceBattles game) {
        this.game = game;
        player = new Player(game);
        camera = new Camera(lobbyBounds.width, lobbyBounds.height);

    }

    @Override
    public void show() {
        System.out.println("Show LobbyScreen");
    }

    public void update(float delta) {
        player.update(delta);
        player.constrain(lobbyBounds);

        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            game.setScreen(new ArenaScreen(game));
        }
    }

    @Override
    public void render(float delta) {
        update(delta);
        game.startWorldDraw(camera.getProjMat());
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        player.draw(game.batch);
        game.endWorldDraw();

        game.batch.begin();
        game.hud.draw(game.batch);
        game.batch.end();
    }
}
