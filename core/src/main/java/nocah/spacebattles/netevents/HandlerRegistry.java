package nocah.spacebattles.netevents;

import nocah.spacebattles.Asteroid;
import nocah.spacebattles.Player;
import nocah.spacebattles.SpaceBattles;

import java.net.Socket;
import java.util.HashMap;
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
        serverMap.put(NetConstants.START_GAME_EVENT_ID, (event) -> {
            game.server.broadcastEvent(event);
            game.server.stopListening();
        });

        clientMap.put(NetConstants.SPAWN_PLAYER_EVENT_ID, (event) -> {
            SpawnEvent e = (SpawnEvent) event;
            game.players[e.playerID] = new Player(game, e.playerID);
        });
        serverMap.put(NetConstants.SPAWN_PLAYER_EVENT_ID, (event) -> {
            SpawnEvent e = (SpawnEvent) event;
            game.server.broadcastExcept(event, e.playerID);
            for (Player player: game.players) {
                if (player != null) game.server.sendEvent(new SpawnEvent(player.id), e.playerID);
            }
        });

        clientMap.put(NetConstants.MOVE_PLAYER_EVENT_ID, (event) -> {
            MoveEvent e = (MoveEvent) event;
            if(game.players[e.playerID] != null) {
                game.players[e.playerID].setX(e.x);
                game.players[e.playerID].setY(e.y);
                game.players[e.playerID].setRotation(e.rotation);
                game.players[e.playerID].velocity.x = e.xVel;
                game.players[e.playerID].velocity.y = e.yVel;
                game.players[e.playerID].rotVelocity = e.rotVel;
                game.players[e.playerID].thrustAnimationState = e.thrustAnimationState;

            }
        });
        serverMap.put(NetConstants.MOVE_PLAYER_EVENT_ID, (event) -> {
            MoveEvent e = (MoveEvent) event;
            game.server.broadcastExcept(event, e.playerID);
        });

        clientMap.put(NetConstants.DISCONNECT_EVENT_ID, (event) -> {
            DisconnectEvent e = (DisconnectEvent) event;
            game.players[e.playerID] = null;
        });
        serverMap.put(NetConstants.DISCONNECT_EVENT_ID, (event) -> {
            DisconnectEvent e = (DisconnectEvent) event;
            game.server.broadcastExcept(event, e.playerID);
            game.server.clientSockets[e.playerID - 1] = null;
        });

        clientMap.put(NetConstants.SHOOT_EVENT_ID, (event) -> {
            ShootEvent e = (ShootEvent) event;
            game.players[e.playerID].fireBullet();
        });
        serverMap.put(NetConstants.SHOOT_EVENT_ID, (event) -> {
            ShootEvent e = (ShootEvent) event;
            game.server.broadcastExcept(event, e.playerID);
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
                    game.projectiles.remove(e.entityId);
                    break;
                case NetConstants.ASTEROID_ENTITY_TYPE:
                    game.asteroids.get(e.entityId).damage(e.damageAmount);
                    break;
            }
        });
        serverMap.put(NetConstants.DAMAGE_EVENT_ID, (event) -> {
            DamageEvent e = (DamageEvent) event;
            game.server.broadcastExcept(e, e.entityId);
        });
    }

    public void handleServerEvent(NetEvent event) {
        Consumer<NetEvent> handler = serverMap.get((int)event.getEventID());
        //handle event for this server client
        handleClientEvent(event);
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
