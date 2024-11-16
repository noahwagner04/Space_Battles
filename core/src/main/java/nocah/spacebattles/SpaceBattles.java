package nocah.spacebattles;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import nocah.spacebattles.netevents.*;

import java.util.ArrayList;
import java.util.Iterator;

public class SpaceBattles extends Game {
    public static final String RSC_LIBGDX_IMG = "textures/libgdx.png";
    public static final String RSC_SQUARE_IMG = "square";
    public static final String RSC_CIRCLE_IMG = "circle";
    public static final String RSC_TRIANGLE_IMG = "triangle";
    public static final String[] RSC_ASTEROID_IMGS = {"asteroid1", "asteroid2", "asteroid3"};
    public static final String RSC_ENTITY_ATLAS = "atlases/entities.atlas";
    public static final String RSC_PARTICLE_ATLAS = "atlases/particles.atlas";
    public static final String RSC_TILED_MAP = "BattleArena/BattleArena.tmx";

    public Server server;
    public Client client;
    public String name;
    public HandlerRegistry handlers;
    public Player[] players = new Player[4];
    public byte id;
    public boolean connected = false;
    public boolean gameStarted = false;

    public final float numOfPosSends = 20;
    float posTimer = 0;

    public ArrayList<Projectile> projectiles = new ArrayList<>();
    public ArrayList<Asteroid> asteroids = new ArrayList<>();

    public HUD hud;

    SpriteBatch batch;
    AssetManager am;

    float res = 0.35f;
    FrameBuffer frameBuffer;

    @Override
    public void create() {
        // pick random seed on server, send to clients
        MathUtils.random.setSeed(0);

        am = new AssetManager();
        batch = new SpriteBatch();
        hud = new HUD(new BitmapFont());
        int fb_w = (int)(Gdx.graphics.getWidth() * res);
        int fb_h = (int)(Gdx.graphics.getHeight() * res);
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, fb_w, fb_h, false);
        handlers = new HandlerRegistry(this);
        am.load(RSC_LIBGDX_IMG, Texture.class);

        am.load(RSC_ENTITY_ATLAS, TextureAtlas.class);
        am.load(RSC_PARTICLE_ATLAS, TextureAtlas.class);

        am.setLoader(TiledMap.class, new TmxMapLoader(new InternalFileHandleResolver()));
        am.load(SpaceBattles.RSC_TILED_MAP, TiledMap.class);

        setScreen(new LoadScreen(this));

        name = "default_name";

        hud.registerAction("server", new HUDActionCommand() {
            static final String help = "creates server to listen for clients, and connects this client to it";

            @Override
            public String execute(String[] cmd) {
                try {
                    if (server != null) return "server already hosting";
                    server = new Server();
                    server.startServer();
                    client = new Client("localhost");
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("join", new HUDActionCommand() {
            static final String help = "used to join server: join <server ip>";

            @Override
            public String execute(String[] cmd) {
                try {
                    if (client != null) return "client already connected";
                    client = new Client(cmd[1]);
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("send", new HUDActionCommand() {
            static final String help = "used to send string to server: send <string>";

            @Override
            public String execute(String[] cmd) {
                try {
                    client.sendEvent(new ChatEvent(name, cmd[1]));
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("name", new HUDActionCommand() {
            static final String help = "used to set name: name <string>";

            @Override
            public String execute(String[] cmd) {
                try {
                    name = cmd[1];
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("start", new HUDActionCommand() {
            static final String help = "used to start game if you're the host";

            @Override
            public String execute(String[] cmd) {
                try {
                    if (server == null) return "only the host can start a game!";
                    client.sendEvent(new StartGameEvent());
                    return "ok!";
                } catch (Exception e) {
                    return help;
                }
            }

            public String help(String[] cmd) {
                return help;
            }
        });
    }

    public void startWorldDraw(Matrix4 proj) {
        batch.setProjectionMatrix(proj.cpy());
        frameBuffer.begin();
        batch.begin();
    }

    public void startWorldDraw() {
        frameBuffer.begin();
        batch.begin();
    }

    public void endWorldDraw() {
        batch.end();
        frameBuffer.end();

        ScreenUtils.clear(0f, 0f, 0f, 1f);

        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        batch.begin();
        drawFrameBuffer();
        batch.end();
    }

    public void drawFrameBuffer() {
        Texture tex = frameBuffer.getColorBufferTexture();
        tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        batch.draw(tex, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, 1, 1);
    }

    public TextureRegion getEntity(String name) {
        TextureAtlas atlas = am.get(SpaceBattles.RSC_ENTITY_ATLAS, TextureAtlas.class);
        return atlas.findRegion(name);
    }

    public void drawSprites(Sprite[] sprites) {
        for (Sprite s : sprites) {
            if (s == null) continue;
            s.draw(batch);
        }
    }

    public void drawSprites(ArrayList<? extends Sprite> sprites) {
        for (Sprite s : sprites) {
            s.draw(batch);
        }
    }

    public void updateMainPlayer(float delta, TiledMap map, Rectangle worldBounds) {
        Player p = players[id];
        p.update(delta);

        for (Asteroid a : asteroids) {
            p.collide((Circle)a.getDamageArea());
        }

        if (worldBounds != null) {
            p.constrain(worldBounds);
        }

        if (map != null) {
            p.collide(map);
        }

        if (1 / numOfPosSends < posTimer) {
            p.sendPlayerMoveEvent();
            posTimer -= 1 / numOfPosSends;
        }
    }

    public void updateProjectiles(float delta, TiledMap map, Rectangle worldBounds) {
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile proj = iterator.next();
            proj.update(delta);

            if (proj.checkBounds(worldBounds)){
                iterator.remove();
                continue;
            }

            if (map != null && proj.checkCollides(map)) {
                iterator.remove();
                continue;
            }

            for (int i = 0; i < players.length; i++) {
                Player p = players[i];
                if (p == null) continue;
                if (p.getDamageArea().contains(proj.getCenter())) {
                    iterator.remove();
                    p.damage(proj.damageAmount);
                    break;
                }
            }

            for (Asteroid a : asteroids) {
                if (a.getDamageArea().contains(proj.getCenter())) {
                    iterator.remove();
                    if (a.damage(proj.damageAmount)) {
                        a.randomizePosition(worldBounds);
                    }
                    break;
                }
            }
        }
    }

    public void updateAsteroids(float delta, Rectangle worldBounds) {
        for (Asteroid a : asteroids) {
            a.update(delta);
            a.bounceOffBounds(worldBounds);
        }
    }

    public void spawnRandomAsteroid(Rectangle worldBounds) {
        TextureRegion tex = getEntity(SpaceBattles.RSC_ASTEROID_IMGS[MathUtils.random(0, 2)]);
        asteroids.add(new Asteroid(tex, worldBounds));
    }

    @Override
    public void dispose() {
        batch.dispose();
        am.dispose();

        if (server != null) server.stop();
        if (client != null) {
            client.sendEvent(new DisconnectEvent(id));
            client.stop();
        }
    }

    public void handleNetworkEvents() {
        int maxEventsToHandle = 100;

        if (server != null) {
            int i = 0;
            while (!server.eventQueue.isEmpty() && i < maxEventsToHandle) {
                NetEvent event = server.eventQueue.poll();
                handlers.handleServerEvent(event);
                i++;
            }
        }
        if (client != null) {
            int i = 0;
            while (!client.eventQueue.isEmpty() && i < maxEventsToHandle) {
                NetEvent event = client.eventQueue.poll();
                handlers.handleClientEvent(event);
                i++;
            }
        }
    }

    public void updateRemotePlayers(float delta) {
        for (int i = 0; i < players.length; i++) {
            if (players[i] != null) {
                if (i == id) continue;
                players[i].updateRemotePlayer(delta);
            }
        }
    }
}
