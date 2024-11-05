package nocah.spacebattles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private String serverAddress;
    private static final int SERVER_PORT = 12345;
    private PrintWriter out;
    private MessageReceiver msgReceiver;
    private Socket socket;

    public Client(String serverAddress) {
        this.serverAddress = serverAddress;
        startClient();
    }

    public void startClient() {
        try {
            socket = new Socket(serverAddress, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to the chat server.");
            msgReceiver = new MessageReceiver(socket);
            new Thread(msgReceiver).start();

        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        if (out != null && !message.isEmpty()) {
            out.println(message);
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
    private Socket socket;

    public MessageReceiver(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String message;
            while ((message = in.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            System.err.println("Error reading from server: " + e.getMessage());
        }
    }
}
