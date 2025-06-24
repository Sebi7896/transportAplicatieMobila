package com.sebastian.licentafrontendtransport.Home.Subscriptions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sebastian.licentafrontendtransport.R;
import com.sebastian.licentafrontendtransport.models.UrbisPassCard.UrbisPassCardMember;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    private final Context context;
    private final List<UrbisPassCardMember> items;
    private final SimpleDateFormat inputDateFormat;
    private final SimpleDateFormat outputDateFormat;

    public SubscriptionAdapter(Context context, List<UrbisPassCardMember> items) {
        this.context = context;
        this.items = items;
        inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        outputDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subscription, parent, false);
        return new SubscriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        UrbisPassCardMember member = items.get(position);

        String status = member.getStatus();
        if (status != null) {
            String displayStatus = status.substring(0,1).toUpperCase() + status.substring(1).toLowerCase();
            holder.tvStatus.setText("Status: " + displayStatus);
        } else {
            holder.tvStatus.setText("Status: N/A");
        }

        String fullName = null;
        try {
            fullName = member.getPerson().getFullName();
        } catch (Exception ignored) {}
        if (fullName != null && !fullName.isEmpty()) {
            holder.tvNameCard.setText(fullName);
        }

        // Expiry date: parse and format
        String expRaw = member.getExpirationDate();
        if (expRaw != null && !expRaw.isEmpty()) {
            String formatted;
            try {
                Date date = inputDateFormat.parse(expRaw);
                formatted = outputDateFormat.format(date);
            } catch (ParseException e) {
                // If parsing fails, just show raw
                formatted = expRaw;
            }
            holder.tvExpiry.setText("Expires: " + formatted);
        } else {
            holder.tvExpiry.setText("Expires: N/A");
        }

    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    // ViewHolder
    static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvStatus;
        TextView tvNameCard;
        TextView tvExpiry;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.ivLogo);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvNameCard = itemView.findViewById(R.id.tvNameCard);
            tvExpiry = itemView.findViewById(R.id.tvExpiry);
        }
    }

    public void updateList(List<UrbisPassCardMember> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }
}