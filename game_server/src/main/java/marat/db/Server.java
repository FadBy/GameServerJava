package marat.db;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private int threadPoolSize = 1;
    private final ExecutorService executorService;
    private DatagramSocket socket;

    private final Properties properties;

    private final String serverPortProperty = "server.port";

    private final String threadPoolSizeProperty = "thread.pool_size";

    private final RequestHandler requestHandler;


    public Server(Properties properties, RequestHandler requestHandler) {
        this.properties = properties;
        threadPoolSize = Integer.parseInt(properties.getProperty(threadPoolSizeProperty));
        executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.requestHandler = requestHandler;
    }

    public void run() {
        int serverPort = Integer.parseInt(properties.getProperty(serverPortProperty));
        try {
            socket = new DatagramSocket(serverPort);
            System.out.println("Server is running on port " + serverPort);
        } catch (SocketException e) {
            System.out.println("Socket exception: " + e.getMessage());
            return;
        }

        while (true) {
            var buffer = new byte[65536];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                Runnable requestHandler = createRequestHandlerThread(packet);
                executorService.execute(requestHandler);
            } catch (IOException e) {
                System.out.println("IO exception: " + e.getMessage());
            }
        }
    }

    private Runnable createRequestHandlerThread(DatagramPacket packet) {
        return () -> {
            InetAddress clientAddress = packet.getAddress();
            int clientPort = packet.getPort();
            String jsonString = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8).trim();
            System.out.println("Received: " + jsonString);

            var response = requestHandler.handle(jsonString);

            System.out.println("Sent: " + response);
            byte[] data = response.getBytes(StandardCharsets.UTF_8);
            DatagramPacket responsePacket = new DatagramPacket(data, data.length, clientAddress, clientPort);
            try {
                socket.send(responsePacket);
            } catch (IOException e) {
                System.out.println("Couldn't send response: " + e.getMessage());
            }
        };
    }


}