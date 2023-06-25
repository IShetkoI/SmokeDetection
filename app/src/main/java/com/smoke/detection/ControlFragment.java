/**
    ******************************************************************************
    * @file     ControlFragment.java
    * @brief    This file contains the bluetooth device control fragment class
    ******************************************************************************
    */


package com.smoke.detection;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


/**
    ******************************************************************************
    * @defgroup    controlFragment ControlFragment class
    * @brief       Bluetooth device control fragment class
    ******************************************************************************
    */

public class ControlFragment extends BaseFragment<FragmentControlBinding> {
    private static final String KEY_DEVICE_ADDRESS = "00:00:00:00:00:00";    ///<    String containing the address of the device to be connected when jumping to this fragment from another
    private Boolean isToDisconnected = false;                                ///<    Device disconnection process status
    private Boolean isConnected = false;                                     ///<    Device connection status
    private Boolean onResume = true;                                         ///<    Blocks updates from the characteristic
    public String address = null;                                            ///<    String containing the address of the device to be connected
    private Bag<Byte> bag = new HashBag<>();                                 ///<    Temporary storage of updates from characteristics


    /**
        ******************************************************************************
        * @brief         Initialize view method
        * @ingroup       controlFragment
        * @param[in]     inflater  - The LayoutInflater class is used to instantiate
        *                            the contents of layout XML files into their
        *                            corresponding View objects.
        * @param[in]     container - The view group is the base class for layouts and
        *                            views containers
        * @param[out]    binding   - for simplified work with elements on the fragment
        ******************************************************************************
        */

    @Override
    FragmentControlBinding initViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentControlBinding.inflate(inflater, container, false);
    }


    /**
        ******************************************************************************
        * @brief        View creation method
        * @ingroup      controlFragment
        * @param[in]    savedInstanceState - A mapping from String keys to various
        *                                    Parcelable values.
        ******************************************************************************
        */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Getting the passed address from another fragment */
        assert getArguments() != null;
        address = getArguments().getString(KEY_DEVICE_ADDRESS);

        /* Creating a manager to control and manage the bluetooth device */
        centralManager = new BluetoothCentralManager(requireContext(), bluetoothCentralManagerCallback, new Handler(Looper.getMainLooper()));
    }


    /**
        ******************************************************************************
        * @brief        Method works when the fragment is created
        * @ingroup      controlFragment
        * @param[in]    view – The View returned by
        *                      onCreateView(LayoutInflater, ViewGroup, Bundle)
        * @param[in]    savedInstanceState – If non-null, this fragment is being
        *                                    re-constructed from a previous saved
        *                                    state as given here.
        ******************************************************************************
        */

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        /* Checking the action with the device */
        if (Objects.equals(address, "Disconnection")) {

            /* Disconnecting the device */
            isToDisconnected = true;
            MainActivity.setToDisconnect(true);
        }
        else if (!Objects.equals(address, "00:00:00:00:00:00") && !isConnected) {

            /* Сonnecting the device */
            BluetoothPeripheral peripheral = centralManager.getPeripheral(address);
            getActivity().findViewById(R.id.cvConnectionStatus).setBackgroundColor(requireContext().getColor(R.color.accent));
            centralManager.connectPeripheral(peripheral, peripheralCallback);
        }

        /* Connecting to the data storage created on your smartphone */
        SharedPreferences prefs = ((MainActivity) requireActivity()).getEditor();

        /* Saving data */
        String status = prefs.getString("status", "");

        /* Changing the event status on the fragment */
        TextView tv = requireActivity().findViewById(R.id.status);

        /* Changing the text */
        switch (status) {
            case "Smoke detector":
                tv.setText(status);
                tv.setTextColor(requireContext().getColor(R.color.red));
                ((TextView) requireActivity().findViewById(R.id.last_status)).setText("Last status");
                break;
            case "Dog bark":
            case "Noise":
            case "Baby cry":
                tv.setText(status);
                tv.setTextColor(requireContext().getColor(R.color.accent));
                ((TextView) requireActivity().findViewById(R.id.last_status)).setText("Last status");
                break;
            default:
                tv.setText(status);
                ((TextView) requireActivity().findViewById(R.id.last_status)).setText("");
                break;
        }

        /* Changing the date */
        String date = prefs.getString("date", "");
        tv = requireActivity().findViewById(R.id.date);
        tv.setText(date);


        /* Changing the time */
        String time = prefs.getString("time", "");
        tv = requireActivity().findViewById(R.id.time);
        tv.setText(time);
    }


    /**
        ******************************************************************************
        * @brief      Callback manager to work with device connection
        * @ingroup    controlFragment
        ******************************************************************************
        */

    private final BluetoothCentralManagerCallback bluetoothCentralManagerCallback = new BluetoothCentralManagerCallback() {


        /**
            ******************************************************************************
            * @brief        The method works when connected to a device
            * @ingroup      controlFragment
            * @param[in]    peripheral - Represents a remote Bluetooth peripheral and
            *                            replaces BluetoothDevice and BluetoothGatt
            ******************************************************************************
            */

        @Override
        public void onConnectedPeripheral(@NotNull BluetoothPeripheral peripheral) {
            Log.e("Connected to", peripheral.getAddress());

            /* Saving the connection status */
            isConnected = true;
            ((MainActivity) requireActivity()).setIsConnected(true);
            isToDisconnected = false;

            MainActivity.setToDisconnect(false);

            requireActivity().findViewById(R.id.cvConnectionStatus).setBackgroundColor(requireContext().getColor(R.color.blue));

            readCharacteristic(peripheral, true);
        }


        /**
            ******************************************************************************
            * @brief        The method works if there is an error when connecting to the
            *               device
            * @ingroup      controlFragment
            * @param[in]    peripheral - Represents a remote Bluetooth peripheral and
            *                            replaces BluetoothDevice and BluetoothGatt
            * @param[in]    status     - The status of the connection
            ******************************************************************************
            */

        @Override
        public void onConnectionFailed(@NotNull BluetoothPeripheral peripheral, @NotNull HciStatus status) {
            Log.e("onConnectionFailed", status.toString());
            requireActivity().findViewById(R.id.cvConnectionStatus).setBackgroundColor(requireContext().getColor(R.color.red));
        }


        /**
            ******************************************************************************
            * @brief        The method works by disconnecting the device from the
            *               smartphone
            * @ingroup      controlFragment
            * @param[in]    peripheral - Represents a remote Bluetooth peripheral and
            *                            replaces BluetoothDevice and BluetoothGatt
            * @param[in]    status     - The status of the connection
            ******************************************************************************
            */

        @Override
        public void onDisconnectedPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull HciStatus status) {
            Log.e("onDisconnectedPeripheral", status.toString());

            /* Saving the connection status */
            ((MainActivity) requireActivity()).setSavedDevice(null);
            isConnected = false;
            ((MainActivity) requireActivity()).setIsConnected(false);
            isToDisconnected = false;
            MainActivity.setToDisconnect(false);

            requireActivity().findViewById(R.id.cvConnectionStatus).setBackgroundColor(requireContext().getColor(R.color.main));
        }
    };


    /**
        ******************************************************************************
        * @brief      Callback manager to work with a connected device
        * @ingroup    controlFragment
        ******************************************************************************
        */

    private final BluetoothPeripheralCallback peripheralCallback = new BluetoothPeripheralCallback() {


        /**
            ******************************************************************************
            * @brief        The method works when services are discovered
            * @ingroup      controlFragment
            * @param[in]    peripheral - Represents a remote Bluetooth peripheral and
            *                            replaces BluetoothDevice and BluetoothGatt
            ******************************************************************************
            */

        @Override
        public void onServicesDiscovered(@NotNull BluetoothPeripheral peripheral) {

            /* Displaying found services in the console */
            List<BluetoothGattService> services = peripheral.getServices();

            for (BluetoothGattService service : services) {

                Log.e("Service", service.getUuid().toString());

                List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();

                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    Log.e("Characteristic", characteristic.getUuid().toString());
                }
            }
        }


        /**
            ******************************************************************************
            * @brief        The method works when connected to notifications
            * @ingroup      controlFragment
            * @param[in]    peripheral     - Represents a remote Bluetooth peripheral and
            *                                replaces BluetoothDevice and BluetoothGatt
            * @param[in]    characteristic - The characteristic contains a value as well
            *                                as additional information and optional GATT
            *                                descriptors
            * @param[in]    status         - The status of the operation
            ******************************************************************************
            */

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


        /**
            ******************************************************************************
            * @brief        The method works by updating the values of the characteristic
            * @ingroup      controlFragment
            * @param[in]    peripheral     - Represents a remote Bluetooth peripheral and
            *                                replaces BluetoothDevice and BluetoothGatt
            * @param[in]    value          - Value of the characteristic
            * @param[in]    characteristic - The characteristic contains a value as well
            *                                as additional information and optional GATT
            *                                descriptors
            * @param[in]    status         - The status of the operation
            ******************************************************************************
            */

        @Override
        public void onCharacteristicUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {

            /* If we disconnect from the device, we stop reading the characteristic */
            isToDisconnected = MainActivity.getToDisconnect();
            if (isToDisconnected) {
                readCharacteristic(peripheral, false);
                centralManager.cancelConnection(peripheral);
                isConnected = false;
                ((MainActivity) requireActivity()).setIsConnected(false);
            }

            /* If the device is connected and continue to collect updates */
            if (isConnected && onResume) {
                bag.add(value[0], 1);
                TextView tv;

                /* When 10 updates are accumulated, we change the appearance of the fragment for the new event */
                if(bag.size() == 10){
                    String last_status;
                    int color;

                    if(bag.getCount((byte)1) >= 6){
                        last_status = "Smoke detector";
                        color = requireContext().getColor(R.color.red);
                        setPicture(R.drawable.smoke);
                    }
                    else if(bag.getCount((byte)2)>=3){
                        last_status = "Baby cry";
                        color = requireContext().getColor(R.color.accent);
                        setPicture(R.drawable.crying);
                    }
                    else if(bag.getCount((byte)4) >= 3){
                        last_status = "Dog bark";
                        color = requireContext().getColor(R.color.accent);
                        setPicture(R.drawable.bark);
                    }
                    else if(bag.getCount((byte)3) >= 4){
                        last_status = "Noise";
                        color = requireContext().getColor(R.color.accent);
                        setPicture(R.drawable.noise);
                    }
                    else {
                        last_status = "Peace";
                        color = requireContext().getColor(R.color.accent);
                        setPicture(R.drawable.peace);
                    }

                    /* Remembering the update time */
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

                    /* Suspend the gathering of updates to delay the picture of a new event */
                    onResume = false;

                    /* After 4 seconds, resume gathering */
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

    private BluetoothCentralManager centralManager = null;    ///<    The manager to control and manage the bluetooth device


    /**
        ******************************************************************************
        * @brief        Event picture update function
        * @ingroup      controlFragment
        * @param[in]    drawable - A picture of the event
        ******************************************************************************
        */

    public void setPicture(int drawable) {
        try {
            ImageView iv = requireActivity().findViewById(R.id.ivAlert);
            iv.setImageResource(drawable);
        }
        catch (Exception ignored) {
        }
    }


    /**
        ******************************************************************************
        * @brief        Event picture update function
        * @ingroup      controlFragment
        * @param[in]    peripheral - Represents a remote Bluetooth peripheral and
        *                            replaces BluetoothDevice and BluetoothGatt
        * @param[in]    flag       - Enable or disable reading the characteristic
        ******************************************************************************
        */

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


    /**
        ******************************************************************************
        * @brief        The method works when you switch to this fragment
        * @ingroup      controlFragment
        * @param[in]    address  - The address of the device to be connected
        * @param[out]   fragment - Fragment data
        ******************************************************************************
        */

    public static ControlFragment newInstance(String address) {

        /* Getting the address that was passed from another fragment or activity */
        Bundle args = new Bundle();
        args.putString(KEY_DEVICE_ADDRESS, address);
        ControlFragment fragment = new ControlFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
