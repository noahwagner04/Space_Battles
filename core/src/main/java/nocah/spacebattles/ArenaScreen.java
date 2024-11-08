package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

public class ArenaScreen extends ScreenAdapter {
    private SpaceBattles game;
    private Player player;
    private Camera camera;

    public ArenaScreen (SpaceBattles game) {
        this.game = game;
        player = new Player(game);

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        camera = new Camera(w, h);
    }

    @Override
    public void show() {
        System.out.println("Show ArenaScreen");
    }

    public void update(float delta) {
        if (game.server != null) game.server.broadcastMessageInQueue();
        if (game.client != null) game.client.printMessageInQueue();
        player.update(delta);
        camera.follow(new Vector2(player.getX(), player.getY()), delta);
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
