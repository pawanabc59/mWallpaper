package com.example.mwallpaper.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mwallpaper.Adapter.WallpaperItemAdapter;
import com.example.mwallpaper.Model.WallpaperItemModel;
import com.example.mwallpaper.R;
import com.example.mwallpaper.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavouriteFragment extends Fragment {

    SessionManager sessionManager;
    ContextThemeWrapper contextThemeWrapper;

    WallpaperItemAdapter wallpaperItemAdapter;
    ArrayList<WallpaperItemModel> wallpaperItemModels;

    RecyclerView favouriteRecyclerView;
    ImageView mbImage;
    TextView showText;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    String TAG = "my";
    String userId;
    String action = "favourite";
    ValueEventListener favouriteValueEventListener, favouriteImageValueEventListener;

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
        View view = localInflater.inflate(R.layout.fragment_favourite, container, false);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mRef = firebaseDatabase.getReference("wallpaper").child("users");

        wallpaperItemModels = new ArrayList<>();
        wallpaperItemAdapter = new WallpaperItemAdapter(getContext(), wallpaperItemModels, action, getActivity());

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        favouriteRecyclerView = view.findViewById(R.id.favoriteRecyclerView);
        mbImage = view.findViewById(R.id.mbImage);
        showText = view.findViewById(R.id.showText);
        Log.d(TAG, "onCreateView: it is in the favourite fragment");

        if (user == null) {
            favouriteRecyclerView.setVisibility(View.GONE);
            mbImage.setVisibility(View.VISIBLE);
            showText.setVisibility(View.VISIBLE);

            showText.setText("You need to login to add the wallpapers to your favourites!!!");
            Log.d(TAG, "onCreateView: it is in the not login if part");

        } else {
            userId = user.getUid();
            Log.d(TAG, "onCreateView: it comes abouve null favourite");
            try {

                favouriteValueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
//                            Log.d(TAG, "onCreateView: It comes into null favourite");
                            favouriteRecyclerView.setVisibility(View.GONE);
                            mbImage.setVisibility(View.VISIBLE);
                            showText.setText("Add wallpapers to favourite to see them here");
                            showText.setVisibility(View.VISIBLE);
                        } else {
//                            Log.d(TAG, "onCreateView: it comes in else part of null part");
                            mbImage.setVisibility(View.GONE);
                            showText.setVisibility(View.GONE);
                            favouriteRecyclerView.setVisibility(View.VISIBLE);

                            favouriteImageValueEventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    wallpaperItemModels.clear();
                                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                        try {
                                            wallpaperItemModels.add(new WallpaperItemModel(dataSnapshot1.child("thumbnail").getValue().toString(), dataSnapshot1.child("userId").getValue().toString()));
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

                            mRef.child(userId).child("favouriteImages").child("images").orderByChild("postNumber").addValueEventListener(favouriteImageValueEventListener);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };

                mRef.child(userId).child("favouriteImages").child("images").addValueEventListener(favouriteValueEventListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, RecyclerView.VERTICAL, false);

        favouriteRecyclerView.setLayoutManager(gridLayoutManager);
        favouriteRecyclerView.setAdapter(wallpaperItemAdapter);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (user != null) {

            try {
                mRef.child(userId).child("favouriteImages").removeEventListener(favouriteValueEventListener);

                mRef.child(userId).child("favouriteImages").child("images").orderByChild("postNumber").addValueEventListener(favouriteImageValueEventListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
