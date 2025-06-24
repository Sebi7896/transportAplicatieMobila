package com.sebastian.licentafrontendtransport.Home.BottomPage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sebastian.licentafrontendtransport.R;

import java.util.List;

public class SliderImageBottomSheetAdapter extends RecyclerView.Adapter<SliderImageBottomSheetAdapter.ViewHolder> {

    private final List<Integer> imageResIds;
    private final List<String> captions;
    private final @ColorInt int textColor;

    // Constructor nou: primește și lista de caption-uri
    public SliderImageBottomSheetAdapter(List<Integer> imageResIds, List<String> captions, int textColor) {

        if (imageResIds.size() != captions.size()) {
            throw new IllegalArgumentException("Lista de imagini și lista de capturi trebuie să aibă aceeași lungime.");
        }
        this.imageResIds = imageResIds;
        this.captions = captions;
        this.textColor = textColor;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_slider_bottom_sheet_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.imageView.setImageResource(imageResIds.get(position));
        holder.captionView.setText(captions.get(position));
        Context context = holder.itemView.getContext();
        holder.captionView.setTextColor(ContextCompat.getColor(context, textColor));

    }

    @Override
    public int getItemCount() {
        return imageResIds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView captionView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.slider_image);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            captionView = itemView.findViewById(R.id.bottom_sheet_slider_text);
        }
    }
}