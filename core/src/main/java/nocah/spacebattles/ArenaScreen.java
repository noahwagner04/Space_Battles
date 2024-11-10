package nocah.spacebattles;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import nocah.spacebattles.netevents.MoveEvent;
import nocah.spacebattles.netevents.NetEvent;

import java.util.Iterator;

public class ArenaScreen extends ScreenAdapter {
    private SpaceBattles game;
    private Player thisPlayer;
    private Camera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Rectangle worldBounds;

    public ArenaScreen (SpaceBattles game) {
        this.game = game;

        thisPlayer = game.players[game.id];
        thisPlayer.setPosition(1, 1);
        map = game.am.get(SpaceBattles.RSC_TILED_MAP);
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f/map.getProperties().get("tilewidth", Integer.class));
        worldBounds = new Rectangle(
            0,
            0,
            map.getProperties().get("width", Integer.class),
            map.getProperties().get("height", Integer.class)
        );

        camera = new Camera(15, 15);
    }

    @Override
    public void show() {
        System.out.println("Show ArenaScreen");
    }

    public void update(float delta) {
        game.handleNetworkEvents();

        if (thisPlayer != null) {
            thisPlayer.update(delta);
            thisPlayer.collide(map);
            camera.follow(thisPlayer.getCenter(), delta);
            game.client.sendEvent(new MoveEvent(game.id,
                thisPlayer.getX(),
                thisPlayer.getY(),
                thisPlayer.getRotation()
            ));
        }
        game.updateProjectiles(delta, map, worldBounds);
    }

    @Override
    public void render(float delta) {
        update(delta);

        game.startWorldDraw(camera.getProjMat());
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        mapRenderer.setView(camera.getOrthCamera());
        mapRenderer.render();
        game.drawPlayers();
        game.drawProjectiles();
        game.endWorldDraw();

        game.batch.begin();
        game.hud.draw(game.batch);
        game.batch.end();
    }
}
