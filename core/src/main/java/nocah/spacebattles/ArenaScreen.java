package nocah.spacebattles;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import nocah.spacebattles.netevents.NetEvent;

public class ArenaScreen extends ScreenAdapter {
    private SpaceBattles game;
    private Player player;
    private Camera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    public ArenaScreen (SpaceBattles game) {
        this.game = game;
        player = new Player(game);
        player.setPosition(1, 1);

        map = game.am.get(SpaceBattles.RSC_TILED_MAP);
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1/32f);

        map.getProperties().getKeys().forEachRemaining(System.out::println);

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
        player.collide(map);
        camera.follow(new Vector2(player.getX(), player.getY()), delta);
    }

    @Override
    public void render(float delta) {
        update(delta);

        game.startWorldDraw(camera.getProjMat());
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        player.draw(game.batch);
        mapRenderer.setView(camera.getOrthCamera());
        mapRenderer.render();
        game.endWorldDraw();

        game.batch.begin();
        game.hud.draw(game.batch);
        game.batch.end();
    }
}
