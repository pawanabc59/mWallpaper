package com.example.mWallpaper.Model;

public class WallpaperItemModel {

    String wallpaperItemURL, anotherUserId, category ;

    public WallpaperItemModel(String wallpaperItemURL, String anotherUserId, String category) {
        this.wallpaperItemURL = wallpaperItemURL;
        this.anotherUserId = anotherUserId;
        this.category = category;
    }

    public String getWallpaperItemURL() {
        return wallpaperItemURL;
    }

    public void setWallpaperItemURL(String wallpaperItemURL) {
        this.wallpaperItemURL = wallpaperItemURL;
    }

    public String getAnotherUserId() {
        return anotherUserId;
    }

    public void setAnotherUserId(String anotherUserId) {
        this.anotherUserId = anotherUserId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
