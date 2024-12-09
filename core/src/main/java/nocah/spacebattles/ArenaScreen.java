package nocah.spacebattles;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import nocah.spacebattles.netevents.UpgradeEvent;

public class ArenaScreen extends ScreenAdapter {
    private SpaceBattles game;
    private Player thisPlayer;
    private Camera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Rectangle worldBounds;
    public HUD hud;

    private float endMessageTimer = 0;
    private float endMessageInterval = 5;

    private boolean hasWon = false;

    private Stage stage;

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
//                if (!p.unlockAbility1) {
//                    return "Too low level! (next ability unlock at level 5)";
//                }
//
//                if (p.ability1 != null && !p.unlockAbility2) {
//                    return "Too low level! (next ability unlock at level 10)";
//                }
//
//                if (p.ability2 != null) {
//                    return "Max number of abilities is unlocked!";
//                }

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
        stage = new Stage(new ScreenViewport());
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

        if (checkGameOver()) {
            if (endMessageTimer > endMessageInterval) {
                game.setScreen(new EndScreen(game, checkWin()));
                return;
            }
            endMessageTimer += delta;
        }
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

        // NOTE: eventually replace with win / lose messages
        if (checkWin()) {
            game.batch.setColor(0, 1, 0, 1);
        } else if (checkLose()) {
            game.batch.setColor(1, 0, 0, 1);
        }
        game.batch.end();
    }

    private boolean checkLose() {
        return game.players[game.id].isSpectating() && game.bases[game.id].isDestroyed();
    }

    private boolean checkWin() {
        if (hasWon) return true;

        float null_bases = 0;
        for (PlayerBase b : game.bases) {
            if (b == null) {
                null_bases++;
                continue;
            }
            if (b == game.bases[game.id]) continue;

            if (!b.isDestroyed()) return false;
        }

        // No win condition if single player, nice for debuging
        if (null_bases == SpaceBattles.MAX_PLAYERS - 1) return false;

        for (Player p : game.players) {
            if (p == null || p == game.players[game.id]) continue;
            if (!p.isSpectating()) return false;
        }

        hasWon = !checkLose();
        return hasWon;
    }

    private boolean checkGameOver() {
        float alive_players = 0;
        float valid_players = 0;

        for (Player p : game.players) {
            if (p == null) continue;
            valid_players++;
            if (!p.isSpectating()) alive_players++;
        }

        // no game over for single player, good for debuging
        if (valid_players == 1) return false;

        if (alive_players > 1) return false;

        float alive_bases = 0;
        for (Player p : game.players) {
            if (p == null) continue;
            if (!game.bases[p.id].isDestroyed()) alive_bases++;
        }

        if (alive_bases > 1) return false;

        if (alive_bases == 1 && alive_players == 1) {
            for (Player p : game.players) {
                if (p == null || p.isSpectating()) continue;
                if (game.bases[p.id].isDestroyed()) return false;
            }
        }

        return true;
    }
}
