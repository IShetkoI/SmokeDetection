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

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private final HashMap<String, ScanResult> devices = new HashMap<>();
    private static ScanResult device = null;

    private static Boolean toDisconnect = false;

    private Boolean isConnected = false;
    private SharedPreferences editor;

    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater ltInflater = getLayoutInflater();
        binding = ActivityMainBinding.inflate(ltInflater);
        View view = binding.getRoot();
        setContentView(view);

        navigate(ControlFragment.newInstance("00:00:00:00:00:00"));

        editor = getSharedPreferences("Storage", Context.MODE_PRIVATE);

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

    public SharedPreferences getEditor() {
        return editor;
    }

    public void setHashMap(HashMap<String, ScanResult> items) {
        devices.clear();
        devices.putAll(items);
    }

    public void setSavedDevice(ScanResult item) {
        device = item;
    }

    public HashMap<String, ScanResult> getHashMap() {
        return devices;
    }

    public static ScanResult getSavedDevice() {
        return device;
    }

    public static void setToDisconnect(Boolean flag) {
        toDisconnect = flag;
    }

    public static Boolean getToDisconnect() {
        return toDisconnect;
    }

    public void setIsConnected(Boolean flag) {
        isConnected = flag;
    }

    public Boolean getIsConnected() {
        return isConnected;
    }

    private void navigate(Fragment fragment) {
        ActivityCompat.requestPermissions(this, ANDROID_12_BLE_PERMISSIONS, 0);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.containerFragment, fragment)
                .commit();
    }
}