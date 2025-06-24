package com.sebastian.licentafrontendtransport.Map;

// imports
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sebastian.licentafrontendtransport.Map.model.Step;
import com.sebastian.licentafrontendtransport.R;

import java.util.List;
import java.util.ArrayList;

public class StepsAdapter extends RecyclerView.Adapter<StepsAdapter.ViewHolder> {
    private final List<Step> steps;
    private final List<Integer> stepColors;

    // Constructor
    public StepsAdapter(List<Step> steps, List<Integer> stepColors) {
        this.steps = steps;
        this.stepColors = stepColors != null && !stepColors.isEmpty() ? stepColors : new ArrayList<>();
    }

    @NonNull
    @Override
    public StepsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_walk_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepsAdapter.ViewHolder holder, int position) {
        Step step = steps.get(position);

        // Set the side-bar color if available
        if (holder.vRouteColor != null && position < stepColors.size()) {
            holder.vRouteColor.setBackgroundColor(stepColors.get(position));
        }

        // 1. Instruction text: some APIs return HTML-formatted instructions.
        String instructionHtml = null;
        try {
            // Adjust field name: if your model has html_instructions or instruction
            instructionHtml = step.html_instructions != null ? step.html_instructions :  "";
        } catch (Exception ignored) {}
        if (instructionHtml != null) {
            // Html.fromHtml: for newer API levels, you can use Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            holder.tvInstruction.setText(Html.fromHtml(instructionHtml, Html.FROM_HTML_MODE_LEGACY));
        } else {
            holder.tvInstruction.setText("");
        }

        // 2. Sub-info: distance and duration. Adjust fields based on your model.
        String distanceText = "";
        String durationText = "";
        try {
            if (step.distance != null && step.distance.text != null) {
                distanceText = step.distance.text;
            }
            if (step.duration != null && step.duration.text != null) {
                durationText = step.duration.text;
            }
        } catch (Exception ignored) {}
        String subinfo = "";
        if (!distanceText.isEmpty() && !durationText.isEmpty()) {
            subinfo = distanceText + " (" + durationText + ")";
        } else if (!distanceText.isEmpty()) {
            subinfo = distanceText;
        } else if (!durationText.isEmpty()) {
            subinfo = durationText;
        }
        holder.tvSubInfo.setText(subinfo);

        // 3. Optional: icon based on travel mode or transit details
        // If your Step model has a field like travel_mode (e.g., "WALKING", "TRANSIT", "DRIVING"), set an icon.
        if (holder.ivIcon != null) {
            String mode = null;
            try {
                mode = step.travel_mode;
            } catch (Exception ignored) {}
            if (mode != null) {
                holder.ivIcon.setVisibility(View.VISIBLE);
                switch (mode.toUpperCase()) {
                    case "WALKING":
                        holder.ivIcon.setImageResource(R.drawable.walk_icon); // replace with your drawable
                        break;
                    case "TRANSIT":
                        // Enhanced transit step handling
                        if (step.transit_details != null && step.transit_details.line != null) {
                            // Extract line info
                            String lineName = null;
                            try {
                                lineName = step.transit_details.line.short_name != null
                                        ? step.transit_details.line.short_name
                                        : step.transit_details.line.name;
                            } catch (Exception ignored) {}
                            String headsign = null;
                            try {
                                headsign = step.transit_details.headsign;
                            } catch (Exception ignored) {}
                            // Set instruction text to include line name and headsign if available
                            if (lineName != null) {
                                String transitInstruction = "Take " + lineName;
                                if (headsign != null && !headsign.isEmpty()) {
                                    transitInstruction += " toward " + headsign;
                                }
                                holder.tvInstruction.setText(transitInstruction);
                            }
                            // Icon based on vehicle type
                            try {
                                String vehicleType = step.transit_details.line.vehicle != null
                                        ? step.transit_details.line.vehicle.type
                                        : null;
                                if (vehicleType != null) {
                                    switch (vehicleType.toUpperCase()) {
                                        case "BUS":
                                            holder.ivIcon.setImageResource(R.drawable.bus_icon);
                                            break;
                                        case "TRAM":
                                            holder.ivIcon.setImageResource(R.drawable.tram_icon);
                                            break;
                                        case "SUBWAY":
                                        case "METRO":
                                            holder.ivIcon.setImageResource(R.drawable.subway_icon);
                                            break;
                                        default:
                                            holder.ivIcon.setImageResource(R.drawable.transit_icon);
                                            break;
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                        break;
                    default:
                        holder.ivIcon.setVisibility(View.GONE);
                        break;
                }
            } else {
                holder.ivIcon.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return steps != null ? steps.size() : 0;
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInstruction;
        TextView tvSubInfo;
        ImageView ivIcon;
        View vRouteColor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInstruction = itemView.findViewById(R.id.tv_instruction);
            tvSubInfo = itemView.findViewById(R.id.tv_step_subinfo);
            ivIcon = itemView.findViewById(R.id.iv_step_icon);
            vRouteColor = itemView.findViewById(R.id.v_route_color);
        }
    }
}