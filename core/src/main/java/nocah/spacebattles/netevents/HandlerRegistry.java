package nocah.spacebattles.netevents;

import com.badlogic.gdx.Game;
import nocah.spacebattles.SpaceBattles;

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
            ChatEvent c = (ChatEvent) event;
            System.out.println(c.sender + ": " + c.message);
        });

        serverMap.put(NetConstants.CHAT_EVENT_ID, (event) -> {
            game.server.broadcastEvent(event);
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
