package com.example.bluetoothsample.viewmodel;

import android.app.Application;

import com.example.bluetoothsample.repository.ble.BleRepo;
import com.example.bluetoothsample.repository.test.TestRepo;
import com.example.bluetoothsample.repository.test.TestRoom;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class TestViewModel extends AndroidViewModel {

    private TestRepo repo;
    private LiveData<List<TestRoom>> listLiveData;

    public TestViewModel(@NonNull Application application) {
        super(application);
        repo = new TestRepo(application);
    }

    public LiveData<List<TestRoom>> getListLiveData() {
        if (listLiveData == null) {
            listLiveData = repo.getAll();
        }
        return listLiveData;
    }

    public void insert(TestRoom testRoom) {
        repo.insert(testRoom);
    }

    public void deleteAll() {
        repo.deleteAll();
    }

}
