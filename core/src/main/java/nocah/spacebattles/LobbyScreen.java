package nocah.spacebattles;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;

public class LobbyScreen extends ScreenAdapter {
    private SpaceBattles game;
    private HUD hud;
    private Player player;

    public LobbyScreen(SpaceBattles game) {
        this.game = game;
        this.hud = new HUD(new BitmapFont());
        this.player = new Player(game);
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
