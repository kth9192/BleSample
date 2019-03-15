package com.example.bluetoothsample.repository.test;

import com.example.bluetoothsample.repository.ble.BleRoom;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface TestDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(TestRoom... testRooms);

    @Query("DELETE FROM TestRoom")
    void deleteAll();

    @Query("SELECT * from TestRoom")
    LiveData<List<TestRoom>> getAll();
}
