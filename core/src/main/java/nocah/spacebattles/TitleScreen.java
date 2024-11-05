package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ScreenUtils;

public class TitleScreen extends ScreenAdapter {
    private SpaceBattles game;
    public TitleScreen(SpaceBattles game) {
        this.game = game;
    }

    @Override
    public void show() {
        System.out.println("Show TitleScreen");
    }

    public void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            game.setScreen(new LobbyScreen(game));
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        ScreenUtils.clear(0f, 1f, 0f, 1f);
    }
}
