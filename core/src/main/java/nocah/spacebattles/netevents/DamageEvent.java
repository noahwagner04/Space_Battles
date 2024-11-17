package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;

public class DamageEvent implements NetEvent{
    public final byte entityType;
    public final int entityId;
    public final float damageAmount;


    public DamageEvent( byte entityType, int entityId, float damageAmount, float currentHealth) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.damageAmount = (currentHealth - damageAmount <= 0) ? Float.MAX_VALUE : damageAmount;
    }

    public byte getEventID() {return NetConstants.DAMAGE_EVENT_ID;}
    public short getDataByteSize() {return 1 + 4*2;}

    public byte[] serialize() {

        // EventID, Event size, playerID
        int totalSize = 1 + 2 + getDataByteSize();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.put(getEventID());
        buffer.putShort(getDataByteSize());
        buffer.put(entityType);
        buffer.putInt(entityId);
        buffer.putFloat(damageAmount);

        return buffer.array();
    }

    public static DamageEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        ByteBuffer buffer = ByteBuffer.wrap(data);

        byte entityType = buffer.get();
        int entityId = buffer.getInt();
        float damageAmount = buffer.getFloat();

        return new DamageEvent(entityType, entityId, damageAmount, damageAmount*2);
    }
}
