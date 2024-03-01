package marat.db.controllers;

import lombok.AllArgsConstructor;
import marat.db.DataBase;
import marat.db.PlayerIdentity;
import marat.db.RequestHandler;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegisterMovementController extends Controller {
    private static final Logger LOGGER = Logger.getLogger(RegisterActionController.class.getName());

    public RegisterMovementController(DataBase dataBase) {
        super(dataBase);
    }

    @Override
    public ClientResponseBody call(String requestBody) {
        try {
            var movementResponse = RequestHandler.convertFromJSON(requestBody, MovementResponse.class);
            dataBase.insertPlayerMovement(movementResponse.PlayerIdentity.getNickname(), movementResponse.MoveDistance);
            return new ClientResponseBody(StatusCode.OK, "");
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to insert PlayerMovement: " + ex.getMessage());
            return new ClientResponseBody(StatusCode.ServerError, ex.getMessage());
        }
    }

    @AllArgsConstructor
    private static class MovementResponse {
        private final PlayerIdentity PlayerIdentity;
        private final float MoveDistance;
    }
}
