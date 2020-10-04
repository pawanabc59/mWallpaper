package com.example.mWallpaper;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public Context context;
    int PRIVATE_MODE = 0;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("wallpaper", PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    //    this method will save the nightmode State : True or False
    public void setNightModeState(Boolean state) {
        editor = sharedPreferences.edit();
        editor.putBoolean("NightMode", state);
        editor.commit();
    }

    //    this method will load the night mode state
    public Boolean loadNightModeState() {
        Boolean state = sharedPreferences.getBoolean("NightMode", false);
        return state;
    }

}
