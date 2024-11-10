package nocah.spacebattles;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import nocah.spacebattles.netevents.MoveEvent;
import nocah.spacebattles.netevents.NetEvent;

public class ArenaScreen extends ScreenAdapter {
    private SpaceBattles game;
    private Player thisPlayer;
    private Camera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    public ArenaScreen (SpaceBattles game) {
        this.game = game;

        thisPlayer = game.players[game.id];
        thisPlayer.setPosition(1, 1);
        map = game.am.get(SpaceBattles.RSC_TILED_MAP);
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1/32f);
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
            camera.follow(new Vector2(thisPlayer.getX(), thisPlayer.getY()), delta);
            game.client.sendEvent(new MoveEvent(game.id,
                thisPlayer.getX(),
                thisPlayer.getY(),
                thisPlayer.getRotation()
            ));
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        game.startWorldDraw(camera.getProjMat());
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        mapRenderer.setView(camera.getOrthCamera());
        mapRenderer.render();
        for (Player player: game.players) {
            if (player == null) continue;
            player.draw(game.batch);
        }
        game.endWorldDraw();

        game.batch.begin();
        game.hud.draw(game.batch);
        game.batch.end();
    }
}
