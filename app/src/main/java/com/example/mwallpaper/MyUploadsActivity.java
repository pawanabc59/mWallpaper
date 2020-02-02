package com.example.mwallpaper;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Collections;

public class MyUploadsActivity extends AppCompatActivity {

    SessionManager sessionManager;
    RecyclerView myUploadsRecyclerView;
    WallpaperItemAdapter wallpaperItemAdapter;
    ArrayList<WallpaperItemModel> wallpaperItemModels;
    String action = "myUploads";

    ImageView myUploadmbImage;
    TextView myUploadshowText;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    String userId;
    ValueEventListener uplodedValueEventListener;

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

        myUploadmbImage = findViewById(R.id.myUploadmbImage);
        myUploadshowText = findViewById(R.id.myUploadshowText);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userId = user.getUid();

        myUploadsRecyclerView = findViewById(R.id.myUploadRecyclerView);

        wallpaperItemModels = new ArrayList<>();
        wallpaperItemAdapter = new WallpaperItemAdapter(getApplicationContext(), wallpaperItemModels, action, MyUploadsActivity.this);

        uplodedValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()){
                    myUploadmbImage.setVisibility(View.VISIBLE);
                    myUploadshowText.setVisibility(View.VISIBLE);
                    myUploadsRecyclerView.setVisibility(View.GONE);
                }
                else {
                    myUploadsRecyclerView.setVisibility(View.VISIBLE);
                    myUploadshowText.setVisibility(View.GONE);
                    myUploadmbImage.setVisibility(View.GONE);
                    wallpaperItemModels.clear();
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        try {
                            wallpaperItemModels.add(new WallpaperItemModel(dataSnapshot1.child("uploadedImage").getValue().toString(), dataSnapshot1.child("userId").getValue().toString()));
//                            Collections.reverse(wallpaperItemModels);
                            wallpaperItemAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mRef.child(userId).child("uploadedImages").child("images").orderByChild("postNumber").addValueEventListener(uplodedValueEventListener);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2, RecyclerView.VERTICAL, false);
        myUploadsRecyclerView.setLayoutManager(gridLayoutManager);
        myUploadsRecyclerView.setAdapter(wallpaperItemAdapter);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getSupportFragmentManager().popBackStack();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRef.child(userId).child("uploadedImages").child("images").removeEventListener(uplodedValueEventListener);
    }
}
