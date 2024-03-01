package marat.db.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer {
    public void run() {
        
    }

    private static void handleClient(DatagramSocket socket, DatagramPacket packet) {
        // Process the received packet here
        String clientMessage = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Received from client: " + clientMessage);

        // Example: Send a response back to the client
        String responseMessage = "Hello, client!";
        byte[] responseData = responseMessage.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, packet.getAddress(), packet.getPort());

        try {
            socket.send(responsePacket);
            System.out.println("Response sent to client");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
