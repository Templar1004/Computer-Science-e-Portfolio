package com.example.eventapp;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// This tells Room that this class is the database for InventoryItem entities
@Database(entities = {InventoryItem.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract InventoryDao inventoryDao();
}