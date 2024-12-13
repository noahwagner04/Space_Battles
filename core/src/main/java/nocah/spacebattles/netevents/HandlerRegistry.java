package nocah.spacebattles.netevents;

import com.badlogic.gdx.math.Vector2;
import nocah.spacebattles.*;

import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class HandlerRegistry {
    private static final Map<Integer, Consumer<NetEvent>> clientMap = new HashMap<>();
    private static final Map<Integer, Consumer<NetEvent>> serverMap = new HashMap<>();
    private SpaceBattles game;

    public HandlerRegistry(SpaceBattles game) {
        this.game = game;

        clientMap.put(NetConstants.CHAT_EVENT_ID, (event) -> {
            ChatEvent e = (ChatEvent) event;
            System.out.println(e.sender + ": " + e.message);
        });
        serverMap.put(NetConstants.CHAT_EVENT_ID, (event) -> {
            handleClientEvent(event);
            game.server.broadcastEvent(event);
        });

        clientMap.put(NetConstants.CONNECTED_EVENT_ID, (event) -> {
            ConnectedEvent e = (ConnectedEvent) event;
            game.connected = true;
            game.id = e.playerID;
            game.seed = e.seed;
        });

        clientMap.put(NetConstants.START_GAME_EVENT_ID, (event) -> {
            game.gameStarted = true;
        });

        clientMap.put(NetConstants.SPAWN_PLAYER_EVENT_ID, (event) -> {
            SpawnEvent e = (SpawnEvent) event;
            game.players[e.playerID] = new Player(game, e.playerID);
        });
        serverMap.put(NetConstants.SPAWN_PLAYER_EVENT_ID, (event) -> {
            handleClientEvent(event);
            SpawnEvent e = (SpawnEvent) event;
            game.server.broadcastExcept(event, e.playerID);
            for (Player player: game.players) {
                if (player != null) game.server.sendEvent(new SpawnEvent(player.id), e.playerID);
            }
        });

        clientMap.put(NetConstants.MOVE_PLAYER_EVENT_ID, (event) -> {
            MoveEvent e = (MoveEvent) event;
            Player player = game.players[e.playerID];
            if (e.minionID == -1) {
                if (player != null) {
                    player.setX(e.x);
                    player.setY(e.y);
                    player.setRotation(e.rotation);
                    player.velocity.x = e.xVel;
                    player.velocity.y = e.yVel;
                    player.rotVelocity = e.rotVel;
                    player.thrustAnimationState = e.thrustAnimationState;

                }
            } else {
                if (game.minions[e.playerID][e.minionID] == null) {
                    game.minions[e.playerID][e.minionID] = new Minion(game, e.playerID, e.minionID);
                    game.minions[e.playerID][e.minionID].setStats(game.bases[e.playerID].minionLevel);
                }

                Minion minion = game.minions[e.playerID][e.minionID];

                if (minion.isDead()) {
                    minion.revive();
                }
                minion.setX(e.x);
                minion.setY(e.y);
                minion.velocity.x = e.xVel;
                minion.velocity.y = e.yVel;
            }

        });
        serverMap.put(NetConstants.MOVE_PLAYER_EVENT_ID, (event) -> {
            handleClientEvent(event);
            MoveEvent e = (MoveEvent) event;
            game.server.broadcastExcept(event, e.playerID);
        });

        // keep here in the case we want to do something when another client leaves
        clientMap.put(NetConstants.DISCONNECT_EVENT_ID, (event) -> {
            DisconnectEvent e = (DisconnectEvent) event;
        });
        serverMap.put(NetConstants.DISCONNECT_EVENT_ID, (event) -> {
            handleClientEvent(event);
            DisconnectEvent e = (DisconnectEvent) event;
            game.server.broadcastExcept(event, e.playerID);
            game.server.clientSockets[e.playerID - 1] = null;
        });

        clientMap.put(NetConstants.SHOOT_EVENT_ID, (event) -> {
            ShootEvent e = (ShootEvent) event;
            Player p = game.players[e.playerID];
            Vector2 pPos = p.getCenter();
            if (e.minionID == -1 && e.bombID == -1) {
                p.fireBullet(e.bulletID);
                p.playShoot();
            } else if (e.minionID != -1) {
                Minion m = game.minions[e.playerID][e.minionID];
                Vector2 target = m.getCenter().add( new Vector2(1, 0).rotateRad(e.rotation));
                m.shootAt(target, e.bulletID);
                m.playShoot();
            } else if (e.bulletID == -1) {
                game.bombs.add(new Bomb(p, game, pPos.x, pPos.y, e.bombID));
            } else {
                Bomb b = null;
                for (Bomb bomb : game.bombs) {
                    if (bomb.id == e.bombID) {
                        b = bomb;
                        break;
                    }
                }
                if (b == null) return;
                Vector2 startPos = new Vector2(b.getX() + b.getOriginX(), b.getY() + b.getOriginY());
                Projectile proj = new Projectile(game, e.bulletID, game.getEntity(SpaceBattles.RSC_SQUARE_IMG), startPos.x, startPos.y, b.bulletSpeed, e.rotation);
                proj.setSize(0.15f, 0.15f);
                proj.setOriginCenter();
                proj.translate(-proj.getOriginX(), -proj.getOriginY());
                proj.setColor(SpaceBattles.PLAYER_COLORS[e.playerID]);
                game.projectiles.add(proj);
            }
        });
        serverMap.put(NetConstants.SHOOT_EVENT_ID, (event) -> {
            ShootEvent e = (ShootEvent) event;

            int bulletID  = game.getBulletID();
            game.players[e.playerID].fireBullet(bulletID);
            game.players[e.playerID].playShoot();
            game.server.broadcastEvent(new ShootEvent(e.playerID, (byte) -1, (byte) -1, bulletID, 0));
        });

        // clients handle collision damage, server handles projectiles
        // clients have the most accurate information regarding collisions
        // to keep consistency for bullets across clients, the server handles these
        clientMap.put(NetConstants.DAMAGE_EVENT_ID, (event) -> {
            DamageEvent e = (DamageEvent) event;
            switch(e.entityType) {
                case NetConstants.PLAYER_ENTITY_TYPE:
                    Player p = game.players[e.entityId];

                    if (p.damage(e.damageAmount)) {
                        if (e.damagerID == e.entityId) return;
                        Player shooter = game.players[e.damagerID];
                        float lvlDiff = p.getLevel() - shooter.getLevel();
                        shooter.gainExperience(Math.max(6 * lvlDiff + 12, 12));
                    }
                    break;
                case NetConstants.PROJECTILE_ENTITY_TYPE:
                    // this essentially acts as a way to despawn the projectiles
                    Iterator<Projectile> iterator = game.projectiles.iterator();
                    while (iterator.hasNext()) {
                        Projectile proj = iterator.next();
                        if (proj.getID() == e.entityId) {
                            proj.playHit();
                            iterator.remove();
                        }
                    }
                    break;
                case NetConstants.ASTEROID_ENTITY_TYPE:
                    Asteroid a = game.asteroids[e.entityId];
                    if (a.damage(e.damageAmount)) {
                        game.players[e.damagerID].gainExperience(a.xp);
                        a.playDestroy();
                        a.randomizeAttributes();
                        a.randomizePosition();
                    }
                    break;
                case NetConstants.BASE_ENTITY_TYPE:
                    if(game.bases[e.entityId].damage(e.damageAmount)) {
                        game.players[e.damagerID].gainExperience(100);
                    }
                    break;
                case NetConstants.MINION_ENTITY_TYPE:
                    byte team = (byte)(e.entityId / SpaceBattles.MAX_MINIONS);
                    byte minion = (byte)(e.entityId % SpaceBattles.MAX_MINIONS);
                    if(game.minions[team][minion].damage(e.damageAmount)) {
                        game.players[e.damagerID].gainExperience(5);
                    }
                    break;
                case NetConstants.BOMB_ENTITY_TYPE:
                    // this essentially acts as a way to despawn the bomb
                    Iterator<Bomb> itr = game.bombs.iterator();
                    while (itr.hasNext()) {
                        Bomb b = itr.next();
                        if (b.id == e.entityId) {
                            b.playExplode();
                            itr.remove();
                        }
                    }
                    break;
            }
        });
        serverMap.put(NetConstants.DAMAGE_EVENT_ID, (event) -> {
            handleClientEvent(event);
            DamageEvent e = (DamageEvent) event;
            game.server.broadcastExcept(e, e.entityId);
        });

        clientMap.put(NetConstants.UPGRADE_EVENT_ID, (event) -> {
            UpgradeEvent e = (UpgradeEvent) event;
            game.players[e.playerID].upgradeStat(e.upgradeID, false);
        });
        serverMap.put(NetConstants.UPGRADE_EVENT_ID, (event) -> {
            handleClientEvent(event);
            UpgradeEvent e = (UpgradeEvent) event;
            game.server.broadcastExcept(event, e.playerID);
        });

        clientMap.put(NetConstants.ABILITY_EVENT_ID, (event) -> {
            AbilityEvent e = (AbilityEvent) event;
            Player p = game.players[e.playerID];

            // server should be the only one to handle bomb ability
            if (game.server == null && e.abilityID == Ability.BOMB) return;

            if (e.toggle == 1) {
                if (e.abilityNum == 1) {
                    if (p.ability1 == null || p.ability1.abilityID != e.abilityID) p.setAbility(e.abilityNum, e.abilityID);
                    p.ability1.onActivate();
                } else if (e.abilityNum == 2) {
                    if (p.ability2 == null || p.ability2.abilityID != e.abilityID) p.setAbility(e.abilityNum, e.abilityID);
                    p.ability2.onActivate();
                }
            } else if (e.toggle == 0) {
                if (e.abilityNum == 1) {
                    if (p.ability1 == null) return;
                    p.ability1.onDeactivate();
                } else if (e.abilityNum == 2) {
                    if (p.ability2 == null) return;
                    p.ability2.onDeactivate();
                }
            }
        });
        serverMap.put(NetConstants.ABILITY_EVENT_ID, (event) -> {
            handleClientEvent(event);
            AbilityEvent e = (AbilityEvent) event;
            game.server.broadcastExcept(event, e.playerID);
        });
    }

    public void handleServerEvent(NetEvent event) {
        Consumer<NetEvent> handler = serverMap.get((int)event.getEventID());
        if (handler != null) {
            handler.accept(event);
        } else {
            System.err.println("No handler registered for event ID: " + event.getEventID());
        }
    }

    public void handleClientEvent(NetEvent event) {
        Consumer<NetEvent> handler = clientMap.get((int)event.getEventID());
        if (handler != null) {
            handler.accept(event);
        } else {
            System.err.println("No handler registered for event ID: " + event.getEventID());
        }
    }
}
