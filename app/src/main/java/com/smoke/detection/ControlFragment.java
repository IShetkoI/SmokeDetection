package com.smoke.detection;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanResult;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.smoke.detection.databinding.FragmentControlBinding;
import com.welie.blessed.BluetoothCentralManager;
import com.welie.blessed.BluetoothCentralManagerCallback;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.BluetoothPeripheralCallback;
import com.welie.blessed.GattStatus;
import com.welie.blessed.HciStatus;

import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ControlFragment extends BaseFragment<FragmentControlBinding> {
    private static final String KEY_DEVICE_ADDRESS = "00:00:00:00:00:00";
    private Boolean isToDisconnected = false;
    private Boolean isConnected = false;
    private Boolean onResume = true;
    public String address = null;
    private Bag<Byte> bag = new HashBag<>();

    @Override
    FragmentControlBinding initViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentControlBinding.inflate(inflater, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        address = getArguments().getString(KEY_DEVICE_ADDRESS);
        centralManager = new BluetoothCentralManager(requireContext(), bluetoothCentralManagerCallback, new Handler(Looper.getMainLooper()));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (Objects.equals(address, "Disconnection")) {
            isToDisconnected = true;
            MainActivity.setToDisconnect(true);
        }
        else if (!Objects.equals(address, "00:00:00:00:00:00") && !isConnected) {
            BluetoothPeripheral peripheral = centralManager.getPeripheral(address);
            getActivity().findViewById(R.id.cvConnectionStatus).setBackgroundColor(requireContext().getColor(R.color.accent));
            centralManager.connectPeripheral(peripheral, peripheralCallback);
        }

        SharedPreferences prefs = ((MainActivity) requireActivity()).getEditor();

        String status = prefs.getString("status", "");
        TextView tv = requireActivity().findViewById(R.id.status);
        switch (status) {
            case "Indoor smoke":
                tv.setText(status);
                tv.setTextColor(requireContext().getColor(R.color.red));
                ((TextView) requireActivity().findViewById(R.id.last_status)).setText("Last status");
                break;
            case "Dog barking":
            case "Background noise":
            case "The baby is crying":
                tv.setText(status);
                tv.setTextColor(requireContext().getColor(R.color.accent));
                ((TextView) requireActivity().findViewById(R.id.last_status)).setText("Last status");
                break;
            default:
                tv.setText(status);
                ((TextView) requireActivity().findViewById(R.id.last_status)).setText("");
                break;
        }

        String date = prefs.getString("date", "");
        tv = requireActivity().findViewById(R.id.date);
        tv.setText(date);

        String time = prefs.getString("time", "");
        tv = requireActivity().findViewById(R.id.time);
        tv.setText(time);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {
        @Override
        public void onConnectedPeripheral(@NotNull BluetoothPeripheral peripheral) {
            Log.e("Connected to", peripheral.getAddress());

            isConnected = true;
            ((MainActivity) requireActivity()).setIsConnected(true);
            isToDisconnected = false;

            MainActivity.setToDisconnect(false);

            requireActivity().findViewById(R.id.cvConnectionStatus).setBackgroundColor(requireContext().getColor(R.color.blue));

            readCharacteristic(peripheral, true);
        }

        @Override
        public void onConnectionFailed(@NotNull BluetoothPeripheral peripheral, @NotNull HciStatus status) {
            Log.e("onConnectionFailed", status.toString());
            requireActivity().findViewById(R.id.cvConnectionStatus).setBackgroundColor(requireContext().getColor(R.color.red));
        }

        @Override
        public void onDisconnectedPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull HciStatus status) {
            Log.e("onDisconnectedPeripheral", status.toString());
            ((MainActivity) requireActivity()).setSavedDevice(null);
            isConnected = false;
            ((MainActivity) requireActivity()).setIsConnected(false);
            isToDisconnected = false;
            MainActivity.setToDisconnect(false);
            requireActivity().findViewById(R.id.cvConnectionStatus).setBackgroundColor(requireContext().getColor(R.color.main));
        }
    };

    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {
        @Override
        public void onServicesDiscovered(@NotNull BluetoothPeripheral peripheral) {
            List<BluetoothGattService> services = peripheral.getServices();
            for (BluetoothGattService service : services) {
                Log.e("Service", service.getUuid().toString());

                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    Log.e("Characteristic", characteristic.getUuid().toString());
                }
            }
        }

        @Override
        public void onNotificationStateUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            if (status == GattStatus.SUCCESS) {
                if(peripheral.isNotifying(characteristic)) {
                    Log.i("Notification", String.format("SUCCESS: Notify set to 'on' for %s", characteristic.getUuid()));
                    setPicture(R.drawable.listen);
                } else {
                    Log.i("Notification", String.format("SUCCESS: Notify set to 'off' for %s", characteristic.getUuid()));
                }
            } else {
                Log.e("Notification", String.format("ERROR: Changing notification state failed for %s", characteristic.getUuid()));
            }
        }


        @Override
        public void onCharacteristicUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
            isToDisconnected = MainActivity.getToDisconnect();
            if (isToDisconnected) {
                readCharacteristic(peripheral, false);
                centralManager.cancelConnection(peripheral);
                isConnected = false;
                ((MainActivity) requireActivity()).setIsConnected(false);
            }
            if (isConnected && onResume) {

                bag.add(value[0], 1);

                TextView tv;

                if(bag.size() == 10){

                    String last_status;
                    int color;

                    if(bag.getCount((byte)1) >= 6){
                        last_status = "Indoor smoke";
                        color = requireContext().getColor(R.color.red);
                        setPicture(R.drawable.smoke);
                    }
                    else if(bag.getCount((byte)2)>=3){
                        last_status = "The baby is crying";
                        color = requireContext().getColor(R.color.accent);
                        setPicture(R.drawable.crying);
                    }
                    else if(bag.getCount((byte)4) >= 3){
                        last_status = "Dog barking";
                        color = requireContext().getColor(R.color.accent);
                        setPicture(R.drawable.bark);
                    }
                    else if(bag.getCount((byte)3) >= 4){
                        last_status = "Background noise";
                        color = requireContext().getColor(R.color.accent);
                        setPicture(R.drawable.noise);
                    }
                    else {
                        last_status = "Peace";
                        color = requireContext().getColor(R.color.accent);
                        setPicture(R.drawable.peace);
                    }

                    Date today = Calendar.getInstance().getTime();
                    SimpleDateFormat formatter = new SimpleDateFormat("MM.dd.yyyy");
                    String date = formatter.format(today);
                    formatter = new SimpleDateFormat("hh:mm:ss");
                    String time = formatter.format(today);

                    SharedPreferences.Editor editor = ((MainActivity) requireActivity()).getEditor().edit();

                    editor.putString("status", last_status);
                    editor.putString("date", date);
                    editor.putString("time", time);
                    editor.apply();

                    tv = requireActivity().findViewById(R.id.status);
                    tv.setText(last_status);
                    tv.setTextColor(color);

                    tv = requireActivity().findViewById(R.id.date);
                    tv.setText(date);

                    tv = requireActivity().findViewById(R.id.time);
                    tv.setText(time);

                    ((TextView) requireActivity().findViewById(R.id.last_status)).setText("Last status");

                    bag.clear();
                    onResume = false;
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setPicture(R.drawable.listen);
                            onResume = true;
                        }
                    }, 4000);
                }
            }
        }
    };

    private BluetoothCentralManager centralManager = null;

    public void setPicture(int drawable) {
        try {
            ImageView iv = requireActivity().findViewById(R.id.ivAlert);
            iv.setImageResource(drawable);
        }
        catch (Exception ignored) {
        }
    }

    public void readCharacteristic(@NotNull BluetoothPeripheral peripheral, Boolean flag) {
        UUID serviceUUID = UUID.fromString( "e9ea0001-e19b-482d-9293-c7907585fc48");
        UUID characteristicUUID = UUID.fromString("e9ea0002-e19b-482d-9293-c7907585fc48");

        BluetoothGattCharacteristic characteristic = peripheral.getCharacteristic(serviceUUID, characteristicUUID);

        if (characteristic != null) {
            peripheral.setNotify(characteristic, flag);
        }
        else {
            getActivity().findViewById(R.id.cvConnectionStatus).setBackgroundColor(requireContext().getColor(R.color.yellow));
        }
    }


    public static ControlFragment newInstance(String address) {
        Bundle args = new Bundle();
        args.putString(KEY_DEVICE_ADDRESS, address);
        ControlFragment fragment = new ControlFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
