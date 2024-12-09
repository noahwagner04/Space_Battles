package nocah.spacebattles;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.ScreenUtils;
import nocah.spacebattles.netevents.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class SpaceBattles extends Game {
    public static final String RSC_LIBGDX_IMG = "textures/libgdx.png";
    public static final String RSC_SQUARE_IMG = "square";
    public static final String RSC_CIRCLE_IMG = "circle";
    public static final String RSC_TRIANGLE_IMG = "triangle";
    public static final String[] RSC_ASTEROID_IMGS = {"asteroid1", "asteroid2", "asteroid3"};
    public static final String RSC_ENTITY_ATLAS = "atlases/entities.atlas";
    public static final String RSC_PARTICLE_ATLAS = "atlases/particles.atlas";
    public static final String RSC_TILED_MAP = "BattleArena/BattleArena.tmx";

    public static final int MAX_PLAYERS = 4;
    public static final int MAX_ASTEROIDS = 20;
    public static final int MAX_MINIONS = 10;

    public Server server;
    public Client client;
    public String name;
    public HandlerRegistry handlers;
    public Player[] players = new Player[MAX_PLAYERS];
    public PlayerBase[] bases = new PlayerBase[MAX_PLAYERS];
    public byte id;
    public boolean connected = false;
    public boolean gameStarted = false;
    private int nextBulletId = 0;

    public final float numOfPosSends = 20;
    float posTimer = 0;
    float minionPosTimer = 0;

    public ArrayList<Projectile> projectiles = new ArrayList<>();
    public Asteroid[] asteroids = new Asteroid[MAX_ASTEROIDS];
    public Minion[][] minions = new Minion[MAX_PLAYERS][MAX_MINIONS];

    public ArrayList<Bomb> bombs = new ArrayList<>();

    public static final Color[] PLAYER_COLORS = {
        new Color(0.3f, 1, 1, 1),
        new Color(0.3f, 1f, 0.3f, 1),
        new Color(1, 1, 0.3f, 1),
        new Color(1, 0.3f, 0.3f, 1)
    };

    SpriteBatch batch;
    AssetManager am;

    // pick random seed on server, send to clients
    public static Random random = new Random(0);

    float res = 1f;
    FrameBuffer frameBuffer;

    @Override
    public void create() {
        am = new AssetManager();
        batch = new SpriteBatch();
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

    public void setBases(Rectangle worldBounds) {
        Vector2 pad = new Vector2(3, 3);

        // bottom left
        if (players[0] == null) return;
        bases[0] = new PlayerBase(this, 0, pad.x, pad.y);
        bases[0].spawnPoint.y += 2;

        // top right
        if (players[1] == null) return;
        bases[1] = new PlayerBase(this, 1, worldBounds.width - pad.x, worldBounds.height - pad.y);
        bases[1].spawnPoint.y -= 2;

        // bottom right
        if (players[2] == null) return;
        bases[2] = new PlayerBase(this, 2, worldBounds.width - pad.x, pad.y);
        bases[2].spawnPoint.y += 2;

        // top left
        if (players[3] == null) return;
        bases[3] = new PlayerBase(this, 3, pad.x, worldBounds.height - pad.y);
        bases[3].spawnPoint.y -= 2;
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

        if (worldBounds != null) {
            p.constrain(worldBounds);
        }

        if (p.isSpectating()) return;

        if (map != null) {
            p.collide(map);
        }

        for (Asteroid a : asteroids) {
            if (a == null) continue;
            Circle asteroidCircle = (Circle)a.getDamageArea();
            float damageAmount = 10;
            if (!p.getInCircle().overlaps(asteroidCircle)) continue;
            sendEvent(new DamageEvent(
                (byte)NetConstants.PLAYER_ENTITY_TYPE,
                id,
                id,
                damageAmount,
                a.getHealth()
            ));
            p.damage(damageAmount);
            p.collide(asteroidCircle, 3);
        }

        for (PlayerBase b : bases) {
            if (b == null || b.isDestroyed()) continue;
            p.collide((Circle)b.getDamageArea(), 2);
        }

        if (1 / numOfPosSends < posTimer) {
            p.sendPlayerMoveEvent();
            posTimer -= 1 / numOfPosSends;
        }
    }

    public void updateBases(float delta) {
        for(PlayerBase b : bases) {
            if (b == null) continue;
            b.update(delta);
        }
    }

    public void updateProjectiles(float delta, TiledMap map, Rectangle worldBounds) {
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile proj = iterator.next();
            proj.update(delta);
            if (server == null) continue;
            boolean removed = false;

            DamageEvent despawnProjectile = new DamageEvent(
                (byte)NetConstants.PROJECTILE_ENTITY_TYPE,
                (byte) proj.team,
                proj.getID(),
                0,
                0
            );

            if (proj.checkBounds(worldBounds)){
                iterator.remove();
                sendEvent(despawnProjectile);
                continue;
            }

            if (map != null && proj.checkCollides(map)) {
                iterator.remove();
                sendEvent(despawnProjectile);
                continue;
            }

            for (Player p : players) {
                if (p == null || p.id == proj.team || p.isSpectating()) continue;

                Circle forceFieldCircle = new Circle(p.getCenter(), ForceField.RADIUS);
                if (p.isInvincible && forceFieldCircle.contains(proj.getCenter())) {
                    sendEvent(despawnProjectile);
                    iterator.remove();
                    removed = true;
                    break;
                }

                if (p.getDamageArea().contains(proj.getCenter())) {
                    sendEvent(
                        new DamageEvent(
                            (byte)NetConstants.PLAYER_ENTITY_TYPE,
                            (byte)proj.team,
                            p.id,
                            proj.damageAmount,
                            p.getHealth()
                        )
                    );

                    if (p.damage(proj.damageAmount)) {
                        Player shooter = players[proj.team];
                        float lvlDiff = p.getLevel() - shooter.getLevel();
                        shooter.gainExperience(Math.max(6 * lvlDiff + 12, 12));
                    }

                    sendEvent(despawnProjectile);
                    iterator.remove();
                    removed = true;
                    break;
                }
            }

            if (removed) continue;
            for (int i = 0; i < bases.length; i++) {
                PlayerBase b = bases[i];
                if (b == null || i == proj.team || b.isDestroyed()) continue;
                if (b.getDamageArea().contains(proj.getCenter())) {
                    sendEvent(
                        new DamageEvent(
                            (byte)NetConstants.BASE_ENTITY_TYPE,
                            (byte) proj.team,
                            i,
                            proj.damageAmount,
                            b.getHealth()
                        )
                    );

                    if (b.damage(proj.damageAmount)) {
                        players[proj.team].gainExperience(100);
                    }

                    sendEvent(despawnProjectile);
                    iterator.remove();
                    removed = true;
                    break;
                }
            }

            if (removed) continue;

            for (int i = 0; i < asteroids.length; i++) {
                Asteroid a = asteroids[i];
                if (a == null) continue;
                if (a.getDamageArea().contains(proj.getCenter())) {
                    sendEvent(
                        new DamageEvent(
                            (byte)NetConstants.ASTEROID_ENTITY_TYPE,
                            (byte) proj.team,
                            i,
                            proj.damageAmount,
                            a.getHealth()
                        )
                    );

                    if (a.damage(proj.damageAmount)) {
                        players[proj.team].gainExperience(a.xp);
                        a.randomizeAttributes();
                        a.randomizePosition();
                    }

                    sendEvent(despawnProjectile);
                    iterator.remove();
                    removed = true;
                    break;
                }
            }

            if (removed) continue;

            for (Minion[] ms : minions) {
                if (ms == null || ms == minions[proj.team]) continue;
                for (int i = 0; i < ms.length; i++) {
                    Minion m = ms[i];
                    if (m == null || m.isDead()) continue;
                    if (m.getDamageArea().contains(proj.getCenter())) {
                        sendEvent(
                            new DamageEvent(
                                (byte)NetConstants.MINION_ENTITY_TYPE,
                                (byte) proj.team,
                                i + (m.getTeam() * MAX_MINIONS),
                                proj.damageAmount,
                                m.getHealth()
                            )
                        );

                        if (m.damage(proj.damageAmount)) {
                            players[proj.team].gainExperience(5);
                        }

                        sendEvent(despawnProjectile);
                        iterator.remove();
                        removed = true;
                        break;
                    }
                }
            }
        }
    }

    public int getBulletID() {
        nextBulletId++;
        return nextBulletId;
    }

    public void updateAsteroids(float delta, Rectangle worldBounds) {
        for (Asteroid a : asteroids) {
            a.update(delta);
            a.bounceOffBounds(worldBounds);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        am.dispose();

        if (server != null) server.stop();
        if (client != null) {
            sendEvent(new DisconnectEvent(id));
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
            Player player = players[i];
            if (i == id || player == null) continue;
            player.updateRemotePlayer(delta);
        }
    }

    public void sendEvent(NetEvent e) {
        if (server != null) {
            server.broadcastEvent(e);
        } else if (client != null) {
            client.sendEvent(e);
        }
    }

    public Minion getNextMinion(int team) {
        Minion m = null;
        int i;
        for (i = 0; i < minions[team].length; i++) {
            m = minions[team][i];
            if (m == null || m.isDead()) {
                break;
            }
        }
        if (m == null && i < MAX_MINIONS) {
            m = new Minion(this, (byte)team, (byte)i);
            minions[team][i] = m;
        }
        return m;
    }

    public void updateMinions(float delta, TiledMap map, Rectangle worldBounds) {
        if (server != null) {
            for (int team = 0; team < MAX_PLAYERS; team++) {
                for (int i = 0; i < MAX_MINIONS; i++) {
                    Minion m = minions[team][i];
                    if (m == null || m.isDead()) continue;
                    m.update(delta);
                    m.collide(map);

                    if (m.checkBounds(worldBounds)) {
                        m.destroy();
                        break;
                    }

                    for (PlayerBase b : bases) {
                        if (b == null) continue;
                        Circle c = (Circle)b.getDamageArea();
                        c.radius *= 1.25f;
                        m.collide(c);
                    }

                    for (Asteroid a : asteroids) {
                        Circle c = (Circle)a.getDamageArea();
                        c.radius *= 1.25f;
                        m.collide(c);
                    }
                }
            }
            if (1 / numOfPosSends < minionPosTimer) {
                sendMinionsPos();
                minionPosTimer -= 1 / numOfPosSends;
            }
        } else {
            updateRemoteMinions(delta);
        }
    }

    public void sendMinionsPos() {
        for (int team = 0; team < MAX_PLAYERS; team++) {
            for (int i = 0; i < MAX_MINIONS; i++) {
                Minion minion = minions[team][i];
                if (minion == null || minion.isDead()) continue;
                minion.sendMinionMoveEvent((byte) team, (byte) i);
            }
        }
    }

    public void updateRemoteMinions(float delta) {
        for (int team = 0; team < MAX_PLAYERS; team++) {
            for (int i = 0; i < MAX_MINIONS; i++) {
                Minion minion = minions[team][i];
                if (minion == null) continue;
                minion.updateRemoteMinion(delta);
            }
        }
    }

    public void updateBombs(float delta) {
        Iterator<Bomb> iterator = bombs.iterator();
        while (iterator.hasNext()) {
            Bomb b = iterator.next();
            if (!b.hasDetonated) {
                b.update(delta);
            } else {
                iterator.remove();
            }
        }
    }
}
