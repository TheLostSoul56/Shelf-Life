package com.example.shelflife;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReceiptsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ReceiptAdapter adapter;
    private MutableLiveData<List<Receipt>> receiptListLiveData = new MutableLiveData<>(new ArrayList<>());
    private FirebaseService firebaseService;

    public ReceiptsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseService = new FirebaseService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receipts, container, false);
        recyclerView = view.findViewById(R.id.receiptsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ReceiptAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        receiptListLiveData.observe(getViewLifecycleOwner(), receipts -> adapter.updateReceipts(receipts));

        // Load receipts from Firebase
        firebaseService.getAllReceipts(receiptListLiveData);

        return view;
    }

    public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder> {
        private List<Receipt> receiptList;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

        public ReceiptAdapter(List<Receipt> receiptList) {
            this.receiptList = receiptList;
        }

        @NonNull
        @Override
        public ReceiptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.receipt_item, parent, false);
            return new ReceiptViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReceiptViewHolder holder, int position) {
            Receipt receipt = receiptList.get(position);
            holder.dateTextView.setText("Date: " + dateFormat.format(receipt.getDate()));

            StringBuilder itemsText = new StringBuilder();
            for (ShelfFragment.Item item : receipt.getExpiredItems()) {
                itemsText.append("• ").append(item.getName())
                        .append(" (").append(item.getStoreName()).append(")\n");
            }
            holder.itemsTextView.setText(itemsText.toString());
        }

        @Override
        public int getItemCount() {
            return receiptList.size();
        }

        public void updateReceipts(List<Receipt> newReceipts) {
            this.receiptList.clear();
            this.receiptList.addAll(newReceipts);
            notifyDataSetChanged();
        }

        public class ReceiptViewHolder extends RecyclerView.ViewHolder {
            TextView dateTextView;
            TextView itemsTextView;

            public ReceiptViewHolder(@NonNull View itemView) {
                super(itemView);
                dateTextView = itemView.findViewById(R.id.receiptDate);
                itemsTextView = itemView.findViewById(R.id.receiptItems);
            }
        }
    }
}
