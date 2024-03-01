package marat.db.controllers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marat.db.DataBase;
import marat.db.PlayerIdentity;
import marat.db.RequestHandler;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LootPickUpController extends Controller {
    private static final Logger LOGGER = Logger.getLogger(LootPickUpController.class.getName());

    public LootPickUpController(DataBase dataBase) {
        super(dataBase);
    }

    @Override
    public ClientResponseBody call(String requestBody) {
        var lootPickUpRequest = RequestHandler.convertFromJSON(requestBody, LootPickUpRequest.class);

        try {
            Map<String, Float> itemToChance = dataBase.findItemChance(lootPickUpRequest.DropSource);

            List<Map.Entry<String, Float>> cumulativeChances = toCumulativeList(itemToChance);

            String selectedItem = selectItemBasedOnChance(cumulativeChances);

            String itemJson = RequestHandler.convertToJSON(new LootPickUpResponse(selectedItem));
            dataBase.insertPlayerInventory(lootPickUpRequest.PlayerIdentity.getNickname(), selectedItem);
            return new ClientResponseBody(StatusCode.OK, itemJson);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed finding itemChance: " + ex.getMessage());
            return new ClientResponseBody(StatusCode.ServerError, ex.getMessage());
        }
    }

    private List<Map.Entry<String, Float>> toCumulativeList(Map<String, Float> itemToChance) {
        List<Map.Entry<String, Float>> items = new ArrayList<>();
        float chanceSum = 0f;
        for (var entry : itemToChance.entrySet()) {
            chanceSum += entry.getValue();
            items.add(new AbstractMap.SimpleEntry<>(entry.getKey(), chanceSum));
        }
        return items;
    }

    private String selectItemBasedOnChance(List<Map.Entry<String, Float>> cumulativeChances) {
        if (cumulativeChances.isEmpty()) {
            return "";
        }

        float totalChance = cumulativeChances.get(cumulativeChances.size() - 1).getValue();
        float randomChance = getRandomFloat(0f, totalChance);

        for (var entry : cumulativeChances) {
            if (randomChance <= entry.getValue()) {
                return entry.getKey();
            }
        }

        return cumulativeChances.get(cumulativeChances.size() - 1).getKey();
    }

    private static float getRandomFloat(float min, float max) {
        return new Random().nextFloat() * (max - min) + min;
    }


    @AllArgsConstructor
    @Getter
    public static class LootPickUpRequest {
        private final PlayerIdentity PlayerIdentity;
        private final String DropSource;
    }

    @AllArgsConstructor
    @Getter
    public static class LootPickUpResponse {
        private final String ItemName;
    }
}
