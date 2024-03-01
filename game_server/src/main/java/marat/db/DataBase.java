package marat.db;

import marat.db.controllers.PlayerInventoryTable;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataBase {
    private static final Logger LOGGER = Logger.getLogger(ServerApplication.class.getName());

    private final String url;
    private final String username;
    private final String password;

    private static final String dbUrl = "db.url";
    private static final String dbUsername = "db.username";
    private static final String dbPassword = "db.password";

    public DataBase(Properties properties) {
        this.url = properties.getProperty(dbUrl);
        this.username = properties.getProperty(dbUsername);
        this.password = properties.getProperty(dbPassword);
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public void insertItem(String name) throws SQLException {
        String SQL = "INSERT INTO item (name) VALUES (?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            LOGGER.log(Level.INFO, "Inserted item with name: " + name);
        }
    }

    public void insertDropSource(String name) throws SQLException {
        String SQL = "INSERT INTO drop_source (name) VALUES (?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            LOGGER.log(Level.INFO, "Inserted dropSource with name: " + name);
        }
    }

    public void insertLootDrops(String dropSourceName, Map<String, Float> itemToChance) throws SQLException {
        for (var entry : itemToChance.entrySet()) {
            insertLootDrop(dropSourceName, entry.getKey(), entry.getValue());
        }
    }

    public void insertLootDrop(String dropSourceName, String itemName, float chance) throws SQLException {
        String SQL = "CALL insert_loot_drop_with_names(?, ?, ?)";
        try (Connection conn = connect();
             CallableStatement cstmt = conn.prepareCall(SQL)) {
            cstmt.setString(1, dropSourceName);
            cstmt.setString(2, itemName);
            cstmt.setFloat(3, chance);
            cstmt.executeUpdate();
            LOGGER.log(Level.INFO, "Inserted chances for " + dropSourceName + " with item " + itemName + "and " + chance);
        }
    }

    public void insertPlayerProfile(String nickname) throws SQLException {
        String SQL = "INSERT INTO player_profile (nickname) VALUES (?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, nickname);
            pstmt.executeUpdate();
            LOGGER.log(Level.INFO, "Inserted player with name: " + nickname);
        }
    }

    public void insertMoneySource(String name, float moneyAmount) throws SQLException {
        String SQL = "INSERT INTO money_source (name, money_amount) VALUES (?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, name);
            pstmt.setFloat(2, moneyAmount);
            pstmt.executeUpdate();
            LOGGER.log(Level.INFO, String.format("Inserted money source with name %s and moneyAmount %f", name, moneyAmount));
        }
    }

    public void insertActionType(String name) throws SQLException {
        String SQL = "INSERT INTO action_type (name) VALUES (?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            LOGGER.log(Level.INFO, String.format("Inserted actionType with name: %s", name));
        }
    }

    public void insertPlayerMovement(String playerProfileName, float moveDistance) throws SQLException {
        String SQL = "CALL insert_player_movement_with_name(?, ?)";
        try (Connection conn = connect();
             CallableStatement cstmt = conn.prepareCall(SQL)) {
            cstmt.setString(1, playerProfileName);
            cstmt.setFloat(2, moveDistance);
            cstmt.executeUpdate();
            LOGGER.log(Level.INFO, String.format("Inserted moveMovement for player %s with distance %f", playerProfileName, moveDistance));
        }
    }

    public void insertPlayerAction(String playerProfileName, String actionTypeName) throws SQLException {
        String SQL = "CALL insert_player_action_with_name(?, ?)";
        try (Connection conn = connect();
             CallableStatement cstmt = conn.prepareCall(SQL)) {
            cstmt.setString(1, playerProfileName);
            cstmt.setString(2, actionTypeName);
            cstmt.executeUpdate();
            LOGGER.log(Level.INFO, String.format("Inserted playerAction for player %s with type %s", playerProfileName, actionTypeName));
        }
    }

    public void insertPlayerInventory(String playerProfileName, String itemName) throws SQLException {
        String SQL = "CALL insert_player_inventory_with_names(?, ?)";
        try (Connection conn = connect();
             CallableStatement cstmt = conn.prepareCall(SQL)) {
            cstmt.setString(1, playerProfileName);
            cstmt.setString(2, itemName);
            cstmt.executeUpdate();
            LOGGER.log(Level.INFO, String.format("Inserted item %s for player %s", playerProfileName, itemName));
        }
    }

    public void processMoneyTransaction(String playerProfileName, String moneySourceName) throws SQLException {
        String SQL = "CALL process_money_transaction(?, ?)";
        if (!checkPlayerMoneyAmount(playerProfileName, moneySourceName)) {
            return;
        }
        try (Connection conn = connect();
             CallableStatement cstmt = conn.prepareCall(SQL)) {
            cstmt.setString(1, playerProfileName);
            cstmt.setString(2, moneySourceName);
            cstmt.executeUpdate();
            LOGGER.log(Level.INFO, String.format("process moneyTransaction %s for player %s", moneySourceName, playerProfileName));
        }
    }

    public boolean checkPlayerMoneyAmount(String playerProfileName, String moneySourceName) throws SQLException {
        String SQL = "SELECT has_enough_money_names(?, ?)";
        try (Connection conn = connect();
             CallableStatement cstmt = conn.prepareCall(SQL)) {
            cstmt.setString(1, playerProfileName);
            cstmt.setString(2, moneySourceName);
            var resultSet = cstmt.executeQuery();
            resultSet.next();
            boolean result = resultSet.getBoolean(1);
            resultSet.close();
            return result;
        }
    }

    public Map<String, Float> findItemChance(String dropSource) throws SQLException {
        var mapItemToChance = new HashMap<String, Float>();
        String SQL = "SELECT * FROM select_drop_chances_with_name(?)";
        try (Connection conn = connect();
             CallableStatement cstmt = conn.prepareCall(SQL)) {
            cstmt.setString(1, dropSource);
            var resultSet = cstmt.executeQuery();
            while (resultSet.next()) {
                mapItemToChance.put(resultSet.getString(1), resultSet.getFloat(2));
            }
            resultSet.close();
            return mapItemToChance;
        }
    }

    public PlayerProfileTable findPlayerProfile(String name) throws SQLException {
        String SQL = "SELECT * FROM player_profile WHERE nickname = ?";
        try (Connection conn = connect();
             CallableStatement cstmt = conn.prepareCall(SQL)) {
            cstmt.setString(1, name);
            var playerResultSet = cstmt.executeQuery();
            if (!playerResultSet.next())
                return null;
            int id = playerResultSet.getInt(1);
            String nickname = playerResultSet.getString(2);
            LocalDateTime registerDate = playerResultSet.getTimestamp(3).toLocalDateTime();
            LocalDateTime lastLoginDate = playerResultSet.getTimestamp(4).toLocalDateTime();
            boolean banStatus = playerResultSet.getBoolean(5);
            float moneyAmount = playerResultSet.getFloat(6);
            return new PlayerProfileTable(id, nickname, registerDate, lastLoginDate, banStatus, moneyAmount);
        }
    }

    public List<PlayerInventoryTable> findPlayerItems(String playerName) throws SQLException {
        var itemList = new ArrayList<PlayerInventoryTable>();
        String SQL = "SELECT * FROM get_items_for_player(?)";
        try (Connection conn = connect();
             CallableStatement cstmt = conn.prepareCall(SQL)) {
            cstmt.setString(1, playerName);
            var resultSet = cstmt.executeQuery();
            while (resultSet.next()) {
                var id = resultSet.getInt(1);
                var itemName = resultSet.getString(2);
                itemList.add(new PlayerInventoryTable(id, itemName));
            }
            return itemList;
        }
    }
}
