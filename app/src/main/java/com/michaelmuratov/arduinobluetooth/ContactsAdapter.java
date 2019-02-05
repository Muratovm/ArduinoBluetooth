package com.michaelmuratov.arduinobluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class ContactsAdapter extends
        RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    // Store a member variable for the devices
    private List<Device> mDevices;

    // Pass in the contact array into the constructor
    public ContactsAdapter(List<Device> devices) {
        mDevices = devices;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_device, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // Get the data model based on position
        final Device device = mDevices.get(position);

        // Set item views based on your views and data model
        TextView address = viewHolder.tvDaviceAddress;
        TextView name = viewHolder.tvDeviceName;
        name.setText(device.getName());
        address.setText(device.getAddress());
        Button button = viewHolder.messageButton;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = device.getAddress();
                BluetoothDevice mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
                GlobalService.mService.connect(address);
            }
        });
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView tvDaviceAddress;
        public TextView tvDeviceName;
        public Button messageButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            tvDaviceAddress = itemView.findViewById(R.id.tv_device_address);
            tvDeviceName = itemView.findViewById(R.id.tv_device_name);
            messageButton = (Button) itemView.findViewById(R.id.btn_device_connect);
        }
    }
}