package com.sebastian.licentafrontendtransport.Alerts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sebastian.licentafrontendtransport.R;

import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {
    private final Context context;
    private final List<AlertItem> alertList;
    public AlertAdapter(Context context, List<AlertItem> alertList) {
        this.context = context;
        this.alertList = alertList;
    }
    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alert, parent, false);
        return new AlertViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        AlertItem item = alertList.get(position);
        holder.tvAlertTitle.setText(item.getTitle());
        holder.tvAlertMessage.setText(item.getMessage());
        Date date = item.getTimestamp().toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault());
        holder.tvAlertTime.setText(sdf.format(date));
    }
    @Override
    public int getItemCount() {
        return alertList != null ? alertList.size() : 0;
    }
    static class AlertViewHolder extends RecyclerView.ViewHolder {
        TextView tvAlertTitle;
        TextView tvAlertMessage;
        TextView tvAlertTime;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlertTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvAlertMessage = itemView.findViewById(R.id.tvAlertMessage);
            tvAlertTime = itemView.findViewById(R.id.tvAlertTime);
        }
    }
}