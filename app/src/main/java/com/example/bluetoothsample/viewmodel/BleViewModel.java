package com.example.bluetoothsample.viewmodel;

import android.app.Application;

import com.example.bluetoothsample.repository.ble.BleRepo;
import com.example.bluetoothsample.repository.ble.BleRoom;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class BleViewModel extends AndroidViewModel {
    
    private BleRepo repo;
    private LiveData<List<BleRoom>> listLiveData;

    public BleViewModel(@NonNull Application application) {
        super(application);
        repo = new BleRepo(application);
    }
    
    public LiveData<List<BleRoom>> getListLiveData() {
        if (listLiveData == null){
            listLiveData = repo.getAll();
        }
        return listLiveData;
    }

    public void insert(BleRoom BleRoom) { repo.insert(BleRoom); }

    public void deleteAll(){repo.deleteAll();}

}
