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

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.DevicesViewHolder> {

    private final List<ScanResult> items = new ArrayList<>();
    private  Callback callback = null;

    @SuppressLint("NotifyDataSetChanged")
    public void update(List<ScanResult> items)  {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

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

    @Override
    public int getItemCount() {
        return items.size();
    }

    class DevicesViewHolder extends RecyclerView.ViewHolder {

        private final ItemDeviceBinding binding;

        public DevicesViewHolder(@NonNull ItemDeviceBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ScanResult scanResult, ScanResult savedDevice) {
            itemView.setOnClickListener(view -> callback.onItemClick(scanResult));
            binding.textName.setText(scanResult.getDevice().getName());
            binding.textAddress.setText(scanResult.getDevice().getAddress());
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

    public interface Callback {
        void onItemClick(ScanResult device);
    }
}
