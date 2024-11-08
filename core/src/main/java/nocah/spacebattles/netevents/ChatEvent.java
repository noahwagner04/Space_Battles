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

    public int getEventID() {return NetConstants.CHAT_EVENT_ID;}
    public int getDataByteSize() {
        return 4 + message.getBytes(StandardCharsets.UTF_8).length + 4 + sender.getBytes(StandardCharsets.UTF_8).length;
    }

    public static byte[] serialize(NetEvent e) {
        ChatEvent chatEvent = (ChatEvent) e;

        byte[] messageBytes = chatEvent.message.getBytes(StandardCharsets.UTF_8);
        byte[] senderBytes = chatEvent.sender.getBytes(StandardCharsets.UTF_8);

        // EventID, Event size, sender length, sender string, message length, message string
        int totalSize = 4 + 4 + chatEvent.getDataByteSize();
        ByteBuffer buffer = ByteBuffer.allocate(totalSize );

        buffer.putInt(chatEvent.getEventID());
        buffer.putInt(chatEvent.getDataByteSize());
        buffer.putInt(senderBytes.length);
        buffer.put(senderBytes);
        buffer.putInt(messageBytes.length);
        buffer.put(messageBytes);

        return buffer.array();
    }

    public static ChatEvent deserialize(byte[] data) {
        // remember that it's just the data, so we don't expect the eventid or event size to be in here
        ByteBuffer buffer = ByteBuffer.wrap(data);

        int senderLength = buffer.getInt();
        byte[] senderBytes = new byte[senderLength];
        buffer.get(senderBytes);
        String sender = new String(senderBytes, StandardCharsets.UTF_8);

        int messageLength = buffer.getInt();
        byte[] messageBytes = new byte[messageLength];
        buffer.get(messageBytes);
        String message = new String(messageBytes, StandardCharsets.UTF_8);

        return new ChatEvent(message, sender);
    }
}
