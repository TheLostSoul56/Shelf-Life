package com.example.shelflife;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
import androidx.appcompat.widget.SearchView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShelfFragment extends Fragment {
    private static final String TAG = "ShelfFragment";

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private MutableLiveData<List<Item>> itemListLiveData = new MutableLiveData<>(new ArrayList<>());
    private List<Item> fullItemList = new ArrayList<>();
    private SearchView searchBar;
    private FloatingActionButton addItemButton;
    private TextView emptyShelfText;

    public ShelfFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener(
                "addItemRequest", this, (requestKey, bundle) -> {
                    String newItemName = bundle.getString("newItemName");
                    String storeName = bundle.getString("storeName");
                    if (newItemName != null && !newItemName.isEmpty()){
                        addItem(new Item(newItemName, storeName));
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shelf, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.shelfRecyclerView);
        searchBar = view.findViewById(R.id.searchBar);
        addItemButton = view.findViewById(R.id.addItemButton);
        emptyShelfText = view.findViewById(R.id.emptyShelfText);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Observe data changes
        itemListLiveData.observe(getViewLifecycleOwner(), items -> {
            adapter.updateItems(items);
            // Show/hide empty state
            if (items.isEmpty()) {
                emptyShelfText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyShelfText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        // Set up search functionality
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterItem(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterItem(newText);
                return true;
            }
        });

        // Set up add button
        addItemButton.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, new AddItemFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Load data
        loadShelfItemsFromFirestore();

        return view;
    }

    private void addItem(Item item) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userID = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference shelfRef = db.collection("users").document(userID).collection("shelfItems");

        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("name", item.getName());
        itemMap.put("store", item.getStoreName());
        itemMap.put("expirationDate", item.getExpirationDate() != 0 ? new Date(item.getExpirationDate()) : null);
        itemMap.put("purchaseDate", item.getPurchaseDate() != 0 ? new Date(item.getPurchaseDate()) : null);

        shelfRef.add(itemMap).addOnSuccessListener(documentReference -> {
            Log.d(TAG, "Item added: " + documentReference.getId());
            item.setDocumentID(documentReference.getId());
        }).addOnFailureListener(e -> Log.e(TAG, "Error adding item", e));

        List<Item> currentList = itemListLiveData.getValue();
        if (currentList == null) {
            currentList = new ArrayList<>();
        }
        currentList.add(item);
        itemListLiveData.setValue(new ArrayList<>(currentList));
    }

    public void deleteSelectedItems() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userID = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference shelfRef = db.collection("users").document(userID).collection("shelfItems");

        List<Item> currentList = itemListLiveData.getValue();
        if (currentList != null) {
            List<Item> updatedList = new ArrayList<>();
            for (Item item : currentList) {
                if (!item.isSelected()) {
                    updatedList.add(item);
                } else {
                    String docID = item.getDocumentID();
                    if (docID != null) {
                        shelfRef.document(docID).delete()
                                .addOnCompleteListener(aVoid -> Log.d(TAG, "Item deleted"))
                                .addOnFailureListener(e -> Log.e(TAG, "Delete failed", e));
                    }
                }
            }
            itemListLiveData.setValue(updatedList);
        }
    }

    // Item class
    public static class Item implements Serializable {
        private String name;
        private String store;
        private boolean isSelected;
        private String documentID;
        private long expirationDate;
        private long purchaseDate;

        public Item(String name, String store) {
            this.name = name;
            this.store = store;
            this.isSelected = false;
            this.expirationDate = 0;
            this.purchaseDate = 0;
        }

        public Item(String name, String store, String documentID) {
            this.name = name;
            this.store = store;
            this.documentID = documentID;
            this.isSelected = false;
            this.expirationDate = 0;
            this.purchaseDate = 0;
        }

        public Item(String name, String store, String documentID, long expirationDate, long purchaseDate) {
            this.name = name;
            this.store = store;
            this.documentID = documentID;
            this.isSelected = false;
            this.expirationDate = expirationDate;
            this.purchaseDate = purchaseDate;
        }

        // Getters and setters
        public String getDocumentID() {
            return documentID;
        }

        public void setDocumentID(String documentID) {
            this.documentID = documentID;
        }

        public String getName() {
            return name;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public String getStoreName() {
            return store;
        }

        public long getExpirationDate() {
            return expirationDate;
        }

        public void setExpirationDate(long expirationDate) {
            this.expirationDate = expirationDate;
        }

        public long getPurchaseDate() {
            return purchaseDate;
        }

        public void setPurchaseDate(long purchaseDate) {
            this.purchaseDate = purchaseDate;
        }
    }

    // ItemAdapter class
    public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
        private List<Item> itemList;

        public ItemAdapter(List<Item> itemList) {
            this.itemList = itemList;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_layout, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            Item item = itemList.get(position);
            holder.nameTextView.setText(item.getName());
            holder.checkBox.setChecked(item.isSelected());
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setSelected(isChecked);
            });
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public void updateItems(List<Item> newItems) {
            this.itemList.clear();
            this.itemList.addAll(newItems);
            notifyDataSetChanged();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder {
            TextView nameTextView;
            CheckBox checkBox;

            public ItemViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.itemName);
                checkBox = itemView.findViewById(R.id.itemCheckbox);
            }
        }
    }

    private void loadShelfItemsFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        String userID = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference shelfRef = db.collection("users").document(userID).collection("shelfItems");

        shelfRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Item> loadedItems = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()) {
                    String name = doc.getString("name");
                    String store = doc.getString("store");
                    if (name != null && store != null) {
                        String docID = doc.getId();
                        Date expDate = doc.getDate("expirationDate");
                        Date purchDate = doc.getDate("purchaseDate");

                        long expTime = expDate != null ? expDate.getTime() : 0;
                        long purchTime = purchDate != null ? purchDate.getTime() : 0;

                        loadedItems.add(new Item(name, store, docID, expTime, purchTime));
                    }
                }
                fullItemList = new ArrayList<>(loadedItems);
                itemListLiveData.setValue(loadedItems);
            } else {
                Log.e(TAG, "Failed to load shelf items", task.getException());
            }
        });
    }

    public void filterItem(String query) {
        if (query == null || query.trim().isEmpty()) {
            itemListLiveData.setValue(new ArrayList<>(fullItemList));
            return;
        }

        List<Item> filteredItems = new ArrayList<>();
        for (Item item : fullItemList) {
            if (item.getName().toLowerCase().contains(query.toLowerCase()) ||
                    item.getStoreName().toLowerCase().contains(query.toLowerCase())) {
                filteredItems.add(item);
            }
        }
        itemListLiveData.setValue(filteredItems);
    }
}
