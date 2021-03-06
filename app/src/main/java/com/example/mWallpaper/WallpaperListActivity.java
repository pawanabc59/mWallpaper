package com.example.mWallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mWallpaper.Adapter.WallpaperItemAdapter;
import com.example.mWallpaper.Model.WallpaperItemModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class WallpaperListActivity extends AppCompatActivity {

    RecyclerView wallpaperRecyclerView;
    WallpaperItemAdapter wallpaperItemAdapter;
    ArrayList<WallpaperItemModel> wallpaperItemModels;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef, mRef2;
    String TAG = "My tag";
    String action = "favourite";
    TextView noWallpaperText;
    ImageView noWallpaperImage;

    SessionManager sessionManager;
    ValueEventListener imageValueEventListener;
//    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(getApplicationContext());
        if (sessionManager.loadNightModeState() == true) {

            setTheme(R.style.darktheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        setContentView(R.layout.activity_wallpaper_list);

//        MobileAds.initialize(this, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {
//            }
//        });
//
//        mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);

        wallpaperRecyclerView = findViewById(R.id.wallpaperRecyclerView);
        noWallpaperText = findViewById(R.id.noWallpaperText);
        noWallpaperImage = findViewById(R.id.noWallpaperImage);

        wallpaperItemModels = new ArrayList<>();

        wallpaperItemAdapter = new WallpaperItemAdapter(getApplicationContext(), wallpaperItemModels, action, WallpaperListActivity.this);

        Intent intent = getIntent();
        final String key = intent.getExtras().getString("key");

        firebaseDatabase = FirebaseDatabase.getInstance();

        mRef = firebaseDatabase.getReference("wallpaper").child("images").child(key).child("images");

        imageValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wallpaperItemModels.clear();
                if (dataSnapshot.exists()) {
                    wallpaperRecyclerView.setVisibility(View.VISIBLE);
                    noWallpaperText.setVisibility(View.GONE);
                    noWallpaperImage.setVisibility(View.GONE);
                    for (final DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
//                    mRef2 = mRef.child(dataSnapshot1.getKey());
                        try {
                            wallpaperItemModels.add(new WallpaperItemModel(dataSnapshot1.child("thumbnail").getValue().toString(), dataSnapshot1.child("userId").getValue().toString(), dataSnapshot1.child("category").getValue(String.class)));
//                    Log.d(TAG, "onDataChange: "+dataSnapshot1.child("thumbnail").getValue().toString());
//                        Collections.reverse(wallpaperItemModels);
                            wallpaperItemAdapter.notifyDataSetChanged();
//                    wallpaperItemModels.add(new WallpaperItemModel("https://firebasestorage.googleapis.com/v0/b/mwallpaper-6feeb.appspot.com/o/wallpaper%2Fcategories%2Fnature%2Fnature.jpg?alt=media&token=d17176fa-8a8e-48bc-af4a-8482a295e249"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    wallpaperRecyclerView.setVisibility(View.GONE);
                    noWallpaperText.setVisibility(View.VISIBLE);
                    noWallpaperImage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mRef.addValueEventListener(imageValueEventListener);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2, RecyclerView.VERTICAL, false);

        wallpaperRecyclerView.setLayoutManager(gridLayoutManager);
        wallpaperRecyclerView.setAdapter(wallpaperItemAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRef.removeEventListener(imageValueEventListener);
    }
}
