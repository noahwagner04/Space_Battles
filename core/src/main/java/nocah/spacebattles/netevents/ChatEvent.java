package nocah.spacebattles.netevents;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ChatEvent implements NetEvent {
    public final String message;
    public final String sender;

    public ChatEvent( String sender, String data) {
        this.message = data;
        this.sender = sender;
    }

    public byte getEventID() {return NetConstants.CHAT_EVENT_ID;}
    public short getDataByteSize() {
        return (short)(2 + message.getBytes(StandardCharsets.UTF_8).length + 2 + sender.getBytes(StandardCharsets.UTF_8).length);
    }

    public byte[] serialize() {

        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] senderBytes = sender.getBytes(StandardCharsets.UTF_8);

        // EventID, Event size, sender length, sender string, message length, message string
        int totalSize = 1 + 2 + getDataByteSize();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);

        buffer.put(getEventID());
        buffer.putShort(getDataByteSize());
        buffer.putShort((short)senderBytes.length);
        buffer.put(senderBytes);
        buffer.putShort((short)messageBytes.length);
        buffer.put(messageBytes);

        return buffer.array();
    }

    public static ChatEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        ByteBuffer buffer = ByteBuffer.wrap(data);

        short senderLength = buffer.getShort();
        byte[] senderBytes = new byte[senderLength];
        buffer.get(senderBytes);
        String sender = new String(senderBytes, StandardCharsets.UTF_8);

        short messageLength = buffer.getShort();
        byte[] messageBytes = new byte[messageLength];
        buffer.get(messageBytes);
        String message = new String(messageBytes, StandardCharsets.UTF_8);

        return new ChatEvent(message, sender);
    }
}
