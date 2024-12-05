package nocah.spacebattles;

import nocah.spacebattles.netevents.ConnectedEvent;
import nocah.spacebattles.netevents.NetEvent;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

// NOTE: creating all the send, broadcast, and stuff like that needs to go in its own namespace
// also, creating all the out streams in each function is a bit goofy, do it once and store it

public class Server {
    private static final int PORT = 12345;
    private List<DataReceiver> dataReceivers = new ArrayList<>();
    public ConcurrentLinkedQueue<NetEvent> eventQueue;
    public Socket[] clientSockets = new Socket[SpaceBattles.MAX_PLAYERS - 1];
    ServerSocket serverSocket;
    private boolean exit = false;
    private SpaceBattles game;

    public void startServer(SpaceBattles game) {
        this.eventQueue = new ConcurrentLinkedQueue<>();
        new Thread(new ServerListener()).start();
        this.game = game;
    }

    public void stop() {
        exit = true;
        stopListening();

        for (Socket client : clientSockets) {
            if (client == null) continue;
            try {
                client.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    public void stopListening() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
    }

    public void broadcastEvent(NetEvent event) {
        byte[] outData = event.serialize();
        for (Socket client : clientSockets) {
            if (client == null) continue;
            try {
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                out.write(outData);
                out.flush();
            } catch (IOException e) {
                System.err.println("error broadcasting message: " + e.getMessage());
            }
        }
    }

    public void sendEvent(NetEvent event, int playerID) {
        byte[] outData = event.serialize();
        try {
            DataOutputStream out = new DataOutputStream(clientSockets[playerID - 1].getOutputStream());
            out.write(outData);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending data to server: " + e.getMessage());
        }
    }

    public void broadcastExcept(NetEvent event, int playerID) {
        byte[] outData = event.serialize();
        for (int i = 0; i < clientSockets.length; i++) {
            Socket client = clientSockets[i];
            if (client == null || i == playerID - 1) continue;

            try {
                DataOutputStream out = new DataOutputStream(client.getOutputStream());
                out.write(outData);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error broadcasting message: " + e.getMessage());
            }
        }
    }



    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(PORT);
                System.out.println("server started on port " + PORT);
                while (!exit) {
                    Socket clientSocket = serverSocket.accept();
                    int index = Arrays.asList(game.players).indexOf(null);
                    clientSockets[index - 1] = clientSocket;
                    sendEvent(new ConnectedEvent((byte)index), index);
                    DataReceiver dataReceiver = new DataReceiver(new DataInputStream(clientSocket.getInputStream()), eventQueue);
                    dataReceivers.add(dataReceiver);
                    new Thread(dataReceiver).start();
                }
            } catch (IOException e) {
                System.err.println("Error running server: " + e.getMessage());
            }
        }
    }
}
