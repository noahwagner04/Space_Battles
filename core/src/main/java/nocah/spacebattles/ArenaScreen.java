package nocah.spacebattles;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import nocah.spacebattles.netevents.ChatEvent;
import nocah.spacebattles.netevents.SpawnEvent;
import nocah.spacebattles.netevents.StartGameEvent;
import nocah.spacebattles.netevents.UpgradeEvent;

public class ArenaScreen extends ScreenAdapter {
    private SpaceBattles game;
    private Player thisPlayer;
    private Camera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Rectangle worldBounds;
    public HUD hud;


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
        hud = new HUD(new BitmapFont());

        game.setBases(worldBounds);
        thisPlayer.respawn();

        for (int i = 0; i < game.asteroids.length; i++) {
            game.asteroids[i] = new Asteroid(game, worldBounds);
        }
        camera = new Camera(15, 15);

        hud.registerAction("upgrade", new HUDActionCommand() {
            static final String help = "upgrade <defence, attack, speed, base_defence, minions>";

            @Override
            public String execute(String[] cmd) {
                if (game.players[game.id].getStatPoints() == 0) {
                    return "No stat points to spend!";
                }
                try {
                    if (cmd[1].contentEquals("defence")) {
                        game.players[game.id].upgradeStat(Player.DEFENCE);
                        game.sendEvent(new UpgradeEvent(game.id, Player.DEFENCE));
                    } else if (cmd[1].contentEquals("attack")) {
                        game.players[game.id].upgradeStat(Player.ATTACK);
                        game.sendEvent(new UpgradeEvent(game.id, Player.ATTACK));
                    } else if (cmd[1].contentEquals("speed")) {
                        game.players[game.id].upgradeStat(Player.SPEED);
                        game.sendEvent(new UpgradeEvent(game.id, Player.SPEED));
                    } else if (cmd[1].contentEquals("base_defence")) {
                        game.players[game.id].upgradeStat(Player.BASE_DEFENCE);
                        game.sendEvent(new UpgradeEvent(game.id, Player.BASE_DEFENCE));
                    } else if (cmd[1].contentEquals("minions")) {
                        game.players[game.id].upgradeStat(Player.MINIONS);
                        game.sendEvent(new UpgradeEvent(game.id, Player.MINIONS));
                    } else {
                        return help;
                    }
                } catch (Exception e) {
                    return help;
                }
                return "ok! (" + game.players[game.id].getStatPoints() + " stat points remaining)";
            }

            public String help(String[] cmd) {
                return help;
            }
        });

        hud.registerAction("unlock", new HUDActionCommand() {
            static final String help = "unlock <dash, rapid_fire, bomb, force_field, invisibility> (no repeats!)";

            @Override
            public String execute(String[] cmd) {
                Player p = game.players[game.id];
                if (!p.unlockAbility1) {
                    return "Too low level! (next ability unlock at level 5)";
                }

                if (p.ability1 != null && !p.unlockAbility2) {
                    return "Too low level! (next ability unlock at level 10)";
                }

                if (p.ability2 != null) {
                    return "Max number of abilities is unlocked!";
                }

                try {
                    if (cmd[1].contentEquals("dash") && !(p.ability1 instanceof Dash)) {
                        if (p.ability1 == null) p.ability1 = new Dash(p, game);
                        else p.ability2 = new Dash(p, game);
                    } else if (cmd[1].contentEquals("rapid_fire") && !(p.ability1 instanceof RapidFire)) {
                        if (p.ability1 == null) p.ability1 = new RapidFire(p, game);
                        else p.ability2 = new RapidFire(p, game);
                    } else if (cmd[1].contentEquals("bomb") && !(p.ability1 instanceof BombDeploy)) {
                        if (p.ability1 == null) p.ability1 = new BombDeploy(p, game);
                        else p.ability2 = new BombDeploy(p, game);
                    } else if (cmd[1].contentEquals("force_field") && !(p.ability1 instanceof ForceField)) {
                        if (p.ability1 == null) p.ability1 = new ForceField(p, game);
                        else p.ability2 = new ForceField(p, game);
                    } else if (cmd[1].contentEquals("invisibility") && !(p.ability1 instanceof Invisibility)) {
                        if (p.ability1 == null) p.ability1 = new Invisibility(p, game);
                        else p.ability2 = new Invisibility(p, game);
                    } else {
                        return help;
                    }
                } catch (Exception e) {
                    return help;
                }
                return "ok!";
            }

            public String help(String[] cmd) {
                return help;
            }
        });
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
        game.updateMinions(delta, map, worldBounds);
        game.updateBombs(delta);
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
        game.drawSprites(game.bases);
        for(int i = 0; i < SpaceBattles.MAX_PLAYERS; i++) {
            game.drawSprites(game.minions[i]);
        }
        game.drawSprites(game.bombs);
        game.drawSprites(game.asteroids);
        game.endWorldDraw();

        game.batch.begin();
        hud.draw(game.batch);
        game.batch.end();
    }
}
