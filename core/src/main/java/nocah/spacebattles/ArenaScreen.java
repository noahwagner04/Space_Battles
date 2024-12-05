package nocah.spacebattles;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

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
        map = game.am.get(SpaceBattles.RSC_TILED_MAP);
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f/map.getProperties().get("tilewidth", Integer.class));
        worldBounds = new Rectangle(
            0,
            0,
            map.getProperties().get("width", Integer.class),
            map.getProperties().get("height", Integer.class)
        );

        game.setBases(worldBounds);
        thisPlayer.respawn();

        for (int i = 0; i < game.asteroids.length; i++) {
            game.asteroids[i] = new Asteroid(game, worldBounds);
        }
        camera = new Camera(15, 15);
    }

    @Override
    public void show() {
        System.out.println("Show ArenaScreen");
    }

    public void update(float delta) {
        game.posTimer += delta;
        game.minionPosTimer += delta;
        game.handleNetworkEvents();

        if (thisPlayer != null) {
            game.updateMainPlayer(delta, map, worldBounds);
            camera.follow(thisPlayer.getCenter(), delta);
        }

        game.updateBases(delta);
        game.updateRemotePlayers(delta);
        game.updateProjectiles(delta, map, worldBounds);
        game.updateAsteroids(delta, worldBounds);
        game.updateMinions(delta, map);

    }

    @Override
    public void render(float delta) {
        update(delta);

        game.startWorldDraw(camera.getProjMat());
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        mapRenderer.setView(camera.getOrthCamera());
        mapRenderer.render();
        game.drawSprites(game.players);
        game.drawSprites(game.projectiles);
        game.drawSprites(game.asteroids);
        game.drawSprites(game.bases);
        for(int i = 0; i < SpaceBattles.MAX_PLAYERS; i++) {
            game.drawSprites(game.minions[i]);
        }
        game.endWorldDraw();

        game.batch.begin();
        game.hud.draw(game.batch);
        game.batch.end();
    }
}
