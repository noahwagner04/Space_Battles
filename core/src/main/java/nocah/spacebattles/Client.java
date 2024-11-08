package nocah.spacebattles;

import nocah.spacebattles.netevents.NetEvent;
import nocah.spacebattles.netevents.SerializerRegistry;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {
    private String serverAddress;
    private static final int SERVER_PORT = 12345;
    private DataOutputStream out;
    private MessageReceiver msgReceiver;
    private Socket socket;
    private ConcurrentLinkedQueue<NetEvent> eventQueue;

    public Client(String serverAddress) {
        this.serverAddress = serverAddress;
        this.eventQueue = new ConcurrentLinkedQueue<>();
        startClient();
    }

    public void startClient() {
        try {
            socket = new Socket(serverAddress, SERVER_PORT);
            out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Connected to the chat server.");
            msgReceiver = new MessageReceiver(socket, eventQueue);
            new Thread(msgReceiver).start();

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    public void sendEvent(NetEvent event) {
        if (out != null) {
            byte[] outData = SerializerRegistry.serialize(event.getEventID(), event);
            try {
                out.write(outData);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error sending data to server: " + e.getMessage());
            }
        }
    }

    public void printMessageInQueue() {
            if (!eventQueue.isEmpty()) {
                NetEvent e = eventQueue.poll();
                byte[]  outData = SerializerRegistry.serialize(e.getEventID(), e);

                int nameLength = ByteBuffer.wrap(outData, 8, 4).getInt();
                String name = new String(outData, 12, nameLength, StandardCharsets.UTF_8);

                int messageLength = ByteBuffer.wrap(outData, 12 + nameLength, 4).getInt();
                String message = new String(outData, 12 + nameLength + 4, messageLength, StandardCharsets.UTF_8);

                System.out.println(name + ": " + message);
            }
    }

    public void stop() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}

class MessageReceiver implements Runnable {
    private final Socket socket;
    private DataInputStream in;
    private ConcurrentLinkedQueue<NetEvent> eventQueue;

    public MessageReceiver(Socket socket, ConcurrentLinkedQueue<NetEvent> eventQueue) {
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
                    int eventID = ByteBuffer.wrap(buffer, currentPos, 4).getInt();
                    currentPos += 4;
                    int eventLength = ByteBuffer.wrap(buffer, currentPos, 4).getInt();
                    currentPos += 4;

                    // I might need to check if the full event data is available
                    byte[] eventData = new byte[eventLength];
                    System.arraycopy(buffer, currentPos, eventData, 0, eventLength);

                    NetEvent event = SerializerRegistry.deserialize(eventID, eventData);
                    eventQueue.add(event);

                    currentPos += eventLength;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from server: " + e.getMessage());
        }
    }
}
