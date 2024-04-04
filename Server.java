package TD10_21126075;

/* SERVER – may enhance to work for multiple clients */
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
  private static List<ClientHandler> clients = new ArrayList<>();

  @SuppressWarnings("resource")
  public static void main(String[] args) {
    ServerSocket server = null;

    int portnumber = 1234;
    if (args.length >= 1) {
      portnumber = Integer.parseInt(args[0]);
    }

    try {
      server = new ServerSocket(portnumber);
      System.out.println("ServerSocket is created " + server);
    } catch (IOException ie) {
      System.out.println("Cannot open socket." + ie);
      System.exit(1);
    }

    while (true) {
      try {
        System.out.println("Waiting for connect request...");
        Socket client = server.accept();
        System.out.println("Connect request is accepted...");

        ClientHandler clientHandler = new ClientHandler(client);
        clients.add(clientHandler);
        clientHandler.start();
      } catch (IOException ie) {
        ie.printStackTrace();
      }
    }
  }

  static class ClientHandler extends Thread {
    private Socket client;
    private PrintWriter pw;

    public ClientHandler(Socket client) {
      this.client = client;
    }

    public void run() {
      try {
        InputStream clientIn = client.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(clientIn));

        OutputStream clientOut = client.getOutputStream();
        pw = new PrintWriter(clientOut, true);

        String msgFromClient;
        while ((msgFromClient = br.readLine()) != null) {
          System.out.println("Message received from client = " + msgFromClient);

          if (msgFromClient.equalsIgnoreCase("goodbye")) {
            break;
          }

          String ansMsg = "I received this message from your part: " + msgFromClient;
          broadcast(ansMsg);
        }
      } catch (IOException e) {
        System.out.println("Client disconnected abruptly.");
      } finally {
        try {
          client.close();
          clients.remove(this);
          broadcast("User " + client.getInetAddress().getHostAddress() + " left the chat room.");
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      }
    }

    private void broadcast(String message) {
      for (ClientHandler clientHandler : clients) {
        if (clientHandler != this) {
          clientHandler.pw.println(message);
          clientHandler.pw.flush();
        }
      }
    }
  }
}