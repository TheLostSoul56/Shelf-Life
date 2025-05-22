package com.example.shelflife;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ListFragment extends Fragment {
    private List<Store> storeList = new ArrayList<>();
    private StoreAdapter adapter;
    private int selectedPosition = -1;
    public ListFragment() {
        // Required empty public constructor
    }
// set up the product class
class Product {
        String name;
        int imageID;

        Product(String name, int imageID){
            this.name = name;
            this.imageID=imageID;
        }
}
//set up the store class
class Store{
        String name;
        List<Product> products;
        boolean isExpanded;

        Store(String name, List<Product> products){
            this.name = name;
            this.products = products;
            this.isExpanded = false;
        }
}

@Override
public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
//this is a listener for when the add to list is added so when its clicked it will run through the current stores
    //then if found put the product into it if it isn't found then it will create a new drop down
        getParentFragmentManager().setFragmentResultListener(
                "itemsForList", this, (requestKey, bundle) -> {
                   ArrayList<ShelfFragment.Item> selectedItems =
                           (ArrayList<ShelfFragment.Item>) bundle.getSerializable("selectedItems");

                   if (selectedItems != null){
                       for (ShelfFragment.Item item : selectedItems){
                           String storeName = item.getStoreName();
                           String productName = item.getName();

                           Store matchedStore = null;
                           for (Store store : storeList){
                               if (store.name.equalsIgnoreCase(storeName)){
                                   matchedStore = store;
                                   break;
                               }
                           }
                           if (matchedStore == null) {
                               matchedStore = new Store(storeName, new ArrayList<>());
                               storeList.add(matchedStore);
                           }

                           matchedStore.products.add(new Product(productName, R.drawable.sample_food));
                       }

                       adapter.notifyDataSetChanged();
                       saveStores();
                       Toast.makeText(getContext(), "Items added to list", Toast.LENGTH_SHORT).show();
                   }
                });
}
//created the store adaptor to pretty much put everything together along with onclick listeners
    // so when the store is clicked it highlights it and u can choose to delete it
class StoreAdapter extends ArrayAdapter<Store>{
        StoreAdapter(Context context, List<Store> stores){
            super(context, 0, stores);
        }

        @Override
    public View getView(int position, View convertView, ViewGroup parent){
            Store store = getItem(position);

            if (convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_store, parent, false);
            }

            TextView storeName = convertView.findViewById(R.id.storeName);
            ImageView toggleIcon = convertView.findViewById(R.id.toggleIcon);
            LinearLayout productsContainer = convertView.findViewById(R.id.productsContainer);
            RelativeLayout header = convertView.findViewById(R.id.header);
            LinearLayout storeCard = convertView.findViewById(R.id.storeCard);

            storeName.setText(store.name);
            productsContainer.removeAllViews();

            for (Product product : store.products){
                View productView = LayoutInflater.from(getContext()).inflate(R.layout.item_product, productsContainer, false);
                TextView productName = productView.findViewById(R.id.productName);
                ImageView productImage = productView.findViewById(R.id.productImage);
                productName.setText(product.name);
                productImage.setImageResource(product.imageID);
                productsContainer.addView(productView);
            }

            productsContainer.setVisibility(store.isExpanded ? View.VISIBLE :View.GONE);
            toggleIcon.setImageResource(store.isExpanded ? R.drawable.ic_arrow_up : R.drawable.ic_arrow_down);

            if (position == selectedPosition) {
                convertView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.blue));
            }else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }

            storeCard.setOnClickListener(v -> {
                selectedPosition = position;
                notifyDataSetChanged();
                Toast.makeText(getContext(), "Selected " + store.name, Toast.LENGTH_SHORT).show();
            });

            header.setOnClickListener( v -> {
                store.isExpanded = !store.isExpanded;
                notifyDataSetChanged();
            });
            return convertView;
        }
}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,@Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadStores();

        adapter = new StoreAdapter(getContext(), storeList);
        ListView listView = view.findViewById(R.id.list);
        listView.setAdapter(adapter);
        Button addStoreButton = view.findViewById(R.id.addStoreButton);
        Button deleteStoreButton = view.findViewById(R.id.deleteStoreButton);
//here is the onclick for when the delete button is pressed
        deleteStoreButton.setOnClickListener(v -> {
            if (selectedPosition != -1 && selectedPosition < storeList.size()) {
                new AlertDialog.Builder(getContext()).setTitle("Delete Store")
                        .setMessage("Are you sure you want to delete " + storeList.get(selectedPosition).name + "?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            storeList.remove(selectedPosition);
                            adapter.notifyDataSetChanged();
                            saveStores();
                            selectedPosition = -1;
                            Toast.makeText(getContext(), "Store deleted", Toast.LENGTH_SHORT).show();
                        }).setNegativeButton("Cancel", null).show();
            }else {
                Toast.makeText(getContext(), "No Store selected", Toast.LENGTH_SHORT).show();
            }
        });
//here is the onclick for when the add store button is clicked
        addStoreButton.setOnClickListener(v -> {addNewStore(); saveStores();});
    }
//add new store method and is linked to the add store button and it add some sample products to the drop down
    private void addNewStore(){
        Context context = getContext();
        EditText input = new EditText(context);
        input.setHint("Enter Store Name");

        new AlertDialog.Builder(context).setTitle("Add New Store").setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String storeName = input.getText().toString().trim();
                    if (storeName.isEmpty()){
                        storeName = "Store" + (storeList.size() + 1);
                    }
                    List<Product> sampleProducts = new ArrayList<>();
                    sampleProducts.add(new Product("Apple", R.drawable.sample_food));
                    sampleProducts.add(new Product("Banana", R.drawable.sample_food_banana));
                    Store newStore = new Store(storeName, sampleProducts);
                    storeList.add(newStore);
                    adapter.notifyDataSetChanged();
                    saveStores();
                }).setNegativeButton("Cancel", null).show();


    }
//set up a save stores method to save the input stores so they don't delete when you change the screen
    private void saveStores(){
        SharedPreferences prefs = getActivity().getSharedPreferences("store_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(storeList);
        editor.putString("store_list", json);
        editor.apply();
    }
//set up a method to load all of the saved stores when the app is loaded or after the user changes screens
    private void loadStores(){
        SharedPreferences prefs = getActivity().getSharedPreferences("store_data", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("store_list", null);
        Type type = new TypeToken<ArrayList<Store>>(){}.getType();
        List<Store> loadedList = gson.fromJson(json, type);
        if (loadedList != null){
            storeList.clear();
            storeList.addAll(loadedList);
        }
    }
}