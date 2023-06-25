/**
    ******************************************************************************
    * @file     DevicesAdapter.java
    * @brief    This file contains the adapter for the list of found devices
    ******************************************************************************
    */

package com.smoke.detection;

import android.annotation.SuppressLint;
import android.bluetooth.le.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smoke.detection.databinding.ItemDeviceBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
    ******************************************************************************
    * @defgroup    devicesAdapter DevicesAdapter class
    * @brief       Device list adapter class
    ******************************************************************************
    */

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.DevicesViewHolder> {

    private final List<ScanResult> items = new ArrayList<>();    ///<    List of found devices
    private Callback callback = null;                            ///<    Callback on clicking on a device in the list


    /**
        ******************************************************************************
        * @brief        The method works by updating the list of found devices
        * @ingroup      controlFragment
        * @param[in]    items  - List of found devices
        ******************************************************************************
        */

    @SuppressLint("NotifyDataSetChanged")
    public void update(List<ScanResult> items)  {

        /* Updating the list of found devices */
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }


    /**
        ******************************************************************************
        * @brief        Callback Assignment Method
        * @ingroup      controlFragment
        * @param[in]    callback - Callback on clicking on a device in the list
        ******************************************************************************
        */

    public void setCallback(Callback callback) {
        this.callback = callback;
    }


    /**
        ******************************************************************************
        * @brief        The method works by creating a list of devices on the fragment
        * @ingroup      controlFragment
        * @param[in]    parent   – The ViewGroup into which the new View will be added
        *                          after it is bound to an adapter position
        * @param[in]    viewType – The view type of the new View.
        ******************************************************************************
        */

    @NonNull
    @Override
    public DevicesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDeviceBinding binding = ItemDeviceBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new DevicesViewHolder(binding);
    }


    /**
        ******************************************************************************
        * @brief        This method should update the contents of the
        *               RecyclerView.ViewHolder.itemView to reflect the item at
        *               the given position
        * @ingroup      controlFragment
        * @param[in]    holder   – The ViewHolder which should be updated to
        *                          represent the contents of the item at the given
        *                          position in the data set
        * @param[in]    position – The position of the item within the adapter's data
        *                          set
        ******************************************************************************
        */

    @Override
    public void onBindViewHolder(@NonNull DevicesViewHolder holder, int position) {
        ScanResult scanResult = MainActivity.getSavedDevice();
        holder.bind(items.get(position), scanResult);

        holder.binding.bDisconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.setToDisconnect(true);
            }
        });
    }


    /**
        ******************************************************************************
        * @brief         Returns the total number of items in the data set held by the
        *                adapter
        * @ingroup       controlFragment
        * @param[out]    The total number of items in this adapter
        ******************************************************************************
        */

    @Override
    public int getItemCount() {
        return items.size();
    }


    /**
        ******************************************************************************
        * @brief      Device list clas
        * @ingroup    controlFragment
        ******************************************************************************
        */

    class DevicesViewHolder extends RecyclerView.ViewHolder {

        private final ItemDeviceBinding binding;    ///<    Binding for simplified work with elements on the fragment


        /**
            ******************************************************************************
            * @brief        Class constructor
            * @ingroup      controlFragment
            * @param[in]    binding - Binding for simplified work with elements on the fragment
            ******************************************************************************
            */

        public DevicesViewHolder(@NonNull ItemDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }


        /**
            ******************************************************************************
            * @brief        Method of creating a line in the list
            * @ingroup      controlFragment
            * @param[in]    scanResult  - Device after finding
            * @param[in]    savedDevice - Saved device already connected to
            ******************************************************************************
            */

        public void bind(ScanResult scanResult, ScanResult savedDevice) {

            /* Create a line with the found device */
            itemView.setOnClickListener(view -> callback.onItemClick(scanResult));
            binding.textName.setText(scanResult.getDevice().getName());
            binding.textAddress.setText(scanResult.getDevice().getAddress());

            /* Create a line with the connected device and enable the "disconnect" button */
            if (savedDevice != null && Objects.equals(scanResult.getDevice().getAddress(), savedDevice.getDevice().getAddress()))
            {
                binding.textRSSI.setVisibility(View.GONE);
                binding.bDisconnect.setVisibility(View.VISIBLE);
            }
            else {
                binding.textRSSI.setText(Integer.toString(scanResult.getRssi()));
            }
        }
    }


    /**
        ******************************************************************************
        * @brief      Creating an interface to work with clicking on the line in the
        *             list
        * @ingroup    controlFragment
        ******************************************************************************
        */

    public interface Callback {
        void onItemClick(ScanResult device);
    }
}
