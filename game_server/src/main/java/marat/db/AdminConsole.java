package marat.db;

import marat.db.DataBase;

import java.sql.SQLException;
import java.util.Scanner;

public class AdminConsole implements Runnable {
    private DataBase dataBase; // Предполагается, что у вас есть класс для работы с базой данных

    public AdminConsole(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    @Override
    public void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String command = scanner.nextLine();
                if ("exit".equalsIgnoreCase(command)) {
                    break;
                }
                processCommand(command);
            }
        }
    }

    private void processCommand(String command) {
        if (command.startsWith("add_item")) {
            String itemName = command.split(" ")[1];
            try {
                dataBase.insertItem(itemName);
            } catch (Exception e) {
                System.err.println("Ошибка при добавлении item: " + e.getMessage());
            }
        } else if (command.startsWith("add_drop_source")) {
            String dropSourceName = command.split(" ")[1];
            try {
                dataBase.insertDropSource(dropSourceName);
            } catch (Exception e) {
                System.err.println("Ошибка при добавлении drop_source: " + e.getMessage());
            }
        } else if (command.startsWith("add_loot_drop")) {
            String dropSourceName = command.split(" ")[1];
            String itemName = command.split(" ")[2];
            float chance = Float.parseFloat(command.split(" ")[3]);
            try {
                dataBase.insertLootDrop(dropSourceName, itemName, chance);
            } catch (Exception e) {
                System.err.println("Ошибка при добавлении loot_drop: " + e.getMessage());
            }
        } else if (command.startsWith("add_money_source")) {
            String moneySourceName = command.split(" ")[1];
            float moneyAmount = Float.parseFloat(command.split(" ")[2]);
            try {
                dataBase.insertMoneySource(moneySourceName, moneyAmount);
            } catch (SQLException e) {
                System.err.println("Ошибка при добавлении money_source: " + e.getMessage());
            }
        } else if (command.startsWith("add_action_type")) {
            String actionTypeName = command.split(" ")[1];
            try {
                dataBase.insertActionType(actionTypeName);
            } catch (SQLException e) {
                System.err.println("Ошибка при добавлении action_type: " + e.getMessage());
            }
        } else if (command.startsWith("ban_player")) {
//            String playerName = command.split(" ")[1];
//            try {
//
//            }
        }
        else {
            System.out.println("Неизвестная команда");
        }
    }
}
