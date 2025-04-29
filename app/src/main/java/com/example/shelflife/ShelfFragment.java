package com.example.shelflife;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
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
private List<Item> itemList;

    public ShelfFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        itemList = new ArrayList<>();
        adapter = new ItemAdapter(itemList);
        recyclerView.setAdapter(adapter);

        Button addToShelf = view.findViewById(R.id.addToShelf);
        //Button addToList = view.findViewById(R.id.addToList);
        Button delete = view.findViewById(R.id.delete);

        addToShelf.setOnClickListener(v -> {
            itemList.add(new Item("New Item " + (itemList.size() + 1)));
            adapter.notifyItemInserted(itemList.size() - 1);
            recyclerView.smoothScrollToPosition(itemList.size() - 1);
        });
        //addToShelf.setOnClickListener(v -> sendSelectedItemsToList());
        delete.setOnClickListener(v -> {
            adapter.deleteSelectedItems();
        });

        return view;
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

        public void deleteSelectedItems(){
            for (int i = itemList.size() - 1; i >=0; i--){
                if (itemList.get(i).isSelected()){
                    itemList.remove(i);
                    notifyItemRemoved(i);
                }
            }
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