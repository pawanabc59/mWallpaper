package com.example.mwallpaper.Model;

public class WallpaperItemModel {

    String wallpaperItemURL;
    String anotherUserId;

    public WallpaperItemModel(String wallpaperItemURL, String anotherUserId) {
        this.wallpaperItemURL = wallpaperItemURL;
        this.anotherUserId = anotherUserId;
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
}
