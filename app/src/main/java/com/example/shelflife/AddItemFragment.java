package com.example.shelflife;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;


public class  AddItemFragment extends Fragment {

private EditText itemNameEditText, editDate, editExpiredDate, storeName;
private Button cancelBtn, submitBtn;
    public AddItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
//handles all the events for each button and tells the app what to do when each one is clicked
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_item, container, false);

        itemNameEditText = view.findViewById(R.id.itemNameForShelf);
        storeName = view.findViewById(R.id.storeNameForShelf);
        editDate = view.findViewById(R.id.editDate);
        editExpiredDate = view.findViewById(R.id.editExpireDate);
        cancelBtn = view.findViewById(R.id.cancel_addingItem);
        submitBtn = view.findViewById(R.id.submitToShelf);

        editDate.setOnClickListener(v->showDatePicker(editDate));
        editExpiredDate.setOnClickListener(v-> showDatePicker(editExpiredDate));
        cancelBtn.setOnClickListener(v ->{
            getParentFragmentManager().popBackStack();
        });

        submitBtn.setOnClickListener(v-> {
            String itemName = itemNameEditText.getText().toString().trim();
            String store = storeName.getText().toString().trim();
           if (!itemName.isEmpty()){
               Bundle result = new Bundle();
               result.putString("newItemName", itemName);
               result.putString("storeName", store);
               getParentFragmentManager().setFragmentResult("addItemRequest", result);

               getParentFragmentManager().popBackStack();
           }
        });

        return view;
    }
// for the dates so it pops up for the user to select the dates on when bought and when they expire
    private void showDatePicker(EditText targetField) {
       Calendar calendar = Calendar.getInstance();
       DatePickerDialog datePickerDialog = new DatePickerDialog(
               requireContext(), (view, year, month, dayOfMonth) -> {
                   String date = (month + 1) + "/" + dayOfMonth + "/" + year;
                   targetField.setText(date);
       },
           calendar.get(Calendar.YEAR),
           calendar.get(Calendar.MONTH),
           calendar.get(Calendar.DAY_OF_MONTH)
       );
       datePickerDialog.show();
    }
}