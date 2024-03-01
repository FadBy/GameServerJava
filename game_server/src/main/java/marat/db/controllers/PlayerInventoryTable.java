package marat.db.controllers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PlayerInventoryTable {
    private final int id;
    private final String itemName;
}
