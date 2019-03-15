package com.example.bluetoothsample.repository.ble;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface BleDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(BleRoom... bleRooms);

    @Query("DELETE FROM BleRoom")
    void deleteAll();

    @Query("SELECT * from BleRoom")
    LiveData<List<BleRoom>> getAll();
}
