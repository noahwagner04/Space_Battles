package nocah.spacebattles;

import nocah.spacebattles.netevents.NetEvent;
import nocah.spacebattles.netevents.NetConstants;
import nocah.spacebattles.netevents.SerializerRegistry;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server {
    private static final int PORT = 12345;
    private List<ClientHandler> clientHandlers = new ArrayList<>();
    private ConcurrentLinkedQueue<NetEvent> eventQueue;
    private List<Socket> clientSockets = new ArrayList<>();
    ServerSocket serverSocket;
    private boolean exit = false;

    public void startServer() {
        this.eventQueue = new ConcurrentLinkedQueue<>();
        new Thread(new ServerListener()).start();
    }

    public void stop() {
        exit = true;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
        for (int i = 0; i < clientHandlers.size(); i++) {
            try {
                clientSockets.get(i).close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    public void broadcastMessageInQueue() {
        byte[] outData;
        if (!eventQueue.isEmpty()) {
            NetEvent e = eventQueue.poll();
            outData = SerializerRegistry.serialize(e.getEventID(), e);
        } else {
            return;
        }
        for (Socket client : clientSockets) {
            try {
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                out.write(outData);
                out.flush();
            } catch (IOException e) {
                System.err.println("error broadcasting message: " + e.getMessage());
            }
        }
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(PORT);
                System.out.println("Chat server started on port " + PORT);
                while (!exit) {
                    Socket clientSocket = serverSocket.accept();
                    clientSockets.add(clientSocket);
                    ClientHandler clientHandler = new ClientHandler(clientSocket, eventQueue);
                    clientHandlers.add(clientHandler);
                    new Thread(clientHandler).start();
                }
            } catch (IOException e) {
                System.err.println("Error running server: " + e.getMessage());
            }
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private DataInputStream in;
    private ConcurrentLinkedQueue<NetEvent> eventQueue;
    public ClientHandler(Socket socket,  ConcurrentLinkedQueue<NetEvent> eventQueue) {
        this.clientSocket = socket;
        this.eventQueue = eventQueue;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(clientSocket.getInputStream());

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
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}
