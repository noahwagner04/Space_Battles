package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;

public class StartGameEvent implements NetEvent{

    public int getEventID() {return NetConstants.START_GAME_EVENT_ID;}
    public int getDataByteSize() {return 0;}

    public static byte[] serialize(NetEvent e) {
        StartGameEvent event = (StartGameEvent) e;

        // EventID, Event size, playerID
        int totalSize = 4 + 4 + event.getDataByteSize();;
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.putInt(event.getEventID());
        buffer.putInt(event.getDataByteSize());
        return buffer.array();
    }

    public static StartGameEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        return new StartGameEvent();
    }
}
