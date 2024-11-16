package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;

public class ShootEvent implements NetEvent{
    public final byte playerID;

    public ShootEvent(byte playerID) {
        this.playerID = playerID;
    }

    public byte getEventID() {return NetConstants.SHOOT_EVENT_ID;}
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

    public static ShootEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte playerID = buffer.get();

        return new ShootEvent(playerID);
    }
}
