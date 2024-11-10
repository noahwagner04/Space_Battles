package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;
import nocah.spacebattles.netevents.NetEvent;

public class MainMenuScreen extends ScreenAdapter {
    private SpaceBattles game;
    public MainMenuScreen(SpaceBattles game) {
        this.game = game;
    }

    @Override
    public void show() {
        System.out.println("Show TitleScreen");
    }

    public void update(float delta) {

        game.handleNetworkEvents();
        if (game.connected) {
            game.setScreen(new LobbyScreen(game));
        }
    }

    @Override
    public void render(float delta) {
        update(delta);
        ScreenUtils.clear(0f, 1f, 0f, 1f);
        game.batch.begin();
        game.hud.draw(game.batch);
        game.batch.end();
    }
}
