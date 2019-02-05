package com.michaelmuratov.arduinobluetooth;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    ArrayList<Device> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Lookup the recyclerview in activity layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        RecyclerView rvContacts = (RecyclerView) findViewById(R.id.rvContacts);

        // Create adapter passing in the sample user data
        ContactsAdapter adapter = new ContactsAdapter(devices);
        // Attach the adapter to the recyclerview to populate items
        rvContacts.setAdapter(adapter);
        // Set layout manager to position the items
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        // That's all!
    }
}