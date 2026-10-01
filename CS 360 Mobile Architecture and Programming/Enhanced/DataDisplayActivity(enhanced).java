package com.example.eventapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class DataDisplayActivity extends AppCompatActivity {
    private AppDatabase db;
    private InventoryAdapter adapter;
    private ExecutorService executorService;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        // Initialize executor for async DB operations
        executorService = Executors.newSingleThreadExecutor();

        // 1. Initialize Database WITHOUT allowMainThreadQueries()
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "inventory-database")
                .build();

        // 2. Setup RecyclerView FIRST
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        emptyView = findViewById(R.id.emptyTextView);

        // Set up the adapter with an empty list initially
        adapter = new InventoryAdapter(new ArrayList<>(), db);
        recyclerView.setAdapter(adapter);

        // Fetch data asynchronously
        loadDatabaseItems();

        // 3. Handle Add Button
        Button addButton = findViewById(R.id.button);
        EditText nameInput = findViewById(R.id.editTextText);

        addButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();

            // --- DEFENSIVE PROGRAMMING: Input Validation & Sanitization ---

            // Check for empty inputs
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter an item name.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check for buffer overflow / excessive string lengths
            if (name.length() > 50) {
                Toast.makeText(this, "Item name is too long. Max 50 characters.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sanitize input to only allow alphanumeric characters and spaces
            if (!Pattern.matches("^[a-zA-Z0-9 ]+$", name)) {
                Toast.makeText(this, "Invalid characters detected. Please use only letters and numbers.", Toast.LENGTH_SHORT).show();
                return;
            }
            // --------------------------------------------------------------

            // Execute database write on a background thread
            executorService.execute(() -> {
                InventoryItem newItem = new InventoryItem();
                newItem.itemName = name;

                try {
                    db.inventoryDao().insert(newItem);

                    // Update UI elements securely on the Main Thread
                    runOnUiThread(() -> {
                        Toast.makeText(DataDisplayActivity.this, "Item successfully added!", Toast.LENGTH_SHORT).show();
                        nameInput.setText(""); // Clear the input field
                        loadDatabaseItems();   // Refresh the Recycler View list
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(DataDisplayActivity.this, "Error saving to database.", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
    }

    /**
     * Helper method to load database items asynchronously
     * and update the RecyclerView on the main UI thread.
     */
    private void loadDatabaseItems() {
        executorService.execute(() -> {
            List<InventoryItem> items = db.inventoryDao().getAllItems();

            runOnUiThread(() -> {
                if (items.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.GONE);
                }
                // Single, clean adapter update
                adapter.updateList(items);
            });
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