package TD10_21126075.TD10_21126075_Ex2;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        String name = JOptionPane.showInputDialog(null, "Enter your name:");

        // Create a JFrame to hold the chat window
        JFrame frame = new JFrame("Chat Window");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a JPanel to hold the components
        JPanel panel = new JPanel();

        // Create a JTextArea for displaying the chat messages
        JTextArea chatDisplay = new JTextArea(10, 20);
        chatDisplay.setEditable(false);

        // Create a JTextField for the chat input
        JTextField chatInput = new JTextField(20);

        // Create a JButton for sending the chat message
        JButton sendButton = new JButton("Send");

        // Add the components to the panel
        panel.add(chatDisplay);
        panel.add(chatInput);
        panel.add(sendButton);

        // Add an ActionListener to the send button
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Send message to the server
                try (Socket clientSocket = new Socket("localhost", 1234)) {
                    OutputStream clientOut = clientSocket.getOutputStream();
                    PrintWriter pw = new PrintWriter(clientOut, true);
                    String message = chatInput.getText();
                    pw.println(name + ": " + message); // Include sender's name in the message
                    chatInput.setText(""); // Clear the chat input

                } catch (IOException ie) {
                    System.out.println("I/O error " + ie);
                }
            }
        });

        // Create a thread to receive messages from the server
        Thread receiveThread = new Thread(new Runnable() {
            public void run() {
                try (Socket clientSocket = new Socket("localhost", 1234)) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String response;
                    while ((response = br.readLine()) != null) {
                        chatDisplay.append(response + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        receiveThread.start(); // Start the receive thread

        // Add the panel to the frame and display it
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
