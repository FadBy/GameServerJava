package marat.db.controllers;

import lombok.AllArgsConstructor;
import marat.db.DataBase;
import marat.db.PlayerIdentity;
import marat.db.RequestHandler;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegisterActionController extends Controller {
    private static final Logger LOGGER = Logger.getLogger(RegisterActionController.class.getName());

    public RegisterActionController(DataBase dataBase) {
        super(dataBase);
    }

    @Override
    public ClientResponseBody call(String requestBody) {
        try {
            var actionRequest = RequestHandler.convertFromJSON(requestBody, ActionRequest.class);
            dataBase.insertPlayerAction(actionRequest.PlayerIdentity.getNickname(), actionRequest.ActionName);
            return new ClientResponseBody(StatusCode.OK, "");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to insert PlayerAction: " + ex.getMessage());
            return  new ClientResponseBody(StatusCode.ServerError, ex.getMessage());
        }
    }

    @AllArgsConstructor
    private static class ActionRequest {
        private final PlayerIdentity PlayerIdentity;
        private final String ActionName;
    }
}
