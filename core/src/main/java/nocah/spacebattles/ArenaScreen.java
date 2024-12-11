package nocah.spacebattles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
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
    private Sound spaceSound;
    private Sound lose;
    private long loseID = -1;

    private float endMessageTimer = 0;
    private float endMessageInterval = 5;

    private boolean hasWon = false;

    private Stage stage;
    private StatUpgradeUI statUpgradeUI;
    private AbilityUnlockUI abilityUnlockUI;
    private XPBarUI xpBarUI;

    public ArenaScreen (SpaceBattles game) {
        this.game = game;
        SpaceBattles.random.setSeed(game.seed);
        thisPlayer = game.players[game.id];
        map = game.am.get(SpaceBattles.RSC_TILED_MAP);
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f/map.getProperties().get("tilewidth", Integer.class));
        worldBounds = new Rectangle(
            0,
            0,
            map.getProperties().get("width", Integer.class),
            map.getProperties().get("height", Integer.class)
        );

        spaceSound = game.am.get(SpaceBattles.RSC_SPACE_AMBIENT_SOUND, Sound.class);
        long soundId = spaceSound.loop();
        spaceSound.setVolume(soundId, 0.08f);

        lose = game.am.get(SpaceBattles.RSC_LOSE_SOUND, Sound.class);
        game.setBases(worldBounds);

        for (Player p: game.players) {
            if (p == null) continue;
            p.respawn();
        }

        Rectangle spawnArea = new Rectangle(5, 5, worldBounds.width - 5, worldBounds.height - 5);

        game.lobbyMusic.stop();

        for (int i = 0; i < game.asteroids.length; i++) {
            game.asteroids[i] = new Asteroid(game, spawnArea);
        }
        camera = new Camera(15, 15);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        statUpgradeUI = new StatUpgradeUI(game, stage);
        abilityUnlockUI = new AbilityUnlockUI(game, stage);
        xpBarUI = new XPBarUI(game, stage);

        hud = new HUD(new BitmapFont());

        hud.registerAction("upgrade", new HUDActionCommand() {
            static final String help = "upgrade <defence, attack, speed, base_defence, minions> <ability points>";

            @Override
            public String execute(String[] cmd) {
                try {
                    int amount = Integer.parseInt(cmd[2]);
                    if (cmd[1].contentEquals("defence")) {
                        for (int i = 0; i < amount; i++) {
                            game.players[game.id].upgradeStat(Player.DEFENCE, true);
                            game.sendEvent(new UpgradeEvent(game.id, Player.DEFENCE));
                        }
                    } else if (cmd[1].contentEquals("attack")) {
                        for (int i = 0; i < amount; i++) {
                            game.players[game.id].upgradeStat(Player.ATTACK, true);
                            game.sendEvent(new UpgradeEvent(game.id, Player.ATTACK));
                        }
                    } else if (cmd[1].contentEquals("speed")) {
                        for (int i = 0; i < amount; i++) {
                            game.players[game.id].upgradeStat(Player.SPEED, true);
                            game.sendEvent(new UpgradeEvent(game.id, Player.SPEED));
                        }
                    } else if (cmd[1].contentEquals("base_defence")) {
                        for (int i = 0; i < amount; i++) {
                            game.players[game.id].upgradeStat(Player.BASE_DEFENCE, true);
                            game.sendEvent(new UpgradeEvent(game.id, Player.BASE_DEFENCE));
                        }
                    } else if (cmd[1].contentEquals("minions")) {
                        for (int i = 0; i < amount; i++) {
                            game.players[game.id].upgradeStat(Player.MINIONS, true);
                            game.sendEvent(new UpgradeEvent(game.id, Player.MINIONS));
                        }
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

        hud.registerAction("unlock", new HUDActionCommand() {
            static final String help = "unlock <dash, rapid_fire, bomb, force_field, invisibility> < 1 or 2 for ability number>";

            @Override
            public String execute(String[] cmd) {
                Player p = game.players[game.id];

                try {
                    int abilityNum = Integer.parseInt(cmd[2]);
                    if (cmd[1].contentEquals("dash") && !(p.ability1 instanceof Dash)) {
                        if (abilityNum == 1) p.ability1 = new Dash(p, game, (byte)1);
                        else if (abilityNum == 2) p.ability2 = new Dash(p, game, (byte) 2);
                    } else if (cmd[1].contentEquals("rapid_fire") && !(p.ability1 instanceof RapidFire)) {
                        if (abilityNum == 1) p.ability1 = new RapidFire(p, game, (byte) 1);
                        else if (abilityNum == 2) p.ability2 = new RapidFire(p, game, (byte) 2);
                    } else if (cmd[1].contentEquals("bomb") && !(p.ability1 instanceof BombDeploy)) {
                        if (abilityNum == 1) p.ability1 = new BombDeploy(p, game, (byte) 1);
                        else if (abilityNum == 2) p.ability2 = new BombDeploy(p, game, (byte) 1);
                    } else if (cmd[1].contentEquals("force_field") && !(p.ability1 instanceof ForceField)) {
                        if (abilityNum == 1) p.ability1 = new ForceField(p, game, (byte) 1);
                        else if (abilityNum == 2) p.ability2 = new ForceField(p, game, (byte) 2);
                    } else if (cmd[1].contentEquals("invisibility") && !(p.ability1 instanceof Invisibility)) {
                        if (abilityNum == 1) p.ability1 = new Invisibility(p, game, (byte) 1);
                        else if (abilityNum == 2) p.ability2 = new Invisibility(p, game, (byte) 2);
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

        game.startWorldDraw();
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        game.batch.draw(game.am.get(SpaceBattles.RSC_STARS1_IMG, Texture.class), 0, 0, 800, 800);
        Texture s2 = game.am.get(SpaceBattles.RSC_STARS2_IMG);
        Texture s3 = game.am.get(SpaceBattles.RSC_STARS3_IMG);
        Vector2 pos2 = camera.getPosition().scl(0.005f);
        Vector2 pos3 = camera.getPosition().scl(0.01f);

        s2.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        game.batch.draw(s2, 0, 0, 800, 800, pos2.x, pos2.y, pos2.x + 1, pos2.y + 1);

        s3.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        game.batch.draw(s3, 0, 0, 800, 800, pos3.x, pos3.y, pos3.x + 1, pos3.y + 1);
        game.endWorldDraw();

        game.startWorldDraw(camera.getProjMat());
        mapRenderer.setView(camera.getOrthCamera());
        mapRenderer.render();
        game.drawSprites(game.projectiles);
        game.drawSprites(game.bases);
        for(int i = 0; i < SpaceBattles.MAX_PLAYERS; i++) {
            game.drawSprites(game.minions[i]);
        }
        game.drawSprites(game.bombs);
        game.drawSprites(game.players);
        game.drawSprites(game.asteroids);
        game.endWorldDraw();

        game.batch.begin();
        hud.draw(game.batch);

        if (checkWin()) {
            game.batch.draw(game.am.get(SpaceBattles.RSC_YOU_WIN_IMG, Texture.class), 0, 0);
            if (!game.winMusic.isPlaying())  {
                game.winMusic.play();
                game.winMusic.setVolume(0.2f);
            }
        } else if (checkLose()) {
            game.batch.draw(game.am.get(SpaceBattles.RSC_YOU_LOSE_IMG, Texture.class), 0, 0);
            if (loseID == -1) {
                loseID = lose.play();
                lose.setVolume(loseID, 5f);
            }
        }
        game.batch.end();

        statUpgradeUI.update();
        abilityUnlockUI.update();
        xpBarUI.update();

        stage.act(delta);
        stage.draw();
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

    @Override
    public void dispose() {
        stage.dispose();
    }
}
