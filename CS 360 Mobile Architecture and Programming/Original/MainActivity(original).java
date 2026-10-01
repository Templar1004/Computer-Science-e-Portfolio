package com.example.eventapp;

import android.os.Bundle;
import java.util.List; // ADD THIS IMPORT
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 1. Initialize the database FIRST
        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "inventory-database")
                .allowMainThreadQueries()
                .build();

        // 2. NOW perform your operations
        InventoryItem newItem = new InventoryItem();
        newItem.itemName = "Safety Goggles";
        newItem.quantity = 50;
        db.inventoryDao().insert(newItem);

        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            // This 'Intent' tells Android to open DataDisplayActivity
            Intent intent = new Intent(MainActivity.this, DataDisplayActivity.class);
            startActivity(intent);
        });

        List<InventoryItem> items = db.inventoryDao().getAllItems();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}