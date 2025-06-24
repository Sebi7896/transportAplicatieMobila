package com.sebastian.licentafrontendtransport.Home.BottomPage;

public class NewsItem {
    private final int imageResId;
    private final String headline;
    private final String date;

    public NewsItem(int imageResId, String headline, String date) {
        this.imageResId = imageResId;
        this.headline = headline;
        this.date = date;
    }

    public int getImageResId() { return imageResId; }
    public String getHeadline() { return headline; }
    public String getDate() { return date; }
}

