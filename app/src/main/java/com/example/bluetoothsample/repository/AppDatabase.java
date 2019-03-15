package com.example.bluetoothsample.repository;

import android.content.Context;

import com.example.bluetoothsample.repository.ble.BleDao;
import com.example.bluetoothsample.repository.ble.BleRoom;
import com.example.bluetoothsample.repository.test.TestDao;
import com.example.bluetoothsample.repository.test.TestRoom;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {BleRoom.class, TestRoom.class}, version = 2, exportSchema = false)

public abstract class AppDatabase extends RoomDatabase {

    public abstract BleDao bleDao();

    public abstract TestDao testDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context){
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "test_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;

    }
}
