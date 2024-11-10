package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;

public class MoveEvent implements NetEvent{
    public final int playerID;
    public final float x;
    public final float y;
    public final float rotation;

    public MoveEvent(int playerID, float x, float y, float rotation) {
        this.playerID = playerID;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }

    public int getEventID() {return NetConstants.MOVE_PLAYER_EVENT_ID;}

    public int getDataByteSize() {return 4 + 4 + 4 + 4;}

    public static byte[] serialize(NetEvent e) {
        MoveEvent event = (MoveEvent) e;

        // EventID, Event size, playerID
        int totalSize = 4 + 4 + event.getDataByteSize();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.putInt(event.getEventID());
        buffer.putInt(event.getDataByteSize());
        buffer.putInt(event.playerID);
        buffer.putFloat(event.x);
        buffer.putFloat(event.y);
        buffer.putFloat(event.rotation);

        return buffer.array();
    }

    public static MoveEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int playerID = buffer.getInt();
        float x = buffer.getFloat();
        float y = buffer.getFloat();
        float rotation = buffer.getFloat();

        return new MoveEvent(playerID, x, y, rotation);
    }
}
