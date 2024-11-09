package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ConnectedEvent implements NetEvent{
    public final int playerID;

    public ConnectedEvent(int playerID) {
        this.playerID = playerID;
    }

    public int getEventID() {return NetConstants.CONNECTED_EVENT_ID;}
    public int getDataByteSize() {return 4;}

    public static byte[] serialize(NetEvent e) {
        ConnectedEvent event = (ConnectedEvent) e;

        // EventID, Event size, playerID
        int totalSize = 4 + 4 + event.getDataByteSize();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.putInt(event.getEventID());
        buffer.putInt(event.getDataByteSize());
        buffer.putInt(event.playerID);

        return buffer.array();
    }

    public static ConnectedEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int playerID = buffer.getInt();

        return new ConnectedEvent(playerID);
    }
}
