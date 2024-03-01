package marat.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.logging.Level;


public class TestDataBase extends DataBase {
    private String url;
    private String user;
    private String password;

    private static final Logger LOGGER = Logger.getLogger(TestDataBase.class.getName());

    private final int itemCount = 20;
    private final int minStringLength = 5;
    private final int maxStringLength = 20;
    private final int dropSourceCount = 20;
    private final int minLootDropPerItem = 1;
    private final int maxLootDropPerItem = 3;
    private final int minItemsPerPlayer = 0;
    private final int maxItemsPerPlayer = 5;
    private final int playerProfileCount = 100;
    private final int minPlayerActionPerPlayer = 1;
    private final int maxPlayerActionPerPlayer = 10;
    private final int actionTypeCount = 20;
    private final int minPlayerMovementPerPlayer = 1;
    private final int maxPlayerMovementPerPlayer = 10;
    private final float minPlayerMovement = 1f;
    private final float maxPlayerMovement = 10f;
    private final int moneySourceCount = 20; 
    private final float minMoneySourceAmount = -100;
    private final float maxMoneySourceAmount = 100;
    private final int minPlayerMoneyTransactionPerPlayer = 1;
    private final int maxPlayerMoneyTransactionPerPlayer = 10;

    public TestDataBase(Properties properties) {
        super(properties);
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return;
        }

        readProperties();
        prepareDataBase();
//        run();
    }

    private void prepareDataBase() {
        executeSqlFile("sql/tables.sql");
        executeSqlFile("sql/triggers.sql");
        executeSqlFile("sql/functions.sql");
    }

    private void executeSqlFile(String filePath) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (input == null) {
                throw new IOException("Resource not found: " + filePath);
            }
    
            String sqlScript = new String(input.readAllBytes(), StandardCharsets.UTF_8);
    
            try (Connection conn = DriverManager.getConnection(url, user, password);
                 Statement stmt = conn.createStatement()) {
                stmt.execute(sqlScript);
            }
        } catch (SQLException e) {
            System.out.println("Error executing SQL file: " + filePath + "\n" + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error reading SQL file: " + filePath + "\n" + e.getMessage());
        }
    }
    


    public void run() {
        try {
            List<String> randomItemNames = getRandomUniqueStringList(itemCount, minStringLength, maxStringLength);
            for (String name : randomItemNames) {
                insertItem(name);
            }

            List<String> dropSourceNames = getRandomUniqueStringList(dropSourceCount, minStringLength, maxStringLength);
            for (String dropSource : dropSourceNames) {
                insertDropSource(dropSource);
            }

            for (String dropSource : dropSourceNames) {
                int lootDropCount = getRandomInt(minLootDropPerItem, maxLootDropPerItem);
                List<Float> chances = getRandomChances(lootDropCount);
                List<String> items = getRandomListElements(randomItemNames, lootDropCount);
                Map<String, Float> itemToChance = IntStream.range(0, lootDropCount)
                        .boxed()
                        .collect(Collectors.toMap(items::get, chances::get));
                insertLootDrops(dropSource, itemToChance);
            }

            List<String> actionTypes = getRandomUniqueStringList(actionTypeCount, minStringLength, maxStringLength);
            for (String actionTypeName : actionTypes) {
                insertActionType(actionTypeName);
            }

            List<String> moneySourceNames = getRandomUniqueStringList(moneySourceCount, minStringLength, maxStringLength);
            for (String moneySourceName : moneySourceNames) {
                float moneyAmount = getRandomFloat(minMoneySourceAmount, maxMoneySourceAmount);
                insertMoneySource(moneySourceName, moneyAmount);
            }

            List<String> playerNicknames = getRandomUniqueStringList(playerProfileCount, minStringLength, maxStringLength);
            for (String nickname : playerNicknames) {
                insertPlayerProfile(nickname);
                int itemCount = getRandomInt(minItemsPerPlayer, maxItemsPerPlayer);
                List<String> items = getRandomListElements(randomItemNames, itemCount);
                for (String item : items) {
                    insertPlayerInventory(nickname, item);
                }

                int actionCount = getRandomInt(minPlayerActionPerPlayer, maxPlayerActionPerPlayer);

                for (int i = 0; i < actionCount; i++) {
                    String actionType = getRandomListElement(actionTypes);
                    insertPlayerAction(nickname, actionType);
                }

                int moveCount = getRandomInt(minPlayerMovementPerPlayer, maxPlayerMovementPerPlayer);

                for (int i = 0; i < moveCount; i++) {
                    float moveDistance = getRandomFloat(minPlayerMovement, maxPlayerMovement);
                    insertPlayerMovement(nickname, moveDistance);
                }

                int moneyTransactions = getRandomInt(minPlayerMoneyTransactionPerPlayer, maxPlayerMoneyTransactionPerPlayer);

                for (int i = 0; i < moneyTransactions; i++) {
                    String moneySource = getRandomListElement(moneySourceNames);
                    processMoneyTransaction(nickname, moneySource);
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }
    }

    private void readProperties() {
        Properties prop = new Properties();
        try (InputStream input = TestDataBase.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                LOGGER.log(Level.SEVERE, "Unable to find config.properties");
                return;
            }

            prop.load(input);

            url = prop.getProperty("db.url");
            System.out.println(url);
            user = prop.getProperty("db.username");
            System.out.println(user);
            password = prop.getProperty("db.password");
            System.out.println(password);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Error reading config.properties: {0}", ex.getMessage());
        }
    }



    private <T> List<T> getRandomListElements(List<T> list, int count) {
        List<T> shuffledList = new ArrayList<>(list);
        Collections.shuffle(shuffledList);
        return shuffledList.subList(0, Math.min(count, shuffledList.size()));
    }

    private <T> T getRandomListElement(List<T> list) {
        return list.get(getRandomInt(0, list.size() - 1));
    }

    private int getRandomInt(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    private String getRandomString(int minLength, int maxLength) {
        int length = getRandomInt(minLength, maxLength);
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            result.append(characters.charAt(index));
        }
        return result.toString();
    }

    private float getRandomFloat(float min, float max) {
        return new Random().nextFloat() * (max - min) + min;
    }

    private List<String> getRandomUniqueStringList(int size, int minLength, int maxLength) {
        Set<String> uniqueStrings = new HashSet<>();
        while (uniqueStrings.size() < size) {
            uniqueStrings.add(getRandomString(minLength, maxLength));
        }
        return new ArrayList<>(uniqueStrings);
    }

    private List<Float> getRandomChances(int count) {
        List<Float> chances = new ArrayList<>();
        if (count == 1) {
            chances.add(100.0f);
            return chances;
        }
    
        List<Float> dividers = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < count - 1; i++) {
            dividers.add(random.nextFloat() * 100);
        }
    
        Collections.sort(dividers);
    
        float lastDivider = 0.0f;
        for (float divider : dividers) {
            chances.add(divider - lastDivider);
            lastDivider = divider;
        }
    
        chances.add(100.0f - lastDivider);
    
        return chances;
    }
}
