package nocah.spacebattles;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class LobbyScreen extends ScreenAdapter {
    private SpaceBattles game;
    private HUD hud;

    public LobbyScreen(SpaceBattles game) {
        this.game = game;
        this.hud = new HUD(new BitmapFont());
    }

    @Override
    public void show() {
        System.out.println("Show LobbyScreen");
    }

    public void update(float delta) {

    }

    @Override
    public void render(float delta) {
        update(delta);
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        game.batch.begin();
        hud.draw(game.batch);
        game.batch.end();
    }
}
