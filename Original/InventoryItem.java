package com.example.eventapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory_items")
public class InventoryItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String itemName;
    public int quantity;
}