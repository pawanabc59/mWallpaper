package com.example.mwallpaper.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mwallpaper.AnotherUserProfileActivity;
import com.example.mwallpaper.Model.WallpaperItemModel;
import com.example.mwallpaper.MyUploadsActivity;
import com.example.mwallpaper.R;
import com.example.mwallpaper.SingleWallpaperActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
    String category;
    String wallpaperURL;

    private Activity parentActivity;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef, mRef2, mRef3, mRef1;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    public WallpaperItemAdapter(Context mContext, ArrayList<WallpaperItemModel> mList, String action, Activity parentActivity) {
        this.mContext = mContext;
        this.mList = mList;
        this.maction = action;
        this.parentActivity = parentActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view;

//        this is to check that whether the
        if (parentActivity instanceof AnotherUserProfileActivity){
            view = layoutInflater.inflate(R.layout.wallpaper_item_small_size, parent, false);
        }
        else {
            view = layoutInflater.inflate(R.layout.wallpaper_item, parent, false);
        }

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        ImageView wallpaperItemImage = holder.wallpaperItemImage;
        CardView wallpaperCardView = holder.wallpaperCardview;
        final ImageView favouriteFilled = holder.favouriteFilled;
        final ImageView favouriteUnfilled = holder.favouriteUnfilled;
        ImageView deleteImage = holder.deleteImage;
        ProgressBar imageProgressBar = holder.imageProgressBar;

        wallpaperURL = mList.get(position).getWallpaperItemURL();

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mRef = firebaseDatabase.getReference("wallpaper").child("users");
        mRef2 = firebaseDatabase.getReference("wallpaper").child("images");
        mRef3 = firebaseDatabase.getReference("wallpaper").child("recentlyUploadedImages");
        mRef1 = firebaseDatabase.getReference("wallpaper");
        user = firebaseAuth.getCurrentUser();

        imageProgressBar.setVisibility(View.GONE);
        Picasso.get().load(mList.get(position).getWallpaperItemURL()).into(wallpaperItemImage);

//        Log.d(TAG, "onBindViewHolder: "+mList.get(position).getWallpaperItemURL());

        if (maction.equals("favourite")) {

            if (user == null) {
                favouriteUnfilled.setVisibility(View.VISIBLE);
                favouriteFilled.setVisibility(View.GONE);
                deleteImage.setVisibility(View.GONE);
                favouriteUnfilled.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(parentActivity)
                                .setTitle("Login First!")
                                .setMessage("You need to login first to save the wallpapers to your favourites.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).show();
                    }
                });
            }
            else {

                userId = user.getUid();

                mRef.child(userId).child("favouriteImages").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        if (dataSnapshot1.child("thumbnail").getValue().toString().equals(mList.get(position).getWallpaperItemURL())) {
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

                    final String pushId = mRef.push().getKey();

                    Task task = mRef.child(userId).child("favouriteImages").child(pushId).child("thumbnail").setValue(mList.get(position).getWallpaperItemURL());
                    task.addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            mRef.child(userId).child("favouriteImages").child(pushId).child("userId").setValue(mList.get(position).getAnotherUserId());
                            favouriteFilled.setVisibility(View.VISIBLE);
                            favouriteUnfilled.setVisibility(View.GONE);
                        }
                    });

                }
            });

            favouriteFilled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    mRef.child(userId).child("favouriteImages").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                if (dataSnapshot1.child("thumbnail").getValue().toString().equals(mList.get(position).getWallpaperItemURL())) {
                                    mRef.child(userId).child("favouriteImages").child(dataSnapshot1.getKey()).child("thumbnail").removeValue();
                                    mRef.child(userId).child("favouriteImages").child(dataSnapshot1.getKey()).child("userId").removeValue();
                                    favouriteFilled.setVisibility(View.GONE);
                                    favouriteUnfilled.setVisibility(View.VISIBLE);
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
        } else {

            userId = user.getUid();

//              here code for delete will occur
            favouriteFilled.setVisibility(View.GONE);
            favouriteUnfilled.setVisibility(View.GONE);
            deleteImage.setVisibility(View.VISIBLE);

            deleteImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {

                    new AlertDialog.Builder(parentActivity)
                            .setTitle("Delete wallpaper")
                            .setMessage("Do you really want to delete this wallpaper")
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    Log.d(TAG, "onClick: It comes in yes part of alert dialog box");
                                    //                    delete from upload of user
                                    mRef.child(userId).child("uploadedImages").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                try {
                                                if (dataSnapshot1.child("uploadedImage").getValue().toString().equals(mList.get(position).getWallpaperItemURL())) {

                                                    mRef.child(userId).child("uploadedImages").child(dataSnapshot1.getKey()).child("uploadedImage").removeValue();
                                                    category = dataSnapshot1.child("category").getValue().toString();
                                                    mRef.child(userId).child("uploadedImages").child(dataSnapshot1.getKey()).child("category").removeValue();
                                                    mRef.child(userId).child("uploadedImages").child(dataSnapshot1.getKey()).child("userId").removeValue();

                                                    //                    delete from images category section section
                                                    mRef2.child(category).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                                if (dataSnapshot1.child("thumbnail").getValue().toString().equals(mList.get(position).getWallpaperItemURL())) {
                                                                    mRef2.child(category).child(dataSnapshot1.getKey()).child("thumbnail").removeValue();
                                                                    mRef2.child(category).child(dataSnapshot1.getKey()).child("userId").removeValue();
                                                                    break;
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });

                                                    break;

                                                }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

//                    if user is selected this wallpaper as favourite then we have to also delete form there
                                    mRef.child(userId).child("favouriteImages").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                try {
                                                    if (dataSnapshot1.child("thumbnail").getValue().toString().equals(mList.get(position).getWallpaperItemURL())) {

                                                        mRef.child(userId).child("favouriteImages").child(dataSnapshot1.getKey()).child("thumbnail").removeValue();
                                                        mRef.child(userId).child("favouriteImages").child(dataSnapshot1.getKey()).child("userId").removeValue();
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

//                    delete from recently uploaded
                                    mRef3.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                try {

                                                    if (dataSnapshot1.child("thumbnail").getValue().toString().equals(mList.get(position).getWallpaperItemURL())) {
                                                        mRef3.child(dataSnapshot1.getKey()).child("thumbnail").removeValue();
                                                        mRef3.child(dataSnapshot1.getKey()).child("userId").removeValue();
                                                        break;
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                                    Intent intent = new Intent(mContext, MyUploadsActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION );
                                    mContext.startActivity(intent);

                                }
                            })
                            .show();

                }
            });

        }

        wallpaperCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SingleWallpaperActivity.class);
                intent.putExtra("image_path", mList.get(position).getWallpaperItemURL());
                intent.putExtra("anotherUserId", mList.get(position).getAnotherUserId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView wallpaperItemImage, favouriteFilled, favouriteUnfilled, deleteImage;
        CardView wallpaperCardview;
        ProgressBar imageProgressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            wallpaperItemImage = itemView.findViewById(R.id.wallpaperItemImage);
            wallpaperCardview = itemView.findViewById(R.id.wallpaper_item_cardview);
            favouriteFilled = itemView.findViewById(R.id.fav_filled);
            favouriteUnfilled = itemView.findViewById(R.id.fav_unfilled);
            deleteImage = itemView.findViewById(R.id.deleteImage);
            imageProgressBar = itemView.findViewById(R.id.imageProgressBar);
        }
    }

}
