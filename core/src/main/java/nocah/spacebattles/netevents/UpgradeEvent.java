package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;

public class UpgradeEvent implements NetEvent{
    public final byte playerID;
    public final byte upgradeID;

    public UpgradeEvent(byte playerID, byte upgradeID) {
        this.playerID = playerID;
        this.upgradeID = upgradeID;
    }

    public byte getEventID() {return NetConstants.UPGRADE_EVENT_ID;}
    public short getDataByteSize() {return 1*2;}

    public byte[] serialize() {

        // EventID, Event size, playerID
        int totalSize = 1 + 2 + getDataByteSize();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.put(getEventID());
        buffer.putShort(getDataByteSize());
        buffer.put(playerID);
        buffer.put(upgradeID);

        return buffer.array();
    }

    public static UpgradeEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte playerID = buffer.get();
        byte upgradeID = buffer.get();

        return new UpgradeEvent(playerID, upgradeID);
    }
}
