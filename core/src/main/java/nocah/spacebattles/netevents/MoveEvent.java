package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;

public class MoveEvent implements NetEvent{
    public final byte playerID;
    public final float x;
    public final float y;
    public final float rotation;
    public final float xVel;
    public final float yVel;
    public final float rotVel;
    public final byte thrustAnimationState;

    public MoveEvent(byte playerID, float x, float y, float rotation, float xVel, float yVel, float rotVel, byte thrustAnimationState) {
        this.playerID = playerID;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.xVel = xVel;
        this.yVel = yVel;
        this.rotVel = rotVel;
        this.thrustAnimationState = thrustAnimationState;
    }

    public byte getEventID() {return NetConstants.MOVE_PLAYER_EVENT_ID;}

    public short getDataByteSize() {return 1 + 4*6 + 1;}

    public byte[] serialize() {
        // EventID, Event size, playerID, x, y, rotation, xvel, yvel, rvel, tanimation
        int totalSize = 1 + 2 + getDataByteSize();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.put(getEventID());
        buffer.putShort(getDataByteSize());
        buffer.put(playerID);
        buffer.putFloat(x);
        buffer.putFloat(y);
        buffer.putFloat(rotation);
        buffer.putFloat(xVel);
        buffer.putFloat(yVel);
        buffer.putFloat(rotVel);
        buffer.put(thrustAnimationState);

        return buffer.array();
    }

    public static MoveEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte playerID = buffer.get();
        float x = buffer.getFloat();
        float y = buffer.getFloat();
        float rotation = buffer.getFloat();
        float xVel = buffer.getFloat();
        float yVel = buffer.getFloat();
        float rotVel = buffer.getFloat();
        byte thrustAnimationState = buffer.get();

        return new MoveEvent(playerID, x, y, rotation, xVel, yVel, rotVel, thrustAnimationState);
    }
}
