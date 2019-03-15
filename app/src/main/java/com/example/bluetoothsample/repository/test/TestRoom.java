package com.example.bluetoothsample.repository.test;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TestRoom {

    @NonNull
    @PrimaryKey
    private String mac;
    private String name;

    public TestRoom(@NonNull String mac, String name) {
        this.mac = mac;
        this.name = name;
    }

    @NonNull
    public String getMac() {
        return mac;
    }

    public void setMac(@NonNull String mac) {
        this.mac = mac;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
