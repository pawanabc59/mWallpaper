package com.example.mwallpaper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.mwallpaper.Adapter.WallpaperItemAdapter;
import com.example.mwallpaper.Model.WallpaperItemModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class WallpaperListActivity extends AppCompatActivity {

    RecyclerView wallpaperRecyclerView;
    WallpaperItemAdapter wallpaperItemAdapter;
    ArrayList<WallpaperItemModel> wallpaperItemModels;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef,mRef2;
    String TAG = "My tag";
    String action = "favourite";

    SessionManager sessionManager;

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

        wallpaperRecyclerView = findViewById(R.id.wallpaperRecyclerView);

        wallpaperItemModels = new ArrayList<>();

        wallpaperItemAdapter = new WallpaperItemAdapter(getApplicationContext(),wallpaperItemModels,action);

        Intent intent = getIntent();
        final String key = intent.getExtras().getString("key");

        firebaseDatabase = FirebaseDatabase.getInstance();

        mRef = firebaseDatabase.getReference("wallpaper").child("images").child(key);

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
//                    mRef2 = mRef.child(dataSnapshot1.getKey());
                    wallpaperItemModels.add(new WallpaperItemModel(dataSnapshot1.child("thumbnail").getValue().toString()));
//                    Log.d(TAG, "onDataChange: "+dataSnapshot1.child("thumbnail").getValue().toString());
                    wallpaperItemAdapter.notifyDataSetChanged();
//                    wallpaperItemModels.add(new WallpaperItemModel("https://firebasestorage.googleapis.com/v0/b/mwallpaper-6feeb.appspot.com/o/wallpaper%2Fcategories%2Fnature%2Fnature.jpg?alt=media&token=d17176fa-8a8e-48bc-af4a-8482a295e249"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2, RecyclerView.VERTICAL,false);

        wallpaperRecyclerView.setLayoutManager(gridLayoutManager);
        wallpaperRecyclerView.setAdapter(wallpaperItemAdapter);
    }
}
