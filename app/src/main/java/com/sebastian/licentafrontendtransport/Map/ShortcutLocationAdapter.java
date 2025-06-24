package com.sebastian.licentafrontendtransport.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.sebastian.licentafrontendtransport.R;

import java.util.List;

public class ShortcutLocationAdapter extends RecyclerView.Adapter<ShortcutLocationAdapter.ViewHolder> {


    public interface OnShortcutClickListener {
        void onShortcutClick(ShortcutLocation shortcut);
    }

    private final List<ShortcutLocation> items;
    private final OnShortcutClickListener listener;
    private final Context context;

    public ShortcutLocationAdapter(Context context, List<ShortcutLocation> items, OnShortcutClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quick_route, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ShortcutLocation item = items.get(position);
        holder.button.setText(item.name);
        holder.button.setOnClickListener(v -> listener.onShortcutClick(item));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialButton button;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.btn_shortcut);
        }
    }
}