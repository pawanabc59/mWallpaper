package com.example.mwallpaper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.mwallpaper.Adapter.WallpaperItemAdapter;
import com.example.mwallpaper.Model.WallpaperItemModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyUploadsActivity extends AppCompatActivity {

    SessionManager sessionManager;
    RecyclerView myUploadsRecyclerView;
    WallpaperItemAdapter wallpaperItemAdapter;
    ArrayList<WallpaperItemModel> wallpaperItemModels;
    String action="myUploads";

    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(getApplicationContext());
        if (sessionManager.loadNightModeState() == true) {

            setTheme(R.style.darktheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        setContentView(R.layout.activity_my_uploads);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mRef = firebaseDatabase.getReference("wallpaper").child("users");

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userId = user.getUid();

        myUploadsRecyclerView = findViewById(R.id.myUploadRecyclerView);

        wallpaperItemModels = new ArrayList<>();
        wallpaperItemAdapter = new WallpaperItemAdapter(getApplicationContext(), wallpaperItemModels, action);

        mRef.child(userId).child("uploadedImages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    wallpaperItemModels.add(new WallpaperItemModel(dataSnapshot1.child("uploadedImage").getValue().toString()));
                    wallpaperItemAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2 ,RecyclerView.VERTICAL, false);
        myUploadsRecyclerView.setLayoutManager(gridLayoutManager);
        myUploadsRecyclerView.setAdapter(wallpaperItemAdapter);

    }
}
