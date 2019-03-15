package com.example.bluetoothsample.repository.test;

import android.app.Application;

import com.example.bluetoothsample.repository.AppDatabase;
import com.example.bluetoothsample.repository.ble.BleDao;
import com.example.bluetoothsample.repository.ble.BleRoom;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.LiveData;

public class TestRepo {

    private TestDao dao;

    private LiveData<List<TestRoom>> listLiveData;
    private ExecutorService executorService;

    public TestRepo(Application application) {
        AppDatabase appDatabase = AppDatabase.getDatabase(application);
        dao = appDatabase.testDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<TestRoom>> getAll() {
        if (listLiveData == null) {
            listLiveData = dao.getAll();
        }

        return listLiveData;
    }

    public void insert(TestRoom customRoom) {
        executorService.execute(() -> dao.insert(customRoom));
    }

    public void deleteAll() {
        executorService.execute(() -> dao.deleteAll());
    }

}
