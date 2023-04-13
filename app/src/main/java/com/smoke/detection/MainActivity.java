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

    private static String CHANNEL_ID = "Alert";

    private final HashMap<String, ScanResult> devices = new HashMap<>();
    private static ScanResult device = null;

    private static Boolean toDisconnect = false;

    private Boolean isConnected = false;

    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.POST_NOTIFICATIONS,
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater ltInflater = getLayoutInflater();
        binding = ActivityMainBinding.inflate(ltInflater);
        View view = binding.getRoot();
        setContentView(view);

        createNotificationChannel();

        navigate(ControlFragment.newInstance("00:00:00:00:00:00"));

        binding.bHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                navigate(ControlFragment.newInstance("00:00:00:00:00:00"));
            }
        });

        binding.bSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                navigate(DevicesFragment.newInstance());
            }
        });
    }

    public void showNotification(String title, String message) {

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(MainActivity.this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(100, buildNotification(title, message));
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

    public void createNotificationChannel() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID,importance);
        mChannel.setDescription(CHANNEL_ID);
        mChannel.enableLights(true);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mNotificationManager.createNotificationChannel(mChannel);
    }

    public Notification buildNotification(String title, String message) {
        Intent fullScreenIntent = new Intent(this, MainActivity.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(this, 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String CHANNEL_ID = "Cat channel";
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ERROR)
                        .setFullScreenIntent(fullScreenPendingIntent, true);

        return notificationBuilder.build();
    }

    private void navigate(Fragment fragment) {
        ActivityCompat.requestPermissions(this, ANDROID_12_BLE_PERMISSIONS, 0);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.containerFragment, fragment)
                .commit();
    }
}