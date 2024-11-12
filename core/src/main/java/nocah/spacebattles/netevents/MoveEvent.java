package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;

public class MoveEvent implements NetEvent{
    public final int playerID;
    public final float x;
    public final float y;
    public final float rotation;
    public final float xVel;
    public final float yVel;
    public final float rotVel;
    public final byte thrustAnimationState;

    public MoveEvent(int playerID, float x, float y, float rotation, float xVel, float yVel, float rotVel, byte thrustAnimationState) {
        this.playerID = playerID;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.xVel = xVel;
        this.yVel = yVel;
        this.rotVel = rotVel;
        this.thrustAnimationState = thrustAnimationState;
    }

    public int getEventID() {return NetConstants.MOVE_PLAYER_EVENT_ID;}

    public int getDataByteSize() {return 4*7 + 1;}

    public static byte[] serialize(NetEvent e) {
        MoveEvent event = (MoveEvent) e;

        // EventID, Event size, playerID, x, y, rotation
        int totalSize = 4 + 4 + event.getDataByteSize();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.putInt(event.getEventID());
        buffer.putInt(event.getDataByteSize());
        buffer.putInt(event.playerID);
        buffer.putFloat(event.x);
        buffer.putFloat(event.y);
        buffer.putFloat(event.rotation);
        buffer.putFloat(event.xVel);
        buffer.putFloat(event.yVel);
        buffer.putFloat(event.rotVel);
        buffer.put(event.thrustAnimationState);

        return buffer.array();
    }

    public static MoveEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int playerID = buffer.getInt();
        float x = buffer.getFloat();
        float y = buffer.getFloat();
        float rotation = buffer.getFloat();
        float xVel = buffer.getFloat();
        float yVel = buffer.getFloat();
        float rotVel = buffer.getFloat();
        byte thustAnimationState = buffer.get();

        return new MoveEvent(playerID, x, y, rotation, xVel, yVel, rotVel, thustAnimationState);
    }
}
