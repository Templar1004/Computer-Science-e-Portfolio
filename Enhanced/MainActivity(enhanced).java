package com.example.eventapp;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    // Executor for handling background tasks securely off the main thread
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize the thread executor
        executorService = Executors.newSingleThreadExecutor();

        // 1. Initialize the database safely WITHOUT allowMainThreadQueries()
        db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class, "inventory-database")
                .build();

        // 2. Perform database insertion on a background thread
        executorService.execute(() -> {
            InventoryItem newItem = new InventoryItem();
            newItem.itemName = "Safety Goggles";
            newItem.quantity = 50;
            db.inventoryDao().insert(newItem);
        });

        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            // This 'Intent' tells Android to open DataDisplayActivity
            Intent intent = new Intent(MainActivity.this, DataDisplayActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up the executor to prevent memory leaks
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}