package marat.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;


public class ServerApplication {
    private final Properties properties;

    private static final Logger LOGGER = Logger.getLogger(ServerApplication.class.getName());

    private final String propetiesPath = "config.properties";

    private final String propertiesShouldDropCreate = "db.should_drop_create";

    private DataBase database;

    private RequestHandler requestHandler;

    private Server server;

    public ServerApplication() {
        properties = new Properties();
        readProperties();
    }

    public void run() {
        boolean shouldCreateDrop = Boolean.parseBoolean(properties.getProperty(propertiesShouldDropCreate));
        if (shouldCreateDrop) {
            database = new TestDataBase(properties);

        } else {
            database = new DataBase(properties);
        }
        Thread adminConsoleThread = new Thread(new AdminConsole(database));
        adminConsoleThread.start();

        requestHandler = new RequestHandler(properties, database);
        server = new Server(properties, requestHandler);
        server.run();


//        while (true) {
//            int serverPort = Integer.parseInt(properties.getProperty(serverPortProperty));
//            try {
//            socket = new DatagramSocket(serverPort);
//            } catch (SocketException e) {
//                System.out.println(e.getMessage());
//            }
//            var buffer = new byte[65536];
//            DatagramPacket packet = new DatagramPacket(buffer, serverPort);
//            try {
//                socket.receive(packet);
//            } catch (IOException e) {
//                System.out.println(e.getMessage());
//            }
//            int clientPort = socket.getPort();
//            InetAddress clientAddress = socket.getInetAddress();
//            String jsonString = new String(packet.getData(), StandardCharsets.UTF_8).trim();
//            System.out.println(jsonString);
//
//        }
    }

    private void readProperties() {
        try (InputStream input = TestDataBase.class.getClassLoader().getResourceAsStream(propetiesPath)) {
            if (input == null) {
                LOGGER.log(Level.SEVERE, "Unable to find config.properties");
                return;
            }
            properties.load(input);

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error reading config.properties: {0}", ex.getMessage());
        }
    }


}