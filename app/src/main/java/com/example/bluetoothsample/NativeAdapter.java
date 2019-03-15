package com.example.bluetoothsample;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.bluetoothsample.databinding.RecyclerTestBinding;
import com.example.bluetoothsample.repository.test.TestRoom;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class NativeAdapter extends ListAdapter<TestRoom, NativeAdapter.BleViewHolder> {

    private static String TAG = BleAdapter.class.getSimpleName();

    protected NativeAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public BleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_test, parent, false);
        return new BleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BleViewHolder holder, int position) {
        holder.getRecyclerTestBinding().setData(getItem(position));

    }

    private static final DiffUtil.ItemCallback<TestRoom> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<TestRoom>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull TestRoom oldModel, @NonNull TestRoom newModel) {
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    return (oldModel.getMac().equals(newModel.getMac()));
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull TestRoom oldModel, @NonNull TestRoom newModel) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldModel.equals(newModel);
                }
            };


    static class BleViewHolder extends RecyclerView.ViewHolder {

        private RecyclerTestBinding recyclerTestBinding;

        public BleViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(v -> {
                Log.d(TAG, "ITEM TOUCH" + getAdapterPosition());

            });
            recyclerTestBinding = DataBindingUtil.bind(itemView);
        }

        public RecyclerTestBinding getRecyclerTestBinding() {
            return recyclerTestBinding;
        }
    }
}

