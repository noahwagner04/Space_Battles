package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DisconnectEvent implements NetEvent{
    public final byte playerID;

    public DisconnectEvent(byte playerID) {
        this.playerID = playerID;
    }

    public byte getEventID() {return NetConstants.DISCONNECT_EVENT_ID;}
    public short getDataByteSize() {return 1;}

    public byte[] serialize() {

        // EventID, Event size, playerID
        int totalSize = 1 + 2 + getDataByteSize();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.put(getEventID());
        buffer.putShort(getDataByteSize());
        buffer.put(playerID);

        return buffer.array();
    }

    public static DisconnectEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte playerID = buffer.get();

        return new DisconnectEvent(playerID);
    }
}
