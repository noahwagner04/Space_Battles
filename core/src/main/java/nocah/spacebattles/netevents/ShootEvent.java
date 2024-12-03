package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;

public class ShootEvent implements NetEvent{
    public final byte playerID;
    public final int bulletID;

    public ShootEvent(byte playerID, int bulletID) {
        this.playerID = playerID;
        this.bulletID = bulletID;
    }

    public byte getEventID() {return NetConstants.SHOOT_EVENT_ID;}
    public short getDataByteSize() {return 1 + 4;}

    public byte[] serialize() {

        // EventID, Event size, playerID
        int totalSize = 1 + 2 + getDataByteSize();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.put(getEventID());
        buffer.putShort(getDataByteSize());
        buffer.put(playerID);
        buffer.putInt(bulletID);

        return buffer.array();
    }

    public static ShootEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte playerID = buffer.get();
        int bulletID = buffer.getInt();

        return new ShootEvent(playerID, bulletID);
    }
}
