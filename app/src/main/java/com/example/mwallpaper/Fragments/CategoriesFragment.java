package com.example.mwallpaper.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mwallpaper.Adapter.CategoryAdapter;
import com.example.mwallpaper.Model.Categories_model;
import com.example.mwallpaper.R;
import com.example.mwallpaper.SessionManager;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Map;

public class CategoriesFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<Categories_model> category_list;
    CategoryAdapter categoryAdapter;
    Context mContext;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef;

    SessionManager sessionManager;
    ContextThemeWrapper contextThemeWrapper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sessionManager = new SessionManager(getContext());
        if (sessionManager.loadNightModeState()==true){
            contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.darktheme);
        }
        else{
            contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppTheme);
        }

        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

        // Inflate the layout for this fragment
        View view = localInflater.inflate(R.layout.fragment_categories, container, false);

        recyclerView = view.findViewById(R.id.categories_recyclerview);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mRef = firebaseDatabase.getReference("wallpaper").child("categories");

        category_list = new ArrayList<>();

        categoryAdapter = new CategoryAdapter(getContext(), category_list);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),1, RecyclerView.VERTICAL,false);

        mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                Categories_model categories_model = dataSnapshot.getValue(Categories_model.class);
                Map<String,String> data = (Map<String, String>) dataSnapshot.getValue();

                category_list.add(new Categories_model(data.get("thumbnail"), dataSnapshot.getKey()));

                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setAdapter(categoryAdapter);

        return view;
    }

}
