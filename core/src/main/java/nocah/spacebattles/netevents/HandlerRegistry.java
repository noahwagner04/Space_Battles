package nocah.spacebattles.netevents;

import com.badlogic.gdx.Game;
import nocah.spacebattles.Player;
import nocah.spacebattles.SpaceBattles;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
        });

        clientMap.put(NetConstants.SPAWN_PLAYER_EVENT_ID, (event) -> {
            SpawnEvent e = (SpawnEvent) event;
            game.players[e.playerID] = new Player(game);
        });
        serverMap.put(NetConstants.SPAWN_PLAYER_EVENT_ID, (event) -> {
            SpawnEvent e = (SpawnEvent) event;
            game.server.broadcastExcept(event, e.playerID);
            for (int i = 0; i < game.server.clientSockets.length; i++) {
                Socket client = game.server.clientSockets[i];
                if (client != null) game.server.sendEvent(new SpawnEvent(i), e.playerID);
            }
            game.players[e.playerID] = new Player(game);
        });

        clientMap.put(NetConstants.MOVE_PLAYER_EVENT_ID, (event) -> {
            MoveEvent e = (MoveEvent) event;
            if(game.players[e.playerID] != null) {
                game.players[e.playerID].setX(e.x);
                game.players[e.playerID].setY(e.y);
                game.players[e.playerID].setRotation(e.rotation);
            }
        });
        serverMap.put(NetConstants.MOVE_PLAYER_EVENT_ID, (event) -> {
            MoveEvent e = (MoveEvent) event;
            game.server.broadcastExcept(event, e.playerID);
        });

    }

    public void handleServerEvent(NetEvent event) {
        Consumer<NetEvent> handler = serverMap.get(event.getEventID());
        if (handler != null) {
            handler.accept(event);
        } else {
            System.out.println("No handler registered for event ID: " + event.getEventID());
        }
    }

    public void handleClientEvent(NetEvent event) {
        Consumer<NetEvent> handler = clientMap.get(event.getEventID());
        if (handler != null) {
            handler.accept(event);
        } else {
            System.out.println("No handler registered for event ID: " + event.getEventID());
        }
    }
}
