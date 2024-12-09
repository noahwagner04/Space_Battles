package nocah.spacebattles.netevents;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DeserializerRegistry {
    private static final Map<Integer, Function<byte[], NetEvent>> deserializeMap = new HashMap<>();

    static {
        deserializeMap.put(NetConstants.CHAT_EVENT_ID, ChatEvent::deserialize);
        deserializeMap.put(NetConstants.CONNECTED_EVENT_ID, ConnectedEvent::deserialize);
        deserializeMap.put(NetConstants.START_GAME_EVENT_ID, StartGameEvent::deserialize);
        deserializeMap.put(NetConstants.SPAWN_PLAYER_EVENT_ID, SpawnEvent::deserialize);
        deserializeMap.put(NetConstants.MOVE_PLAYER_EVENT_ID, MoveEvent::deserialize);
        deserializeMap.put(NetConstants.DISCONNECT_EVENT_ID, DisconnectEvent::deserialize);
        deserializeMap.put(NetConstants.SHOOT_EVENT_ID, ShootEvent::deserialize);
        deserializeMap.put(NetConstants.DAMAGE_EVENT_ID, DamageEvent::deserialize);
        deserializeMap.put(NetConstants.UPGRADE_EVENT_ID, UpgradeEvent::deserialize);
        deserializeMap.put(NetConstants.ABILITY_EVENT_ID, AbilityEvent::deserialize);


        // at some point im going to add all net events here
    }

    public static NetEvent deserialize(byte eventID, byte[] data) {
        Function<byte[], NetEvent> deserializer = deserializeMap.get((int)eventID);
        if (deserializer != null) {
            return deserializer.apply(data);
        }
        throw new IllegalArgumentException("No deserializer registered for event ID: " + eventID);
    }
}
