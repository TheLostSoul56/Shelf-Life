package com.example.shelflife;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ShelfFragment extends Fragment {
private RecyclerView recyclerView;
private ItemAdapter adapter;
private MutableLiveData<List<Item>> itemListLiveData = new MutableLiveData<>(new ArrayList<>());


    public ShelfFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getParentFragmentManager().setFragmentResultListener(
                "addItemRequest", this, (requestKey, bundle) -> {
                    String newItemName = bundle.getString("newItemName");
                    if (newItemName != null && !newItemName.isEmpty()){
                        Item newItem = new Item(newItemName);
                        addItem(newItem);
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
        View view = inflater.inflate(R.layout.fragment_shelf,container, false);

        recyclerView= view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        itemListLiveData.observe(getViewLifecycleOwner(), items -> adapter.updateItems(items));

        Button addToShelf = view.findViewById(R.id.addToShelf);
        //Button addToList = view.findViewById(R.id.addToList);
        Button delete = view.findViewById(R.id.delete);

        addToShelf.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, new AddItemFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
        //addToShelf.setOnClickListener(v -> sendSelectedItemsToList());
        delete.setOnClickListener(v -> deleteSelectedItems());

        return view;
    }

    private void addItem (Item item) {
        List<Item> currentList = itemListLiveData.getValue();
        List<Item> updatedList = new ArrayList<>();
        if (currentList == null){
            currentList = new ArrayList<>();
        }
        currentList.add(item);
        itemListLiveData.setValue(new ArrayList<>(currentList));
    }

    public void deleteSelectedItems(){
        List<Item> currentList = itemListLiveData.getValue();
        if (currentList != null){
            List<Item> updatedList = new ArrayList<>();
            for (Item item : currentList){
                if (!item.isSelected()){
                    updatedList.add(item);
                }
            }
            itemListLiveData.setValue(updatedList);
        }
    }
// items class
   public static class Item {
        private String name;
        private boolean isSelected;

        public Item(String name) {
            this.name = name;
            this.isSelected = false;
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

}