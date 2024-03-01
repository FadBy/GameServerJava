package marat.db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import marat.db.controllers.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Optional;


public class RequestHandler {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private final Map<String, Controller> controllers = new HashMap<String, Controller>();

    private final Properties properties;

    private final DataBase database;

    public RequestHandler(Properties properties, DataBase dataBase) {
        this.properties = properties;
        this.database = dataBase;
        controllers.put("authentication", new AuthenticationController(database));
        controllers.put("get_inventory", new GetInventoryController(dataBase));
        controllers.put("loot_pick_up", new LootPickUpController(dataBase));
        controllers.put("process_money", new ProcessMoneyController(dataBase));
        controllers.put("register_movement", new RegisterMovementController(dataBase));
        controllers.put("register_action", new RegisterActionController(dataBase));
    }

    public String handle(String jsonString) {
        ClientRequest clientRequest = convertFromJSON(jsonString, ClientRequest.class);
        var clientResponseBody = controllers.get(clientRequest.getRequestType()).call(clientRequest.getBody());
        var clientResponse = new ClientResponse(clientResponseBody.getStatusCode(), clientRequest, clientResponseBody.getBody());
        return convertToJSON(clientResponse);
    }

    public static <T> T convertFromJSON(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public static <T> String convertToJSON(T o) {
        return gson.toJson(o);
    }
}
