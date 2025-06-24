package com.sebastian.licentafrontendtransport.Home.BottomPage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sebastian.licentafrontendtransport.R;

import java.util.List;

public class NewsRecyclerViewBottomSheetAdapter extends RecyclerView.Adapter<NewsRecyclerViewBottomSheetAdapter.NewsViewHolder>{
    private final List<NewsItem> newsList;

    public NewsRecyclerViewBottomSheetAdapter(List<NewsItem> newsList) {
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout-ul item_news_recycler.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.items_news_recycler, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem item = newsList.get(position);
        holder.newsImage.setImageResource(item.getImageResId());
        holder.newsHeadline.setText(item.getHeadline());
        holder.newsDate.setText(item.getDate());
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        final ImageView newsImage;
        final TextView newsHeadline;
        final TextView newsDate;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImage = itemView.findViewById(R.id.news_image);
            newsHeadline = itemView.findViewById(R.id.news_headline);
            newsDate = itemView.findViewById(R.id.news_date);
        }
    }
}
