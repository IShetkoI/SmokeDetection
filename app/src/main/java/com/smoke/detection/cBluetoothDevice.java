package com.smoke.detection;

public class cBluetoothDevice {

    private final String name;
    private final String address;

    public cBluetoothDevice(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
