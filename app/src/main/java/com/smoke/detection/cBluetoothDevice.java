/**
    ******************************************************************************
    * @file     cBluetoothDevice.java
    * @brief    This file contains a class that stores data about the bluetooth
    *           device found
    ******************************************************************************
    */

package com.smoke.detection;


/**
    ******************************************************************************
    * @defgroup    bluetoothDevice BluetoothDevice class
    * @brief       A class that stores data about the bluetooth device found
    ******************************************************************************
    */

public class cBluetoothDevice {

    private final String name;       ///<    Device name
    private final String address;    ///<    Device address


    /**
        ******************************************************************************
        * @brief        Class constructor
        * @ingroup      bluetoothDevice
        * @param[in]    name    - Device name
        * @param[in]    address - Device address
        ******************************************************************************
        */

    public cBluetoothDevice(String name, String address) {
        this.name = name;
        this.address = address;
    }


    /**
     ******************************************************************************
     * @brief         Method to get the device name
     * @ingroup       bluetoothDevice
     * @param[out]    name    - Device name
     ******************************************************************************
     */

    public String getName() {
        return name;
    }


    /**
     ******************************************************************************
     * @brief         Method to get the device address
     * @ingroup       bluetoothDevice
     * @param[out]    address - Device address
     ******************************************************************************
     */

    public String getAddress() {
        return address;
    }
}
