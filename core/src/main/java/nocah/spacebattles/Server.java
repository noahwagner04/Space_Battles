package nocah.spacebattles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 12345;
    private static List<ClientHandler> clientHandlers = new ArrayList<>();

    public void startServer() {
        new Thread(new ServerListener()).start();
    }

    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Chat server started on port " + PORT);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    //System.out.println("New client connected: " + clientSocket.getInetAddress());
                    ClientHandler clientHandler = new ClientHandler(clientSocket, clientHandlers);
                    clientHandlers.add(clientHandler);
                    new Thread(clientHandler).start();
                }
            } catch (IOException e) {
                System.err.println("Error starting server: " + e.getMessage());
            }
        }
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private List<ClientHandler> clients;

    public ClientHandler(Socket socket, List<ClientHandler> clients) {
        this.clientSocket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            String message;
            while ((message = in.readLine()) != null) {
                broadcastMessage(message);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                clients.remove(this);
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.out.println(message);
        }
    }
}
