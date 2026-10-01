package com.example.eventapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import java.util.List;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;
import android.widget.TextView;
import android.view.View;

public class DataDisplayActivity extends AppCompatActivity {
    private AppDatabase db;
    private InventoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        // 1. Initialize Database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "inventory-database")
                .allowMainThreadQueries().build();

        // 2. Setup RecyclerView FIRST
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        TextView emptyView = findViewById(R.id.emptyTextView);

        List<InventoryItem> items = db.inventoryDao().getAllItems();
        if (items.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }

        adapter = new InventoryAdapter(items, db);
        recyclerView.setAdapter(adapter);

        // 3. Handle Add Button
        Button addButton = findViewById(R.id.button);
        EditText nameInput = findViewById(R.id.editTextText);

        addButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            if (!name.isEmpty()) {
                InventoryItem newItem = new InventoryItem();
                newItem.itemName = name;
                db.inventoryDao().insert(newItem);

                List<InventoryItem> updatedList = db.inventoryDao().getAllItems();
                adapter.updateList(updatedList);

                Toast.makeText(this, "Item added!", Toast.LENGTH_SHORT).show();

            // Refresh the adapter
            adapter.updateList(db.inventoryDao().getAllItems());
            Toast.makeText(this, "Item added to inventory!", Toast.LENGTH_SHORT).show();
        }
            else {
            Toast.makeText(this, "Please enter an item name", Toast.LENGTH_SHORT).show();
        }
        });
    }
}