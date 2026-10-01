package com.example.eventapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private List<InventoryItem> itemList;
    private AppDatabase db;

    public InventoryAdapter(List<InventoryItem> itemList, AppDatabase db) {
        this.itemList = itemList;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem currentItem = itemList.get(position);
        holder.itemName.setText(currentItem.itemName);

        // Delete functionality
        holder.deleteButton.setOnClickListener(v -> {
            db.inventoryDao().delete(currentItem);
            itemList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, itemList.size());
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateList(List<InventoryItem> newList) {
        this.itemList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemName;
        public ImageButton deleteButton;

        public ViewHolder(View view) {
            super(view);
            itemName = view.findViewById(R.id.textView2);
            deleteButton = view.findViewById(R.id.imageButton); // Ensure this ID matches grid_item.xml
        }
    }
}