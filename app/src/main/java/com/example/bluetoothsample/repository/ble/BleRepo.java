package com.example.bluetoothsample.repository.ble;

import android.app.Application;

import com.example.bluetoothsample.repository.AppDatabase;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.LiveData;

public class BleRepo {

    private BleDao dao;

    private LiveData<List<BleRoom>> listLiveData;
    private ExecutorService executorService;

    public BleRepo(Application application) {
        AppDatabase appDatabase = AppDatabase.getDatabase(application);
        dao = appDatabase.bleDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<BleRoom>> getAll(){
        if (listLiveData == null){
            listLiveData = dao.getAll();
        }

        return listLiveData;
    }

    public void insert(BleRoom customRoom) {
        executorService.execute(() -> dao.insert(customRoom));
    }

    public void deleteAll( ){
        executorService.execute(() -> dao.deleteAll());
    }
}
