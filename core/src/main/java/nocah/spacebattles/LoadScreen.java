package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ScreenUtils;

public class LoadScreen extends ScreenAdapter {
    private SpaceBattles game;
    private float timer = 0;
    private float waitTime = 1;
    private boolean canRender = false;

    public LoadScreen(SpaceBattles game) {
        this.game = game;
    }

    @Override
    public void show() {
        System.out.println("Show LoadScreen");
    }

    public void update(float delta) {
        if (game.am.update(16) && timer > waitTime) {
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        if (!game.am.isLoaded("libgdx.png")) {
            return;
        }

        canRender = true;
        timer += Gdx.graphics.getDeltaTime();
    }

    @Override
    public void render(float delta) {
        update(delta);

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        if (!canRender) {
            return;
        }

        game.batch.begin();
        Texture gdxTex = game.am.get("libgdx.png", Texture.class);
        game.batch.draw(
            gdxTex,
            Gdx.graphics.getWidth() / 2f - gdxTex.getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f - gdxTex.getHeight() / 2f
        );
        game.batch.end();
    }
}
