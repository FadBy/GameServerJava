package marat.db.controllers;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marat.db.*;

public class AuthenticationController extends Controller {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationController.class.getName());

    public AuthenticationController(DataBase dataBase) {
        super(dataBase);
    }

    @Override
    public ClientResponseBody call(String requestBody) {
        var authorizationRequest = RequestHandler.convertFromJSON(requestBody, AuthorizationRequest.class);
        String nickname = authorizationRequest.getPlayerIdentity().getNickname();

        try {
            PlayerProfileTable playerProfileTable = dataBase.findPlayerProfile(nickname);

            if (playerProfileTable == null) {
                dataBase.insertPlayerProfile(nickname);
                playerProfileTable = dataBase.findPlayerProfile(nickname);
            } else if (playerProfileTable.isBanStatus()) {
                return generateClientResponseBody(new AuthorizationConfirmation(false, "You're banned", playerProfileTable));
            }
            return generateClientResponseBody(new AuthorizationConfirmation(true, "", playerProfileTable));

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database operation failed: " + ex.getMessage());
            return new ClientResponseBody(StatusCode.ServerError, ex.getMessage());
        }
    }

    @Getter
    @AllArgsConstructor
    private static class AuthorizationConfirmation {
        private final boolean Permitted;
        private final String Message;
        private final PlayerProfileTable PlayerProfile;
    }

    @Getter
    @AllArgsConstructor
    private static class AuthorizationRequest {
        private PlayerIdentity PlayerIdentity;
    }
}
