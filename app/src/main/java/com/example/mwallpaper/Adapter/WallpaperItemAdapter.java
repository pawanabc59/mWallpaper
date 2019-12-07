package com.example.mwallpaper.Adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mwallpaper.Model.WallpaperItemModel;
import com.example.mwallpaper.R;
import com.example.mwallpaper.SingleWallpaperActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class WallpaperItemAdapter extends RecyclerView.Adapter<WallpaperItemAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<WallpaperItemModel> mList;
    String TAG = "My tag";
    String userId;
    String maction;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    public WallpaperItemAdapter(Context mContext, ArrayList<WallpaperItemModel> mList, String action) {
        this.mContext = mContext;
        this.mList = mList;
        this.maction = action;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        View view = layoutInflater.inflate(R.layout.wallpaper_item,parent,false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        ImageView wallpaperItemImage = holder.wallpaperItemImage;
        CardView wallpaperCardView = holder.wallpaperCardview;
        final ImageView favouriteFilled = holder.favouriteFilled;
        final ImageView favouriteUnfilled = holder.favouriteUnfilled;
        ImageView deleteImage = holder.deleteImage;

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mRef = firebaseDatabase.getReference("wallpaper").child("users");
        user = firebaseAuth.getCurrentUser();
        userId = user.getUid();

        Picasso.get().load(mList.get(position).getWallpaperItemURL()).into(wallpaperItemImage);
//        Log.d(TAG, "onBindViewHolder: "+mList.get(position).getWallpaperItemURL());

        wallpaperCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SingleWallpaperActivity.class);
                intent.putExtra("image_path", mList.get(position).getWallpaperItemURL());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });

        if (maction.equals("favourite")) {

            mRef.child(userId).child("favouriteImages").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                        if (dataSnapshot1.child("thumbnail").getValue().toString().equals(mList.get(position).getWallpaperItemURL())){
                            favouriteFilled.setVisibility(View.VISIBLE);
                            favouriteUnfilled.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            favouriteUnfilled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    favouriteFilled.setVisibility(View.VISIBLE);
                    favouriteUnfilled.setVisibility(View.GONE);

                    String pushId = mRef.push().getKey();

                    mRef.child(userId).child("favouriteImages").child(pushId).child("thumbnail").setValue(mList.get(position).getWallpaperItemURL());

                }
            });

            favouriteFilled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    favouriteFilled.setVisibility(View.GONE);
                    favouriteUnfilled.setVisibility(View.VISIBLE);

                    mRef.child(userId).child("favouriteImages").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                if (dataSnapshot1.child("thumbnail").getValue().toString().equals(mList.get(position).getWallpaperItemURL())) {
                                    mRef.child(userId).child("favouriteImages").child(dataSnapshot1.toString()).removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            });
        }
        else {
//              here code for delete will occur
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView wallpaperItemImage, favouriteFilled, favouriteUnfilled, deleteImage;
        CardView wallpaperCardview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            wallpaperItemImage = itemView.findViewById(R.id.wallpaperItemImage);
            wallpaperCardview = itemView.findViewById(R.id.wallpaper_item_cardview);
            favouriteFilled = itemView.findViewById(R.id.fav_filled);
            favouriteUnfilled = itemView.findViewById(R.id.fav_unfilled);
            deleteImage = itemView.findViewById(R.id.deleteImage);
        }
    }

}
