package marat.db.controllers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marat.db.DataBase;
import marat.db.PlayerIdentity;
import marat.db.PlayerProfileTable;
import marat.db.RequestHandler;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProcessMoneyController extends Controller {
    private static final Logger LOGGER = Logger.getLogger(LootPickUpController.class.getName());

    public ProcessMoneyController(DataBase dataBase) {
        super(dataBase);
    }

    @Override
    public ClientResponseBody call(String requestBody) {
        try {
            var processMoneyRequest = RequestHandler.convertFromJSON(requestBody, ProcessMoneyRequest.class);
            String playerNickname = processMoneyRequest.PlayerIdentity.getNickname();
            String moneySource = processMoneyRequest.MoneySource;
            boolean enoughMoney = dataBase.checkPlayerMoneyAmount(playerNickname, moneySource);
            if (!enoughMoney) {
                return generateClientResponseBody("", StatusCode.ClientError);
            }
            dataBase.processMoneyTransaction(playerNickname, moneySource);
            var playerProfileTable = dataBase.findPlayerProfile(playerNickname);
            return generateClientResponseBody(new ProcessMoneyResponse(playerProfileTable));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return new ClientResponseBody(StatusCode.ServerError, ex.getMessage());
        }
    }

    @AllArgsConstructor
    @Getter
    private static class ProcessMoneyRequest {
        private PlayerIdentity PlayerIdentity;
        private String MoneySource;
    }

    @AllArgsConstructor
    @Getter
    private static class ProcessMoneyResponse {
        private PlayerProfileTable PlayerProfileTable;
    }
}
