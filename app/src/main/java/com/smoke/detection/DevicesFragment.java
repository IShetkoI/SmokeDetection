/**
    ******************************************************************************
    * @file     DevicesFragment.java
    * @brief    This file contains a fragment class with a list of found devices
    ******************************************************************************
    */

package com.smoke.detection;

import android.bluetooth.le.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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


/**
    ******************************************************************************
    * @defgroup    devicesFragment DevicesFragment class
    * @brief       The fragment class with the list of found devices
    ******************************************************************************
    */

public class DevicesFragment extends BaseFragment<FragmentDevicesBinding> implements DevicesAdapter.Callback {

    private final DevicesAdapter devicesAdapter = new DevicesAdapter();     ///<    Device list adapter class
    private final HashMap<String, ScanResult> devices = new HashMap<>();    ///<    List of found devices


    /**
        ******************************************************************************
        * @brief      Callback manager to work with device connection
        * @ingroup    devicesFragment
        ******************************************************************************
        */

    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {


        /**
            ******************************************************************************
            * @brief        The method works when new devices are detected
            * @ingroup      devicesFragment
            * @param[in]    peripheral - Represents a remote Bluetooth peripheral and
            *                            replaces BluetoothDevice and BluetoothGatt
            * @param[in]    scanResult - Data about the device found
            ******************************************************************************
            */

        @Override
        public void onDiscoveredPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull ScanResult scanResult) {

            /* Saving, sorting and updating the device list */
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


    /**
        ******************************************************************************
        * @brief        This overwrites the initialization method of the fragment
        *               display
        * @ingroup      devicesFragment
        * @param[in]    inflater  - The LayoutInflater class is used to instantiate
        *                           the contents of layout XML files into their
        *                           corresponding View objects.
        * @param[in]    container - The view group is the base class for layouts and
        *                           views containers
        ******************************************************************************
        */

    @Override
    FragmentDevicesBinding initViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentDevicesBinding.inflate(inflater, container, false);
    }


    /**
        ******************************************************************************
        * @brief        Method works when the fragment is created
        * @ingroup      devicesFragment
        * @param[in]    view – The View returned by
        *                      onCreateView(LayoutInflater, ViewGroup, Bundle)
        * @param[in]    savedInstanceState – If non-null, this fragment is being
        *                                    re-constructed from a previous saved
        *                                    state as given here.
        ******************************************************************************
        */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* Create a list of found devices and configure it */
        binding.rvDevicesRecycler.addItemDecoration(new DividerItemDecoration(requireContext(), RecyclerView.VERTICAL));
        binding.rvDevicesRecycler.setAdapter(devicesAdapter);
        binding.rvDevicesRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        devicesAdapter.setCallback(this);

        /* Creating a manager to control and manage the bluetooth device */
        centralManager = new BluetoothCentralManager(requireContext(), bluetoothCentralManagerCallback, new Handler(Looper.getMainLooper()));

        /* Locking the refresh button when connected to the device */
        Boolean isDeviceConnected = ((MainActivity) requireActivity()).getIsConnected();
        if (!isDeviceConnected) {
            binding.bRefresh.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    scanDevices();
                }
            });
        }
    }


    /**
        ******************************************************************************
        * @brief        New device search function
        * @ingroup      devicesFragment
        ******************************************************************************
        */

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


    /**
        ******************************************************************************
        * @brief      Called when the Fragment is no longer resumed
        * @ingroup    devicesFragment
        ******************************************************************************
        */

    @Override
    public void onPause() {
        super.onPause();

        /* Saving the current device list */
        ((MainActivity) requireActivity()).setHashMap(devices);
    }


    /**
        ******************************************************************************
        * @brief      Called when the fragment is visible to the user and actively
        *             running
        * @ingroup    devicesFragment
        ******************************************************************************
        */

    @Override
    public void onResume() {
        super.onResume();

        /* Resume the display of the found devices list */
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

        /* If a device is connected and needs to be disconnected */
        Boolean isDeviceConnected = ((MainActivity) requireActivity()).getIsConnected();

        if (isDeviceConnected) {
            isConnected();
        }
    }


    /**
        ******************************************************************************
        * @brief      Function to check if the device needs to be disconnected
        * @ingroup    devicesFragment
        ******************************************************************************
        */

    public void isConnected() {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
                                  @Override
                                  public void run() {
                                      /* Disconnecting the device */
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


    /**
        ******************************************************************************
        * @brief      Device disconnection function
        * @ingroup    devicesFragment
        ******************************************************************************
        */

    public void disconnectPeripheral() {
        ((MainActivity) getActivity()).setSavedDevice(null);
        navigateToControlScreen("Disconnection");
    }


    /**
        ******************************************************************************
        * @brief        Function for processing a click on the line in the device list
        * @ingroup      devicesFragment
        * @param[in]    scanResult - Data about the device found
        ******************************************************************************
        */

    @Override
    public void onItemClick(ScanResult scanResult) {
        ((MainActivity) getActivity()).setSavedDevice(scanResult);

        navigateToControlScreen(scanResult.getDevice().getAddress());
    }


    /**
        ******************************************************************************
        * @brief        Changing fragment function
        * @ingroup      devicesFragment
        * @param[in]    address - The address of the device to be connected
        ******************************************************************************
        */

    private void navigateToControlScreen(String address) {
        ImageButton imb = getActivity().findViewById(R.id.bHome);
        imb.setImageResource(R.drawable.home_red);
        imb = getActivity().findViewById(R.id.bSearch);
        imb.setImageResource(R.drawable.search_accent);

        getParentFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.containerFragment, ControlFragment.newInstance(address))
                .commit();
    }


    /**
        ******************************************************************************
        * @brief        The method works when you switch to this fragment
        * @ingroup      controlFragment
        * @param[out]   fragment - Fragment data
        ******************************************************************************
        */

    public static DevicesFragment newInstance() {
        return new DevicesFragment();
    }

    private BluetoothCentralManager centralManager = null;    ///<    The manager to control and manage the bluetooth device
}
