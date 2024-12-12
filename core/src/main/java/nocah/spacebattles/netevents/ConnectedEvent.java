package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ConnectedEvent implements NetEvent{
    public final byte playerID;
    public final int seed;

    public ConnectedEvent(byte playerID, int seed) {
        this.playerID = playerID;
        this.seed = seed;
    }

    public byte getEventID() {return NetConstants.CONNECTED_EVENT_ID;}
    public short getDataByteSize() {return 1 + 4;}

    public byte[] serialize() {

        // EventID, Event size, playerID
        int totalSize = 1 + 2 + getDataByteSize();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.put(getEventID());
        buffer.putShort(getDataByteSize());
        buffer.put(playerID);
        buffer.putInt(seed);

        return buffer.array();
    }

    public static ConnectedEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte playerID = buffer.get();
        int seed = buffer.getInt();

        return new ConnectedEvent(playerID, seed);
    }
}
