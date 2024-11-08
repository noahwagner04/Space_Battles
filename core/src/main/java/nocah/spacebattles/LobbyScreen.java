package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import nocah.spacebattles.netevents.ChatEvent;

public class LobbyScreen extends ScreenAdapter {
    private SpaceBattles game;
    private Player player;

    public LobbyScreen(SpaceBattles game) {
        this.game = game;
        player = new Player(game);

    }

    @Override
    public void show() {
        System.out.println("Show LobbyScreen");
    }

    public void update(float delta) {
        player.update(delta);
        player.constrain(new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            game.setScreen(new ArenaScreen(game));
        }
    }

    @Override
    public void render(float delta) {
        update(delta);
        game.startWorldDraw();
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        player.draw(game.batch);
        game.endWorldDraw();

        game.batch.begin();
        game.hud.draw(game.batch);
        game.batch.end();
    }
}
