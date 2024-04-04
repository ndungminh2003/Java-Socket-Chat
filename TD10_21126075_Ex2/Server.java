package TD10_21126075.TD10_21126075_Ex2;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
    private static List<ClientHandler> clients = new ArrayList<>();

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        ServerSocket server = null;
        Socket client;

        // Default port number we are going to use
        int portnumber = 1234;
        if (args.length >= 1) {
            portnumber = Integer.parseInt(args[0]);
        }

        // Create Server side socket
        try {
            server = new ServerSocket(portnumber);
        } catch (IOException ie) {
            System.out.println("Cannot open socket." + ie);
            System.exit(1);
        }
        System.out.println("ServerSocket is created " + server);

        // Wait for the data from the client and reply
        while (true) {
            try {
                // Listens for a connection to be made to
                // this socket and accepts it. The method blocks until
                // a connection is made
                System.out.println("Waiting for connect request...");
                client = server.accept();

                System.out.println("Connect request is accepted...");
                String clientHost = client.getInetAddress().getHostAddress();
                int clientPort = client.getPort();
                System.out.println("Client host = " + clientHost + " Client port = " + clientPort);

                // Create a new thread for the client
                ClientHandler clientHandler = new ClientHandler(client);
               
                clients.add(clientHandler);

                Thread clientThread = new Thread(clientHandler);
            
                clientThread.start();

            } catch (IOException ie) {
                ie.printStackTrace();
            }
        }
    }

    static class ClientHandler implements Runnable {
        private Socket client;
        private BufferedReader br;
        private PrintWriter pw;
        private String name; // Client name
        private long threadId; // Thread ID

        @SuppressWarnings("deprecation")
        public ClientHandler(Socket client) {
            this.client = client;
            this.threadId = Thread.currentThread().getId(); // Store the thread ID when the thread starts
        }

        public void run() {
            try {
                // Read data from the client
                InputStream clientIn = client.getInputStream();
                br = new BufferedReader(new InputStreamReader(clientIn));

                // Send response to the client
                OutputStream clientOut = client.getOutputStream();
                pw = new PrintWriter(clientOut, true);

                String msgFromClient;

                while ((msgFromClient = br.readLine()) != null) {
                    System.out.println("Message received from client = " + msgFromClient);

                    // Broadcast the message to all connected clients
                    for (ClientHandler clientHandler : clients) {
                        PrintWriter clientWriter = clientHandler.pw;
                        clientWriter.println(msgFromClient);
                        clientWriter.flush(); // Ensure the message is sent immediately
                    }
                }

            } catch (SocketException e) {
                // Handle the SocketException gracefully
            
                if (e.getMessage().equals("Connection reset")) {
                    System.out.println(threadId);
                    try {
                        // Close the connection
                        client.close();
                        br.close();
                        pw.close();

                        // Remove this client from the list of clients
                        clients.remove(this);

                        // Broadcast the disconnection message to other clients
                        for (ClientHandler otherClient : clients) {
                            if (otherClient != this) { // Skip sending to the disconnected client
                                String leftMessage = (name != null) ? name + " left the chat room."
                                        : "Unknown left the chat room.";
                                otherClient.pw.println(leftMessage);
                                otherClient.pw.flush(); // Ensure the message is sent immediately
                            }
                        }

                        System.out.println("Client disconnected abruptly.");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
