package com.sebastian.licentafrontendtransport.Map;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.sebastian.licentafrontendtransport.Map.model.Leg;
import com.sebastian.licentafrontendtransport.Map.model.Route;
import com.sebastian.licentafrontendtransport.Map.model.Step;
import com.sebastian.licentafrontendtransport.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.RouteViewHolder> {
    private final Context context;
    private final List<Route> routes;
    public interface OnItemClickListener {
        void onItemClick(Route route);
    }

    private final OnItemClickListener listener;

    public RoutesAdapter(@NonNull Context context, @NonNull List<Route> routes, OnItemClickListener listener) {
        this.context = context;
        this.routes = routes;
        this.listener = listener;
    }

    public static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView tvWalkStart;
        ChipGroup chipGroup;
        TextView tvWalkEnd;
        TextView tvTotalDuration;
        TextView tvTimeRange;
        public RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWalkStart = itemView.findViewById(R.id.tvWalkStart);
            chipGroup = itemView.findViewById(R.id.chipGroupRoutes);
            tvWalkEnd = itemView.findViewById(R.id.tvWalkEnd);
            tvTotalDuration = itemView.findViewById(R.id.tvTotalDuration);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
        }
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.route_layout, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        Route route = routes.get(position);
        // Bind total duration
        holder.tvTotalDuration.setText(route.legs.get(0).duration.text);
        // Combine steps
        List<Step> steps = new ArrayList<>();
        for (Leg leg : route.legs) {
            if (leg.steps != null) {
                steps.addAll(leg.steps);
            }
        }
        // Walking durations
        if (!steps.isEmpty()) {
            Step firstStep = steps.get(0);
            Step lastStep = steps.get(steps.size() - 1);
            if (firstStep.duration != null) {
                holder.tvWalkStart.setText(String.valueOf(firstStep.duration.value / 60));
            }
            if (lastStep.duration != null) {
                holder.tvWalkEnd.setText(String.valueOf(lastStep.duration.value / 60));
            }
        } else {
            holder.tvWalkStart.setText("");
            holder.tvWalkEnd.setText("");
        }
        // Populate vehicle chips
        holder.chipGroup.removeAllViews();
        Set<String> vehicleNames = new LinkedHashSet<>();
        for (Step step : steps) {
            if ("TRANSIT".equals(step.travel_mode)) {
                String lineName;
                if (step.transit_details.line.short_name == null) {
                    lineName = step.transit_details.line.name;
                } else {
                    lineName = step.transit_details.line.short_name;
                }
                vehicleNames.add(lineName);
            }
        }
        for (String name : vehicleNames) {
            Chip chip = new Chip(context);
            chip.setText(name);
            chip.setChipBackgroundColorResource(R.color.white);
            // Make chip non-interactive
            chip.setClickable(false);
            chip.setCheckable(false);
            chip.setFocusable(false);
            chip.setChipStrokeColor(ColorStateList.valueOf(context.getColor(R.color.red_color)));
            holder.chipGroup.addView(chip);
        }
        // Time range
        if(route.legs.get(0).departure_time != null && route.legs.get(0).arrival_time != null) {
            holder.tvTimeRange.setText(String.format("%s - %s", route.legs.get(0).departure_time.text, route.legs.get(0).arrival_time.text));
        }
        holder.tvTimeRange.setVisibility(View.VISIBLE);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(route);
            }
        });
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }
}
