package nocah.spacebattles.netevents;

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
        });

        clientMap.put(NetConstants.START_GAME_EVENT_ID, (event) -> {
            game.gameStarted = true;
        });
        // this is never called for main client
        serverMap.put(NetConstants.START_GAME_EVENT_ID, (event) -> {
            handleClientEvent(event);
            game.server.broadcastEvent(event);
            game.server.stopListening();
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
                    game.minions[e.playerID][e.minionID] = new Minion(game, e.playerID);
                }
                Minion minion = game.minions[e.playerID][e.minionID];
                minion.setX(e.x);
                minion.setY(e.y);
                minion.setRotation(e.rotation);
                minion.velocity.x = e.xVel;
                minion.velocity.y = e.yVel;
            }

        });
        serverMap.put(NetConstants.MOVE_PLAYER_EVENT_ID, (event) -> {
            handleClientEvent(event);
            MoveEvent e = (MoveEvent) event;
            game.server.broadcastExcept(event, e.playerID);
        });

        // this can cause bugs, not the best way to do it
        clientMap.put(NetConstants.DISCONNECT_EVENT_ID, (event) -> {
            DisconnectEvent e = (DisconnectEvent) event;
            game.players[e.playerID] = null;
        });
        serverMap.put(NetConstants.DISCONNECT_EVENT_ID, (event) -> {
            handleClientEvent(event);
            DisconnectEvent e = (DisconnectEvent) event;
            game.server.broadcastExcept(event, e.playerID);
            game.server.clientSockets[e.playerID - 1] = null;
        });

        clientMap.put(NetConstants.SHOOT_EVENT_ID, (event) -> {
            ShootEvent e = (ShootEvent) event;
            game.players[e.playerID].fireBullet(e.bulletID);
        });
        serverMap.put(NetConstants.SHOOT_EVENT_ID, (event) -> {
            ShootEvent e = (ShootEvent) event;
            int bulletID  = game.getBulletID();
            game.players[e.playerID].fireBullet(bulletID);
            game.server.broadcastEvent(new ShootEvent(e.playerID, bulletID));
        });

        // clients handle collision damage, server handles projectiles
        // clients have the most accurate information regarding collisions
        // to keep consistency for bullets across clients, the server handles these
        clientMap.put(NetConstants.DAMAGE_EVENT_ID, (event) -> {
            DamageEvent e = (DamageEvent) event;
            switch(e.entityType) {
                case NetConstants.PLAYER_ENTITY_TYPE:
                    game.players[e.entityId].damage(e.damageAmount);
                    break;
                case NetConstants.PROJECTILE_ENTITY_TYPE:
                    // this essentially acts as a way to despawn the projectiles
                    Iterator<Projectile> iterator = game.projectiles.iterator();
                    while (iterator.hasNext()) {
                        Projectile proj = iterator.next();
                        if (proj.getID() == e.entityId) {
                            iterator.remove();
                        }
                    }
                    break;
                case NetConstants.ASTEROID_ENTITY_TYPE:
                    game.asteroids[e.entityId].damage(e.damageAmount);
                    break;
                case NetConstants.BASE_ENTITY_TYPE:
                    game.bases[e.entityId].damage(e.damageAmount);
                    break;
            }
        });
        serverMap.put(NetConstants.DAMAGE_EVENT_ID, (event) -> {
            handleClientEvent(event);
            DamageEvent e = (DamageEvent) event;
            game.server.broadcastExcept(e, e.entityId);
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
