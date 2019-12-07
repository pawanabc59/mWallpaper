package com.example.mwallpaper.Model;

import android.widget.ImageView;
import android.widget.TextView;

public class Categories_model {
    String category_image;
    String category_text;

    public Categories_model(String category_image, String category_text) {
        this.category_image = category_image;
        this.category_text = category_text;
    }

    public String getCategory_image() {
        return category_image;
    }

    public void setCategory_image(String category_image) {
        this.category_image = category_image;
    }

    public String getCategory_text() {
        return category_text;
    }

    public void setCategory_text(String category_text) {
        this.category_text = category_text;
    }
}
