package marat.db.controllers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import marat.db.DataBase;
import marat.db.PlayerProfileTable;
import marat.db.RequestHandler;

import java.util.Optional;

public abstract class Controller {
    protected DataBase dataBase;

    public abstract ClientResponseBody call(String requestBody);

    public Controller(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    protected <T> ClientResponseBody generateClientResponseBody(T bodyObject) {
        String json = RequestHandler.convertToJSON(bodyObject);
        return new ClientResponseBody(StatusCode.OK, json);
    }

    protected ClientResponseBody generateClientResponseBody(String jsonObject, StatusCode statusCode) {
        return new ClientResponseBody(statusCode, jsonObject);
    }

    protected ClientResponseBody generateClientResponseBody(String jsonObject) {
        return generateClientResponseBody(jsonObject, StatusCode.OK);
    }

    @AllArgsConstructor
    @Getter
    public static class ClientResponseBody {
        private final int statusCode;
        private final String body;

        public ClientResponseBody(StatusCode statusCode, String body) {
            this.body = body;
            this.statusCode = statusCode.getValue();
        }
    }

    @Getter
    public enum StatusCode {
        OK(0), ClientError(1), ServerError(2);

        private final int value;
        private StatusCode(int value) {
            this.value = value;
        }

    }
}
