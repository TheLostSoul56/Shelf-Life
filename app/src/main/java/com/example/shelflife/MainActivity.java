package com.example.shelflife;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.example.shelflife.databinding.ActivityMainBinding;

import android.view.View;

import com.example.shelflife.R;
import com.google.firebase.FirebaseApp;

import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar2);



        //sets the start up to go to the home screen when the app is opened
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
        }

        //an event listener to change the screen when the nav bar is clicked
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();
            if (itemID == R.id.home_button){
                replaceFragment(new HomeFragment());
            }else if (itemID == R.id.list){
                replaceFragment(new ListFragment());
            }else if (itemID == R.id.shelf){
                replaceFragment(new ShelfFragment());
            }else if (itemID == R.id.expired){
                replaceFragment(new ExpiredFragment());
            }else if (itemID == R.id.settings){
                replaceFragment(new SettingsFragment());
            }
            return true;
        });

    }





    // here is the method to change the screen or fragment when the event handler is triggered
    public void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
    // the code for the search bar to take in text and display required info
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

            MenuItem searchItem = menu.findItem(R.id.action_search);
            if (searchItem != null) {
                androidx.appcompat.widget.SearchView searchView =
                        (androidx.appcompat.widget.SearchView) searchItem.getActionView();


                searchView.setQueryHint("Search My Shelf");

                searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {

                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {

                        return false;
                    }
                });
            }
        return true;
    }
    public void addUser(View v){
        AddUserFragment newUser = new AddUserFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, newUser);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        
        //Fragment newFrag = new Fragment();
        //replaceFragment(newFrag);
    }
    public void goBack(View v)
    {
        HomeFragment newHome = new HomeFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, newHome);
        fragmentTransaction.commit();
       // replaceFragment(newHome);
    }
    //
}