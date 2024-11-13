package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;

public class StartGameEvent implements NetEvent{

    public byte getEventID() {return NetConstants.START_GAME_EVENT_ID;}
    public short getDataByteSize() {return 0;}

    public byte[] serialize() {

        // EventID, Event size
        int totalSize = 1 + 2 + getDataByteSize();;
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.put(getEventID());
        buffer.putShort(getDataByteSize());
        return buffer.array();
    }

    public static StartGameEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        return new StartGameEvent();
    }
}
