package com.example.shelflife;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.bundle.BundleElement;

import org.jetbrains.annotations.NonNls;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShelfFragment extends Fragment {
private RecyclerView recyclerView;
private ItemAdapter adapter;
private MutableLiveData<List<Item>> itemListLiveData = new MutableLiveData<>(new ArrayList<>());
private List<Item> fullItemList = new ArrayList<>();


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
                        //Item newItem = new Item(newItemName, storeName);
                        addItem(new Item(newItemName, storeName));
                    }
                }
        );
    }
// within the onCreateView i have put onClickListeners to add items to the list when the add to shelf
    // button is clicked and when the check box it checked and hit delete it will delete only the selected
    //items this also holds the recycle view that allows the user to scroll through the list
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loadShelfItemsFromFirestore();
        View view = inflater.inflate(R.layout.fragment_shelf,container, false);

        recyclerView= view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        itemListLiveData.observe(getViewLifecycleOwner(), items -> adapter.updateItems(items));

        Button addToShelf = view.findViewById(R.id.addToShelf);
        Button addToList = view.findViewById(R.id.addToList);
        Button delete = view.findViewById(R.id.delete);

        addToShelf.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, new AddItemFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // event listener for add to list button
        addToList.setOnClickListener(v -> {
            List<Item> selectedItems = new ArrayList<>();
            List<Item> currentItems = itemListLiveData.getValue();

            if (currentItems != null){
                for (Item item : currentItems){
                    if (item.isSelected()){
                        selectedItems.add(item);
                    }
                }
                if (!selectedItems.isEmpty()){
                    Bundle result = new Bundle();
                    result.putSerializable("selectedItems", new ArrayList<>(selectedItems));
                    getParentFragmentManager().setFragmentResult("itemsForList", result);
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null){
                        String userId = currentUser.getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        CollectionReference shelfRef = db.collection("users").document(userId).collection("shelfItems");

                        for (Item item : selectedItems) {
                            String docId = item.getDocumentID();
                            if (docId != null) {
                                shelfRef.document(docId).delete().addOnCompleteListener(aVoid -> Log.d("Firestore", "Item moved & deleted: " + docId))
                                        .addOnFailureListener(e -> Log.e("Firestore", "Failed to delete item: " + docId, e));
                            }
                        }
                    }
                    currentItems.removeAll(selectedItems);
                    itemListLiveData.setValue(currentItems);
                }
            }
        });
        delete.setOnClickListener(v -> deleteSelectedItems());

        return view;
    }

    private void addItem (Item item) {
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

        shelfRef.add(itemMap).addOnSuccessListener(documentReference -> Log.d("FireStore", "Item added: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.d("FireStore", "Error adding item", e));


        List<Item> currentList = itemListLiveData.getValue();
        if (currentList == null){
            currentList = new ArrayList<>();
        }
        currentList.add(item);
        itemListLiveData.setValue(new ArrayList<>(currentList));

    }

    public void deleteSelectedItems(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
             return;
        }

        String userID = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference shelfRef = db.collection("users").document(userID).collection("shelfItems");

        List<Item> currentList = itemListLiveData.getValue();
        if (currentList != null){
            List<Item> updatedList = new ArrayList<>();
            for (Item item : currentList){
                if (!item.isSelected()){
                    updatedList.add(item);
                }else {
                    String docID = item.getDocumentID();
                    if (docID != null) {
                        shelfRef.document(docID).delete().addOnCompleteListener(aVoid -> Log.d("Firestore", "Item deleted"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Delete failed, e"));
                    }
                }
            }
            itemListLiveData.setValue(updatedList);
        }
    }
// items class
   public static class Item {
        private String name;
        private String store;
        private boolean isSelected;
        private String documentID;

        public Item(String name, String store) {
            this.name = name;
            this.store = store;
            this.isSelected = false;
        }

        public Item(String name, String store, String documentID) {
            this.name = name;
            this.store = store;
            this.documentID = documentID;
            this.isSelected = false;
        }

        public String getDocumentID(){
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

        public String getStoreName(){
            return store;
        }
   }
// the item adapter that will track the position of each item so when deleted is clicked
    // it will delete that one position and move everything up
   public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder>{
        private List<Item> itemList;

        public ItemAdapter(List<Item> itemList){
            this.itemList = itemList;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_layout, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
       public void onBindViewHolder(ItemViewHolder holder, int position){
            Item item = itemList.get(position);
            holder.nameTextView.setText(item.getName());
            holder.checkBox.setChecked(item.isSelected());
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked)-> {
                item.setSelected(isChecked);
            });
        }

        @Override
       public int getItemCount(){
            return itemList.size();
        }


        public void updateItems(List<Item> newItems){
            this.itemList.clear();
            this.itemList.addAll(newItems);
            notifyDataSetChanged();
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder{
            TextView nameTextView;
            CheckBox checkBox;

            public ItemViewHolder(View itemView){
                super(itemView);
                nameTextView = itemView.findViewById(R.id.itemName);
                checkBox = itemView.findViewById(R.id.itemCheckbox);
            }
        }
   }

   private void loadShelfItemsFromFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null){
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
                        loadedItems.add(new Item(name, store, docID));
                    }
                }
                fullItemList = new ArrayList<>(loadedItems);
                itemListLiveData.setValue(loadedItems);
            }else {
                Log.e("Firestore", "Failed to load shelf items", task.getException());
            }
        });
   }

  public void filterItem(String query) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
       if (currentUser == null) {
           return;
       }
       String userID = currentUser.getUid();
       FirebaseFirestore db = FirebaseFirestore.getInstance();
       CollectionReference shelfRef = db.collection("users").document(userID).collection("shelfItems");


        if (query == null || query.trim().isEmpty()){
            loadShelfItemsFromFirestore();
            return;
        }

        shelfRef.orderBy("name").startAt(query.toLowerCase()).endAt(query.toLowerCase() + "\uf8ff").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Item> filteredItems = new ArrayList<>();
                for (DocumentSnapshot doc : task.getResult()){
                    String name = doc.getString("name");
                    String store = doc.getString("store");
                    if (name != null && store != null && name.toLowerCase().contains(query.toLowerCase())) {
                        filteredItems.add(new Item(name,store,doc.getId()));
                    }
                }
                adapter.updateItems(filteredItems);
            }
        });
   }

}