package com.example.mWallpaper.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mWallpaper.Adapter.WallpaperItemAdapter;
import com.example.mWallpaper.Model.WallpaperItemModel;
import com.example.mWallpaper.R;
import com.example.mWallpaper.SessionManager;
import com.example.mWallpaper.UploadImageActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    RecyclerView homeRecylerView;
    FloatingActionButton floatingUploadButton;
    WallpaperItemAdapter wallpaperItemAdapter;
    ArrayList<WallpaperItemModel> wallpaperItemModels;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    String action = "favourite";

    SessionManager sessionManager;
    ContextThemeWrapper contextThemeWrapper;
    ValueEventListener homeImageValueEventListener;
    Query query;
    String my;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sessionManager = new SessionManager(getContext());
        if (sessionManager.loadNightModeState() == true) {
            contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.darktheme);
        } else {
            contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppTheme);
        }

        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

        // Inflate the layout for this fragment
        View view = localInflater.inflate(R.layout.fragment_home, container, false);

        homeRecylerView = view.findViewById(R.id.homeRecyclerView);
        floatingUploadButton = view.findViewById(R.id.floationUploadButton);

        firebaseDatabase = FirebaseDatabase.getInstance();
//        firebaseDatabase.setPersistenceEnabled(true);
        query = firebaseDatabase.getReference("wallpaper").child("recentlyUploadedImages").child("images").orderByChild("postNumber");

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        wallpaperItemModels = new ArrayList<>();
        wallpaperItemAdapter = new WallpaperItemAdapter(getContext(), wallpaperItemModels, action, getActivity());

        homeImageValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wallpaperItemModels.clear();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    try {
                        wallpaperItemModels.add(new WallpaperItemModel(dataSnapshot1.child("thumbnail").getValue(String.class), dataSnapshot1.child("userId").getValue(String.class), dataSnapshot1.child("category").getValue(String.class)));
//                        Collections.reverse(wallpaperItemModels);
                        wallpaperItemAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        query.addValueEventListener(homeImageValueEventListener);

        my = "changed";

        floatingUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (user == null && !user.isEmailVerified()) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Login First!")
                                .setMessage("You need to login first to Upload the wallpapers.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                    } else {

                        Intent intent = new Intent(getContext(), UploadImageActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Login First!")
                            .setMessage("You need to login first to Upload the wallpapers.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                }
            }
        });

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false);

//        gridLayoutManager.setReverseLayout(true);
//        gridLayoutManager.setStackFromEnd(true);
        homeRecylerView.setLayoutManager(gridLayoutManager);
        homeRecylerView.setAdapter(wallpaperItemAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        query.addValueEventListener(homeImageValueEventListener);
        Log.d("my", "onResume: " + my);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        query.removeEventListener(homeImageValueEventListener);
    }
}
