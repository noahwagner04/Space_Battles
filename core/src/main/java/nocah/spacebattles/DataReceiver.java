package nocah.spacebattles;

import nocah.spacebattles.netevents.DeserializerRegistry;
import nocah.spacebattles.netevents.NetEvent;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataReceiver implements Runnable {
    private final Socket socket;
    private DataInputStream in;
    private ConcurrentLinkedQueue<NetEvent> eventQueue;

    public DataReceiver(Socket socket, ConcurrentLinkedQueue<NetEvent> eventQueue) {
        this.socket = socket;
        this.eventQueue = eventQueue;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {

                int currentPos = 0;

                while (currentPos < bytesRead) {
                    byte eventID = ByteBuffer.wrap(buffer, currentPos, 1).get();
                    currentPos += 1;
                    short eventLength = ByteBuffer.wrap(buffer, currentPos, 2).getShort();
                    currentPos += 2;

                    // I might need to check if the full event data is available
                    byte[] eventData = new byte[eventLength];
                    System.arraycopy(buffer, currentPos, eventData, 0, eventLength);

                    NetEvent event = DeserializerRegistry.deserialize(eventID, eventData);
                    eventQueue.add(event);

                    currentPos += eventLength;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing reading socket: " + e.getMessage());
            }
        }
    }
}
