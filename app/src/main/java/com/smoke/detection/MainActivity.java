/**
    ******************************************************************************
    * @file     MainActivity.java
    * @brief    This file contains the main activity class
    ******************************************************************************
    */

package com.smoke.detection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.smoke.detection.databinding.ActivityMainBinding;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;


/**
    ******************************************************************************
    * @defgroup    mainActivity MainActivity class
    * @brief       Main activity class
    ******************************************************************************
    */

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;                                    ///<    Binding for simplified work with elements on the fragment

    private final HashMap<String, ScanResult> devices = new HashMap<>();    ///<    List of found devices
    private static ScanResult device = null;                                ///<    Data about the device found

    private static Boolean toDisconnect = false;                            ///<    Device disconnection process status

    private Boolean isConnected = false;                                    ///<    Device connection status
    private SharedPreferences editor;                                       ///<    Storage on the smartphone

    /* List of basic requirements for working with BLE */
    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
    };


    /**
        ******************************************************************************
        * @brief        The method is called when the activity is first created
        * @ingroup      mainActivity
        * @param[in]    savedInstanceState - A mapping from String keys to various
        *                                    Parcelable values.
        ******************************************************************************
        */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater ltInflater = getLayoutInflater();
        binding = ActivityMainBinding.inflate(ltInflater);
        View view = binding.getRoot();
        setContentView(view);

        /* Immediately open a fragment of the device management */
        navigate(ControlFragment.newInstance("00:00:00:00:00:00"));

        /* Creating storage on the smartphone */
        editor = getSharedPreferences("Storage", Context.MODE_PRIVATE);

        /* Assigning actions to the buttons */
        binding.bHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                binding.bHome.setImageResource(R.drawable.home_red);
                binding.bSearch.setImageResource(R.drawable.search_accent);
                navigate(ControlFragment.newInstance("00:00:00:00:00:00"));
            }
        });

        binding.bSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                binding.bHome.setImageResource(R.drawable.home_accent);
                binding.bSearch.setImageResource(R.drawable.search_red);
                navigate(DevicesFragment.newInstance());
            }
        });
    }


    /**
        ******************************************************************************
        * @brief         Function to access storage from fragments
        * @ingroup       mainActivity
        * @param[out]    editor - Storage on the smartphone
        ******************************************************************************
        */

    public SharedPreferences getEditor() {
        return editor;
    }


    /**
        ******************************************************************************
        * @brief        Function for saving the list of found devices
        * @ingroup      mainActivity
        * @param[in]    items - List of found devices
        ******************************************************************************
        */

    public void setHashMap(HashMap<String, ScanResult> items) {
        devices.clear();
        devices.putAll(items);
    }


    /**
        ******************************************************************************
        * @brief        Function for saving the data of a connected device
        * @ingroup      mainActivity
        * @param[in]    item - Connected device data
        ******************************************************************************
        */

    public void setSavedDevice(ScanResult item) {
        device = item;
    }


    /**
        ******************************************************************************
        * @brief         Function for getting the list of found devices
        * @ingroup       mainActivity
        * @param[out]    devices - List of found devices
        ******************************************************************************
        */

    public HashMap<String, ScanResult> getHashMap() {
        return devices;
    }


    /**
        ******************************************************************************
        * @brief         Function for retrieving data from a connected device
        * @ingroup       mainActivity
        * @param[out]    device - Connected device data
        ******************************************************************************
        */

    public static ScanResult getSavedDevice() {
        return device;
    }


    /**
        ******************************************************************************
        * @brief        Function to save the status on the device disconnection
        * @ingroup      mainActivity
        * @param[in]    flag - Are we going to disconnect the device or not
        ******************************************************************************
        */

    public static void setToDisconnect(Boolean flag) {
        toDisconnect = flag;
    }


    /**
        ******************************************************************************
        * @brief         Function for receiving status to disconnect the device
        * @ingroup       mainActivity
        * @param[out]    toDisconnect - Are we going to disconnect the device or not
        ******************************************************************************
        */

    public static Boolean getToDisconnect() {
        return toDisconnect;
    }


    /**
        ******************************************************************************
        * @brief        Function for saving the connection status of the device
        * @ingroup      mainActivity
        * @param[in]    flag - Whether the device is connected or not
        ******************************************************************************
        */

    public void setIsConnected(Boolean flag) {
        isConnected = flag;
    }


    /**
        ******************************************************************************
        * @brief         Function to get the connection status of the device
        * @ingroup       mainActivity
        * @param[out]    isConnected - Whether the device is connected or not
        ******************************************************************************
        */

    public Boolean getIsConnected() {
        return isConnected;
    }


    /**
        ******************************************************************************
        * @brief        Changing fragment function
        * @ingroup      mainActivity
        * @param[in]    fragment - To which fragment do we change
        ******************************************************************************
        */

    private void navigate(Fragment fragment) {

        /* Requesting permission */
        ActivityCompat.requestPermissions(this, ANDROID_12_BLE_PERMISSIONS, 0);

        /* Changing the fragment */
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.containerFragment, fragment)
                .commit();
    }
}