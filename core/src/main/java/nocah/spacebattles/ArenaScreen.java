package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import nocah.spacebattles.netevents.NetEvent;

public class ArenaScreen extends ScreenAdapter {
    private SpaceBattles game;
    private Player player;
    private Camera camera;
    private Tilemap tilemap;

    public ArenaScreen (SpaceBattles game) {
        this.game = game;
        player = new Player(game);
        player.setPosition(1, 1);
        tilemap = new Tilemap(30, 30, "arena.txt", game.am.get(SpaceBattles.RSC_SQUARE_IMG, Texture.class), 256, 256);
        tilemap.addTile('.', new Tile(0, 0, new Color(0, 0, 0, 0)));
        tilemap.addTile('#', new Tile(0, 0));

        camera = new Camera(15, 15);
    }

    @Override
    public void show() {
        System.out.println("Show ArenaScreen");
    }

    public void update(float delta) {
        if (game.server != null) {
            if (!game.server.eventQueue.isEmpty()) {
                NetEvent event = game.server.eventQueue.poll();
                game.handlers.handleServerEvent(event);
            }
        }
        if (game.client != null) {
            if (!game.client.eventQueue.isEmpty()) {
                NetEvent event = game.client.eventQueue.poll();
                game.handlers.handleClientEvent(event);
            }
        }
        player.update(delta);
        player.collide(tilemap);
        camera.follow(new Vector2(player.getX(), player.getY()), delta);
    }

    @Override
    public void render(float delta) {
        update(delta);

        game.startWorldDraw(camera.getProjMat());
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        tilemap.render(game.batch);
        player.draw(game.batch);
        game.endWorldDraw();

        game.batch.begin();
        game.hud.draw(game.batch);
        game.batch.end();
    }
}
