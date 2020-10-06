package com.example.mWallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mWallpaper.Adapter.WallpaperItemAdapter;
import com.example.mWallpaper.Model.WallpaperItemModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AnotherUserProfileActivity extends AppCompatActivity {

    ImageView anotherUserProfileImage, parentImage;
    TextView anotherUserEmailId;
    RecyclerView anotherUserRecyclerView;

    WallpaperItemAdapter wallpaperItemAdapter;
    ArrayList<WallpaperItemModel> wallpaperItemModels;

    SessionManager sessionManager;

    String anotherUserId, parentImagePath;
    String action = "favourite";

    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    ValueEventListener anotherUserProfileValueEventListener, uplodedImageValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(getApplicationContext());
        if (sessionManager.loadNightModeState() == true) {

            setTheme(R.style.darktheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        setContentView(R.layout.activity_another_user_profile);

        anotherUserEmailId = findViewById(R.id.anotherUserEmailId);
        anotherUserProfileImage = findViewById(R.id.anotherUserProfileImage);
        anotherUserRecyclerView = findViewById(R.id.anotherUserRecyclerView);
        parentImage = findViewById(R.id.parentImage);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        Intent intent = getIntent();
        anotherUserId = intent.getExtras().getString("anotherUserId");
        parentImagePath = intent.getExtras().getString("parentImagePath");

        firebaseDatabase = FirebaseDatabase.getInstance();
        mRef = firebaseDatabase.getReference("wallpaper").child("users").child(anotherUserId);

        Picasso.get().load(parentImagePath).into(parentImage);

        wallpaperItemModels = new ArrayList<>();
        wallpaperItemAdapter = new WallpaperItemAdapter(getApplicationContext(), wallpaperItemModels, action, this);

        anotherUserProfileValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                anotherUserEmailId.setText(dataSnapshot.child("email").getValue().toString());
                Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).into(anotherUserProfileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mRef.addValueEventListener(anotherUserProfileValueEventListener);

        uplodedImageValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wallpaperItemModels.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    wallpaperItemModels.add(new WallpaperItemModel(dataSnapshot1.child("uploadedImage").getValue(String.class), dataSnapshot1.child("userId").getValue(String.class), dataSnapshot1.child("category").getValue(String.class)));
                    wallpaperItemAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mRef.child("uploadedImages").child("images").orderByChild("postNumber").addValueEventListener(uplodedImageValueEventListener);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2, RecyclerView.VERTICAL, false);

        anotherUserRecyclerView.setLayoutManager(gridLayoutManager);
        anotherUserRecyclerView.setAdapter(wallpaperItemAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mRef.addValueEventListener(anotherUserProfileValueEventListener);
        mRef.child("uploadedImages").child("images").addValueEventListener(uplodedImageValueEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRef.removeEventListener(anotherUserProfileValueEventListener);

        mRef.child("uploadedImages").child("images").removeEventListener(uplodedImageValueEventListener);
    }

    //    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//
//        if(getFragmentManager().getBackStackEntryCount() > 0)
//            getFragmentManager().popBackStack();
//        else {
//            super.onBackPressed();
//        }
//    }
}
