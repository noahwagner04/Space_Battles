package nocah.spacebattles.netevents;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SerializerRegistry {
    private static final Map<Integer, Function<NetEvent, byte[]>> serializeMap = new HashMap<>();
    private static final Map<Integer, Function<byte[], NetEvent>> deserializeMap = new HashMap<>();

    static {
        serializeMap.put(NetConstants.CHAT_EVENT_ID, ChatEvent::serialize);
        deserializeMap.put(NetConstants.CHAT_EVENT_ID, ChatEvent::deserialize);

        serializeMap.put(NetConstants.CONNECTED_EVENT_ID, ConnectedEvent::serialize);
        deserializeMap.put(NetConstants.CONNECTED_EVENT_ID, ConnectedEvent::deserialize);

        serializeMap.put(NetConstants.START_GAME_EVENT_ID, StartGameEvent::serialize);
        deserializeMap.put(NetConstants.START_GAME_EVENT_ID, StartGameEvent::deserialize);

        serializeMap.put(NetConstants.SPAWN_PLAYER_EVENT_ID, SpawnEvent::serialize);
        deserializeMap.put(NetConstants.SPAWN_PLAYER_EVENT_ID, SpawnEvent::deserialize);

        serializeMap.put(NetConstants.MOVE_PLAYER_EVENT_ID, MoveEvent::serialize);
        deserializeMap.put(NetConstants.MOVE_PLAYER_EVENT_ID, MoveEvent::deserialize);

        // at some point im going to add all net events here
    }

    public static byte[] serialize(int eventID, NetEvent e) {
        Function<NetEvent, byte[]> serializer = serializeMap.get(eventID);
        if (serializer != null) {
            return serializer.apply(e);
        }
        throw new IllegalArgumentException("No serializer registered for event ID: " + eventID);
    }

    public static NetEvent deserialize(int eventID, byte[] data) {
        Function<byte[], NetEvent> deserializer = deserializeMap.get(eventID);
        if (deserializer != null) {
            return deserializer.apply(data);
        }
        throw new IllegalArgumentException("No deserializer registered for event ID: " + eventID);
    }
}
