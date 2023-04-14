package com.smoke.detection;

import android.bluetooth.le.ScanResult;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.smoke.detection.databinding.FragmentDevicesBinding;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class DevicesFragment extends BaseFragment<FragmentDevicesBinding> implements DevicesAdapter.Callback {

    private final DevicesAdapter devicesAdapter = new DevicesAdapter();
    private final HashMap<String, ScanResult> devices = new HashMap<>();

    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {
        @Override
        public void onDiscoveredPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull ScanResult scanResult) {
            devices.put(scanResult.getDevice().getAddress(), scanResult);

            List<ScanResult> items = new ArrayList<>(devices.values());
            List<ScanResult> itemsNamed = new ArrayList<>();
            List<ScanResult> itemsNotNamed = new ArrayList<>();

            for (ScanResult result : items) {
                if (result.getDevice().getName() != null) {
                    itemsNamed.add(result);
                } else {
                    itemsNotNamed.add(result);
                }
            }

            List<ScanResult> litems = new ArrayList<>();
            litems.addAll(itemsNamed);
            litems.addAll(itemsNotNamed);
            devicesAdapter.update(litems);
        }
    };

    @Override
    FragmentDevicesBinding initViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentDevicesBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(requireContext(), R.drawable.divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        binding.rvDevicesRecycler.addItemDecoration(horizontalDecoration);
        binding.rvDevicesRecycler.setAdapter(devicesAdapter);
        binding.rvDevicesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        devicesAdapter.setCallback(this);

        centralManager = new BluetoothCentralManager(requireContext(), bluetoothCentralManagerCallback, new Handler(Looper.getMainLooper()));

        Boolean isDeviceConnected = ((MainActivity) requireActivity()).getIsConnected();
        if (!isDeviceConnected) {
            binding.bRefresh.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    scanDevices();
                }
            });
        }

        ImageView iv = requireActivity().findViewById(R.id.bHome);
        iv.setImageResource(R.drawable.home_accent);
        iv = requireActivity().findViewById(R.id.bgHome);
        iv.setImageResource(R.drawable.convex_button);
        iv = requireActivity().findViewById(R.id.bSearch);
        iv.setImageResource(R.drawable.search_orange);
        iv = requireActivity().findViewById(R.id.bgSearch);
        iv.setImageResource(R.drawable.concave_button);
    }

    public void scanDevices() {
        devices.clear();
        centralManager.scanForPeripherals();
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        centralManager.stopScan();
                    }
                }, 1000);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) requireActivity()).setHashMap(devices);
    }

    @Override
    public void onResume() {
        super.onResume();

        devices.clear();
        devices.putAll(((MainActivity) requireActivity()).getHashMap());

        if (devices.size() != 0) {
            List<ScanResult> items = new ArrayList<>(devices.values());
            List<ScanResult> itemsNamed = new ArrayList<>();
            List<ScanResult> itemsNotNamed = new ArrayList<>();

            for (ScanResult result : items) {
                if (result.getDevice().getName() != null) {
                    itemsNamed.add(result);
                } else {
                    itemsNotNamed.add(result);
                }
            }
            List<ScanResult> litems = new ArrayList<>();
            litems.addAll(itemsNamed);
            litems.addAll(itemsNotNamed);
            devicesAdapter.update(litems);
        }

        Boolean isDeviceConnected = ((MainActivity) requireActivity()).getIsConnected();

        if (isDeviceConnected) {
            isConnected();
        }
    }

    public void isConnected() {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
                                  @Override
                                  public void run() {
                                      Boolean flag = MainActivity.getToDisconnect();
                                      if (flag) {
                                          disconnectPeripheral();
                                          t.cancel();
                                      }
                                  }
                              },
                0,
                1000);
    }

    public void disconnectPeripheral() {
        ((MainActivity) getActivity()).setSavedDevice(null);
        navigateToControlScreen("Disconnection");
    }

    @Override
    public void onItemClick(ScanResult scanResult) {
        ((MainActivity) getActivity()).setSavedDevice(scanResult);

        navigateToControlScreen(scanResult.getDevice().getAddress());
    }

    private void navigateToControlScreen(String address) {
        ImageView iv = requireActivity().findViewById(R.id.bHome);
        iv.setImageResource(R.drawable.home_orange);
        iv = requireActivity().findViewById(R.id.bgHome);
        iv.setImageResource(R.drawable.convex_button);
        iv = requireActivity().findViewById(R.id.bSearch);
        iv.setImageResource(R.drawable.search_accent);
        iv = requireActivity().findViewById(R.id.bgSearch);
        iv.setImageResource(R.drawable.convex_button);

        getParentFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.containerFragment, ControlFragment.newInstance(address))
                .commit();
    }

    public static DevicesFragment newInstance() {
        return new DevicesFragment();
    }

    private BluetoothCentralManager centralManager = null;
}
