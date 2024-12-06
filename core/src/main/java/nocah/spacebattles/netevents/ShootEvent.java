package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;

public class ShootEvent implements NetEvent{
    public final byte playerID;
    public final byte minionID;
    public final int bulletID;
    public final float rotation;

    public ShootEvent(byte playerID, byte minionID, int bulletID, float rotation) {
        this.playerID = playerID;
        this.minionID = minionID;
        this.bulletID = bulletID;
        this.rotation = rotation;
    }

    public byte getEventID() {return NetConstants.SHOOT_EVENT_ID;}
    public short getDataByteSize() {return 1*2 + 4*2;}

    public byte[] serialize() {

        // EventID, Event size, playerID
        int totalSize = 1 + 2 + getDataByteSize();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.put(getEventID());
        buffer.putShort(getDataByteSize());
        buffer.put(playerID);
        buffer.put(minionID);
        buffer.putInt(bulletID);
        buffer.putFloat(rotation);

        return buffer.array();
    }

    public static ShootEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte playerID = buffer.get();
        byte minionID = buffer.get();
        int bulletID = buffer.getInt();
        float rotation = buffer.getFloat();

        return new ShootEvent(playerID, minionID, bulletID, rotation);
    }
}
