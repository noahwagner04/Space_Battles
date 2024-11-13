package nocah.spacebattles;

import nocah.spacebattles.netevents.NetEvent;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {
    private String serverAddress;
    private static final int SERVER_PORT = 12345;
    private DataOutputStream out;
    private DataReceiver msgReceiver;
    private Socket socket;
    public ConcurrentLinkedQueue<NetEvent> eventQueue;

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
            msgReceiver = new DataReceiver(socket, eventQueue);
            new Thread(msgReceiver).start();

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    public void sendEvent(NetEvent event) {
        if (out != null) {
            byte[] outData = event.serialize();
            try {
                out.write(outData);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error sending data to server: " + e.getMessage());
            }
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
