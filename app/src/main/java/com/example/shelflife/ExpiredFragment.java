package com.example.shelflife;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpiredFragment extends Fragment {
    private static final String TAG = "ExpiredFragment";

    private RecyclerView recyclerView;
    private ExpiredItemAdapter adapter;
    private MutableLiveData<List<ExpiredItem>> expiredItemsLiveData = new MutableLiveData<>(new ArrayList<>());
    private Button deleteSelectedBtn, restoreSelectedBtn;
    private TextView emptyStateText;

    public ExpiredFragment() {
        // Required empty public constructor
    }

    public static ExpiredFragment newInstance() {
        return new ExpiredFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expired, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.expiredRecyclerView);
        deleteSelectedBtn = view.findViewById(R.id.deleteExpiredBtn);
        restoreSelectedBtn = view.findViewById(R.id.restoreExpiredBtn);
        emptyStateText = view.findViewById(R.id.emptyExpiredText);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExpiredItemAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Observe changes to expired items list
        expiredItemsLiveData.observe(getViewLifecycleOwner(), expiredItems -> {
            adapter.updateItems(expiredItems);
            // Show/hide empty state message
            if (expiredItems.isEmpty()) {
                emptyStateText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        // Load expired items
        loadExpiredItems();

        // Set up button click listeners
        deleteSelectedBtn.setOnClickListener(v -> deleteSelectedItems());
        restoreSelectedBtn.setOnClickListener(v -> restoreSelectedItems());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when fragment becomes visible
        loadExpiredItems();
    }

    private void loadExpiredItems() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a reference to the "expiredItems" collection for this user
        CollectionReference expiredItemsRef = db.collection("users")
                .document(userId)
                .collection("expiredItems");

        // Query expired items, sorted by expiration date (most recently expired first)
        expiredItemsRef.orderBy("expirationDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ExpiredItem> expiredItems = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        String store = document.getString("store");
                        Date expirationDate = document.getDate("expirationDate");
                        Date purchaseDate = document.getDate("purchaseDate");
                        if (name != null) {
                            ExpiredItem item = new ExpiredItem(
                                    document.getId(),
                                    name,
                                    store != null ? store : "",
                                    expirationDate,
                                    purchaseDate
                            );
                            expiredItems.add(item);
                        }
                    }
                    expiredItemsLiveData.setValue(expiredItems);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load expired items", e);
                    Toast.makeText(getContext(), "Failed to load expired items", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteSelectedItems() {
        List<ExpiredItem> selectedItems = getSelectedItems();
        if (selectedItems.isEmpty()) {
            Toast.makeText(getContext(), "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference expiredItemsRef = db.collection("users")
                .document(userId)
                .collection("expiredItems");

        // Delete each selected item
        for (ExpiredItem item : selectedItems) {
            expiredItemsRef.document(item.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Item deleted: " + item.getName());
                        // Remove from local list after successful deletion
                        List<ExpiredItem> currentItems = expiredItemsLiveData.getValue();
                        if (currentItems != null) {
                            currentItems.remove(item);
                            expiredItemsLiveData.setValue(new ArrayList<>(currentItems));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to delete item: " + item.getName(), e);
                        Toast.makeText(getContext(), "Failed to delete item: " + item.getName(), Toast.LENGTH_SHORT).show();
                    });
        }
        Toast.makeText(getContext(), "Deleting selected items...", Toast.LENGTH_SHORT).show();
    }

    private void restoreSelectedItems() {
        List<ExpiredItem> selectedItems = getSelectedItems();
        if (selectedItems.isEmpty()) {
            Toast.makeText(getContext(), "No items selected", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference shelfItemsRef = db.collection("users")
                .document(userId)
                .collection("shelfItems");
        CollectionReference expiredItemsRef = db.collection("users")
                .document(userId)
                .collection("expiredItems");

        // Restore each selected item to shelf
        for (ExpiredItem item : selectedItems) {
            // Create map for shelf item
            Map<String, Object> shelfItem = new HashMap<>();
            shelfItem.put("name", item.getName());
            shelfItem.put("store", item.getStore());
            if (item.getExpirationDate() != null) {
                shelfItem.put("expirationDate", item.getExpirationDate());
            }
            if (item.getPurchaseDate() != null) {
                shelfItem.put("purchaseDate", item.getPurchaseDate());
            }

            // Add to shelf collection
            shelfItemsRef.add(shelfItem)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Item restored to shelf: " + item.getName());
                        // Delete from expired items after successful restoration
                        expiredItemsRef.document(item.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Item removed from expired: " + item.getName());
                                    // Remove from local list
                                    List<ExpiredItem> currentItems = expiredItemsLiveData.getValue();
                                    if (currentItems != null) {
                                        currentItems.remove(item);
                                        expiredItemsLiveData.setValue(new ArrayList<>(currentItems));
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to restore item: " + item.getName(), e);
                        Toast.makeText(getContext(), "Failed to restore item: " + item.getName(), Toast.LENGTH_SHORT).show();
                    });
        }
        Toast.makeText(getContext(), "Restoring selected items to shelf...", Toast.LENGTH_SHORT).show();
    }

    private List<ExpiredItem> getSelectedItems() {
        List<ExpiredItem> selectedItems = new ArrayList<>();
        List<ExpiredItem> allItems = expiredItemsLiveData.getValue();
        if (allItems != null) {
            for (ExpiredItem item : allItems) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                }
            }
        }
        return selectedItems;
    }

    // ExpiredItem class to represent an expired item
    public static class ExpiredItem {
        private String id;
        private String name;
        private String store;
        private Date expirationDate;
        private Date purchaseDate;
        private boolean selected;

        public ExpiredItem() {
            // Required empty constructor for Firestore
        }

        public ExpiredItem(String id, String name, String store, Date expirationDate, Date purchaseDate) {
            this.id = id;
            this.name = name;
            this.store = store;
            this.expirationDate = expirationDate;
            this.purchaseDate = purchaseDate;
            this.selected = false;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getStore() {
            return store;
        }

        public Date getExpirationDate() {
            return expirationDate;
        }

        public Date getPurchaseDate() {
            return purchaseDate;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        // Calculate days since expiration
        public int getDaysSinceExpiration() {
            if (expirationDate == null) {
                return 0;
            }
            long diffInMillis = System.currentTimeMillis() - expirationDate.getTime();
            return (int) (diffInMillis / (1000 * 60 * 60 * 24));
        }
    }

    // Adapter for expired items
    private class ExpiredItemAdapter extends RecyclerView.Adapter<ExpiredItemAdapter.ExpiredItemViewHolder> {
        private List<ExpiredItem> items;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        public ExpiredItemAdapter(List<ExpiredItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ExpiredItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_layout, parent, false);
            return new ExpiredItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ExpiredItemViewHolder holder, int position) {
            ExpiredItem item = items.get(position);
            holder.nameTextView.setText(item.getName());

            if (item.getStore() != null && !item.getStore().isEmpty()) {
                holder.storeTextView.setText("Store: " + item.getStore());
                holder.storeTextView.setVisibility(View.VISIBLE);
            } else {
                holder.storeTextView.setVisibility(View.GONE);
            }

            if (item.getExpirationDate() != null) {
                holder.expirationTextView.setText("Expired: " + dateFormat.format(item.getExpirationDate()));
                // Set text color based on how long it's been expired
                int daysSinceExpiration = item.getDaysSinceExpiration();
                if (daysSinceExpiration > 30) {
                    holder.expirationTextView.setTextColor(Color.RED);
                } else if (daysSinceExpiration > 14) {
                    holder.expirationTextView.setTextColor(Color.rgb(255, 165, 0)); // Orange
                } else {
                    holder.expirationTextView.setTextColor(Color.rgb(255, 215, 0)); // Gold
                }
                holder.expirationTextView.setVisibility(View.VISIBLE);
            } else {
                holder.expirationTextView.setVisibility(View.GONE);
            }

            if (item.getPurchaseDate() != null) {
                holder.purchaseDateTextView.setText("Purchased: " + dateFormat.format(item.getPurchaseDate()));
                holder.purchaseDateTextView.setVisibility(View.VISIBLE);
            } else {
                holder.purchaseDateTextView.setVisibility(View.GONE);
            }

            holder.checkBox.setChecked(item.isSelected());
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setSelected(isChecked);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public void updateItems(List<ExpiredItem> newItems) {
            this.items = newItems;
            notifyDataSetChanged();
        }

        class ExpiredItemViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView, storeTextView, expirationTextView, purchaseDateTextView;
            CheckBox checkBox;

            public ExpiredItemViewHolder(@NonNull View itemView) {
                super(itemView);
                try {
                    nameTextView = itemView.findViewById(R.id.expiredItemName);
                    storeTextView = itemView.findViewById(R.id.expiredItemStore);
                    expirationTextView = itemView.findViewById(R.id.expiredItemExpirationDate);
                    purchaseDateTextView = itemView.findViewById(R.id.expiredItemPurchaseDate);
                    checkBox = itemView.findViewById(R.id.expiredItemCheckbox);

                    // Make the entire item clickable to toggle checkbox
                    itemView.setOnClickListener(v -> {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            ExpiredItem item = items.get(position);
                            item.setSelected(!item.isSelected());
                            checkBox.setChecked(item.isSelected());
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error finding views in ExpiredItemViewHolder: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    // Static method to move item from shelf to expired
    public static void moveItemToExpired(ShelfFragment.Item item) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a map for the expired item
        Map<String, Object> expiredItem = new HashMap<>();
        expiredItem.put("name", item.getName());
        expiredItem.put("store", item.getStoreName());

        // Add current date as expiration date if not present
        if (item.getExpirationDate() != 0) {
            expiredItem.put("expirationDate", new Date(item.getExpirationDate()));
        } else {
            expiredItem.put("expirationDate", new Date());
        }

        // Add purchase date if available
        if (item.getPurchaseDate() != 0) {
            expiredItem.put("purchaseDate", new Date(item.getPurchaseDate()));
        }

        // Add to expired items collection
        db.collection("users")
                .document(userId)
                .collection("expiredItems")
                .add(expiredItem)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Item moved to expired: " + item.getName());

                    // Remove from shelf items collection
                    if (item.getDocumentID() != null) {
                        db.collection("users")
                                .document(userId)
                                .collection("shelfItems")
                                .document(item.getDocumentID())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Item removed from shelf: " + item.getName());
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error removing item from shelf", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error moving item to expired", e);
                });
    }
}

