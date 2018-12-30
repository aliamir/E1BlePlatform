package com.example.amir.e1bleplatform;

public class BleDevice{
    private String name;
    private String address;
    public final String[] serviceStrings = {
            "bleDevice",
            "remapCmd",
            "LedCmd"};

    public enum serviceStringsIndex {
        BLEDEVICE,
        CONNECTION_STATUS,
        ACK_REMAP_CMD,
        ACK_LED_CMD,
        ACK_REFLASH_CMD,
        REMAP_CMD,
        LED_CMD,
        REFLASH_CMD

    }

    public BleDevice(String name, String address) {
        super();
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
