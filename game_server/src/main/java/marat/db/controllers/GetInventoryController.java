package marat.db.controllers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marat.db.DataBase;
import marat.db.PlayerIdentity;
import marat.db.RequestHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetInventoryController extends Controller {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationController.class.getName());

    public GetInventoryController(DataBase dataBase) {
        super(dataBase);
    }

    @Override
    public ClientResponseBody call(String requestBody) {
        var inventoryRequest = RequestHandler.convertFromJSON(requestBody, InventoryRequest.class);
        try {
            var itemList = dataBase.findPlayerItems(inventoryRequest.PlayerIdentity.getNickname());
            List<String> itemNames = itemList.stream()
                    .map(PlayerInventoryTable::getItemName)
                    .toList();
            return generateClientResponseBody(new InventoryResponse(itemNames));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to find player items: " + ex.getMessage());
            return new ClientResponseBody(StatusCode.ServerError, "");
        }
    }

    @AllArgsConstructor
    @Getter
    public static class InventoryRequest {
        private final PlayerIdentity PlayerIdentity;
    }

    @AllArgsConstructor
    @Getter
    public static class InventoryResponse {
        private final List<String> Items;
    }
}
