package com.app_devs.DonateEasy.Users;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.app_devs.DonateEasy.PlacesPOJO;
import com.app_devs.DonateEasy.R;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private List<PlacesPOJO.CustomA> stLstStores;
    private List<StoreModel> models;

    public RecyclerViewAdapter(List<PlacesPOJO.CustomA> stores, List<StoreModel> storeModels) {
        stLstStores = stores;
        models = storeModels;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final  View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ngo_cards, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.setData(stLstStores.get(holder.getAdapterPosition()), holder, models.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return Math.min(5, stLstStores.size());
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView name;
        AppCompatTextView address;
        AppCompatTextView distance;
        StoreModel model;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.ngo_name);
            address = itemView.findViewById(R.id.ngo_address);
            distance = itemView.findViewById(R.id.ngo_distance);
        }

        @SuppressLint("SetTextI18n")
        public void setData(PlacesPOJO.CustomA info, MyViewHolder holder, StoreModel storeModel) {

            this.model = storeModel;

            holder.distance.setText(model.distance + "\n"  + model.duration);
            holder.name.setText(info.name);
            holder.address.setText(info.vicinity);
        }
    }
}
