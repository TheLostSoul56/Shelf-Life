package com.example.shelflife;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class FirebaseService {
    private static final String TAG = "FirebaseService";
    private static final String ITEMS_COLLECTION = "items";
    private static final String RECEIPTS_COLLECTION = "receipts";

    private FirebaseFirestore db;

    public FirebaseService() {
        db = FirebaseFirestore.getInstance();
    }

    // Save item to Firestore
    public void saveItem(ShelfFragment.Item item) {
        // Generate document ID since Item doesn't have getId() method
        String itemId = item.getName().replaceAll("\\s+", "_").toLowerCase() + "_" + System.currentTimeMillis();

        db.collection(ITEMS_COLLECTION)
                .document(itemId)
                .set(item)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Item saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error saving item", e));
    }

    // Get all items from Firestore
    public void getAllItems(MutableLiveData<List<ShelfFragment.Item>> itemListLiveData) {
        db.collection(ITEMS_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ShelfFragment.Item> items = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ShelfFragment.Item item = document.toObject(ShelfFragment.Item.class);
                            // Set document ID since it's not part of the Item object
                            if (item != null) {
                                item.setDocumentID(document.getId());
                            }
                            items.add(item);
                        }
                        itemListLiveData.setValue(items);
                    } else {
                        Log.e(TAG, "Error getting items", task.getException());
                    }
                });
    }

    // Delete item from Firestore
    public void deleteItem(String itemId) {
        db.collection(ITEMS_COLLECTION)
                .document(itemId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Item deleted successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting item", e));
    }

    // Move expired items to receipts
    public void moveExpiredItemsToReceipts(MutableLiveData<List<ShelfFragment.Item>> itemListLiveData) {
        db.collection(ITEMS_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ShelfFragment.Item> allItems = new ArrayList<>();
                        List<ShelfFragment.Item> expiredItems = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ShelfFragment.Item item = document.toObject(ShelfFragment.Item.class);
                            if (item != null) {
                                item.setDocumentID(document.getId());
                                allItems.add(item);

                                // Check if item is expired - since isExpired() doesn't exist
                                // We'll check based on a field in Firestore or use current time as fallback
                                Long expirationTimestamp = document.getLong("expirationDate");
                                boolean isExpired = false;

                                if (expirationTimestamp != null) {
                                    isExpired = System.currentTimeMillis() > expirationTimestamp;
                                } else {
                                    // If no expiration date in document, check if it's older than 7 days
                                    Date creationDate = document.getDate("createdAt");
                                    if (creationDate != null) {
                                        long weekInMillis = 7 * 24 * 60 * 60 * 1000;
                                        isExpired = System.currentTimeMillis() - creationDate.getTime() > weekInMillis;
                                    }
                                }

                                if (isExpired) {
                                    expiredItems.add(item);
                                    // Delete expired item using document ID
                                    deleteItem(document.getId());
                                }
                            }
                        }

                        // Update the LiveData with non-expired items
                        List<ShelfFragment.Item> nonExpiredItems = new ArrayList<>(allItems);
                        nonExpiredItems.removeAll(expiredItems);
                        itemListLiveData.setValue(nonExpiredItems);

                        // Create a receipt with expired items if any
                        if (!expiredItems.isEmpty()) {
                            createReceipt(expiredItems);
                        }
                    } else {
                        Log.e(TAG, "Error checking for expired items", task.getException());
                    }
                });
    }

    // Create a receipt in Firestore
    private void createReceipt(List<ShelfFragment.Item> expiredItems) {
        Receipt receipt = new Receipt(expiredItems);
        // Use the ID from Receipt class if it exists, otherwise generate one
        String receiptId = receipt.getId();

        db.collection(RECEIPTS_COLLECTION)
                .document(receiptId)
                .set(receipt)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Receipt created successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error creating receipt", e));
    }

    // Get all receipts from Firestore
    public void getAllReceipts(MutableLiveData<List<Receipt>> receiptListLiveData) {
        db.collection(RECEIPTS_COLLECTION)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Receipt> receipts = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Receipt receipt = document.toObject(Receipt.class);
                            receipts.add(receipt);
                        }
                        receiptListLiveData.setValue(receipts);
                    } else {
                        Log.e(TAG, "Error getting receipts", task.getException());
                    }
                });
    }
}
