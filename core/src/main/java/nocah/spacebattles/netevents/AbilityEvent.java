package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;

public class AbilityEvent implements NetEvent{
    public final byte playerID;
    public final byte abilityID;
    public final byte abilityNum;
    public final byte toggle;


    public AbilityEvent(byte playerID, byte abilityID, byte abilityNum, byte toggle) {
        this.playerID = playerID;
        this.abilityID = abilityID;
        this.abilityNum = abilityNum;
        this.toggle = toggle;
    }

    public byte getEventID() {return NetConstants.ABILITY_EVENT_ID;}
    public short getDataByteSize() {return 1*4;}

    public byte[] serialize() {

        // EventID, Event size, playerID
        int totalSize = 1 + 2 + getDataByteSize();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.put(getEventID());
        buffer.putShort(getDataByteSize());
        buffer.put(playerID);
        buffer.put(abilityID);
        buffer.put(abilityNum);
        buffer.put(toggle);

        return buffer.array();
    }

    public static AbilityEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte playerID = buffer.get();
        byte abilityID = buffer.get();
        byte abilityNum = buffer.get();
        byte toggle = buffer.get();

        return new AbilityEvent(playerID, abilityID, abilityNum, toggle);
    }
}
