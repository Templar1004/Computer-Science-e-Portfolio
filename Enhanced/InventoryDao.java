package com.example.eventapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface InventoryDao {
    @Query("SELECT * FROM inventory_items")
    List<InventoryItem> getAllItems();

    @Insert
    void insert(InventoryItem item);

    @Delete
    void delete(InventoryItem item);

    @Update
    void update(InventoryItem item);
}