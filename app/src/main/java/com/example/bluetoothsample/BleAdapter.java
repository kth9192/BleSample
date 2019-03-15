package com.example.bluetoothsample;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ederdoski.simpleble.utils.BluetoothLEHelper;
import com.example.bluetoothsample.databinding.RecyclerBleBinding;
import com.example.bluetoothsample.repository.ble.BleRoom;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;


public class BleAdapter extends ListAdapter<BleRoom, BleAdapter.BleViewHolder> {

    private static String TAG = BleAdapter.class.getSimpleName();

    protected BleAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public BleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_ble, parent, false);
        return new BleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BleViewHolder holder, int position) {
        holder.getRecyclerBleBinding().setData(getItem(position));

    }

    private static final DiffUtil.ItemCallback<BleRoom> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<BleRoom>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull BleRoom oldModel, @NonNull BleRoom newModel) {
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    return (oldModel.getMac().equals(newModel.getMac()));
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull BleRoom oldModel, @NonNull BleRoom newModel) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldModel.equals(newModel);
                }
            };


    static class BleViewHolder extends RecyclerView.ViewHolder {

        private RecyclerBleBinding recyclerBleBinding;

        public BleViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> {
                Log.d(TAG, "ITEM TOUCH" + getAdapterPosition());

            });
            recyclerBleBinding = DataBindingUtil.bind(itemView);
        }

        public RecyclerBleBinding getRecyclerBleBinding() {
            return recyclerBleBinding;
        }
    }
}
