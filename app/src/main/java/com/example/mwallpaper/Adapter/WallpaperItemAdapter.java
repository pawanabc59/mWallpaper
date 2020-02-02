package com.example.mwallpaper.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
    String category="";
    String wallpaperURL;

    private Activity parentActivity;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef, mRef2, mRef3, mRef1;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    ValueEventListener favouriteValueEventListener, categoryValueEventListener, deleteRecentlyUploadedImageValueEventListener,deleteFavouriteImageValueEventListener,deleteCategoryImageValueEventListener, deleteUploadedImageValueEventListener,recentValueEventListener, uploadedValueEventListener, showFavouriteValueEventListener, addFavouriteValueEventListener;
    int favouriteNumberOfImages, categoryNumberOfImages, recentNumberOfImages, uploadedNumberOfImages;

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
        if (parentActivity instanceof AnotherUserProfileActivity) {
            view = layoutInflater.inflate(R.layout.wallpaper_item_small_size, parent, false);
        } else {
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

        if (user != null) {

            userId = user.getUid();

            favouriteValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    favouriteNumberOfImages = dataSnapshot.child("numberOfImages").getValue(Integer.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            uploadedValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    uploadedNumberOfImages = dataSnapshot.child("numberOfImages").getValue(Integer.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            recentValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    recentNumberOfImages = dataSnapshot.getValue(Integer.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            mRef.child(userId).child("favouriteImages").addValueEventListener(favouriteValueEventListener);

            mRef.child(userId).child("uploadedImages").addValueEventListener(uploadedValueEventListener);

            mRef3.child("numberOfImages").addValueEventListener(recentValueEventListener);
        }

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
            } else {

                userId = user.getUid();

                showFavouriteValueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        try {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                if (dataSnapshot1.child("thumbnail").getValue().toString().equals(mList.get(position).getWallpaperItemURL())) {
                                    favouriteFilled.setVisibility(View.VISIBLE);
                                    favouriteUnfilled.setVisibility(View.GONE);
                                }
//                                else {
//                                    favouriteFilled.setVisibility(View.GONE);
//                                    favouriteUnfilled.setVisibility(View.VISIBLE);
//                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };

                mRef.child(userId).child("favouriteImages").child("images").addListenerForSingleValueEvent(showFavouriteValueEventListener);

                favouriteUnfilled.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final String pushId = mRef.push().getKey();

                        Task task = mRef.child(userId).child("favouriteImages").child("images").child(pushId).child("thumbnail").setValue(mList.get(position).getWallpaperItemURL());
                        task.addOnSuccessListener(new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {
                                mRef.child(userId).child("favouriteImages").child("images").child(pushId).child("userId").setValue(mList.get(position).getAnotherUserId());
                                mRef.child(userId).child("favouriteImages").child("images").child(pushId).child("postNumber").setValue((-(favouriteNumberOfImages + 1)));
                                mRef.child(userId).child("favouriteImages").child("numberOfImages").setValue((favouriteNumberOfImages + 1));
                                favouriteFilled.setVisibility(View.VISIBLE);
                                favouriteUnfilled.setVisibility(View.GONE);
                            }
                        });

                    }
                });

                favouriteFilled.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        addFavouriteValueEventListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                try {
                                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                        if (dataSnapshot1.child("thumbnail").getValue().toString().equals(mList.get(position).getWallpaperItemURL())) {
                                            mRef.child(userId).child("favouriteImages").child("images").child(dataSnapshot1.getKey()).child("thumbnail").removeValue();
                                            mRef.child(userId).child("favouriteImages").child("images").child(dataSnapshot1.getKey()).child("userId").removeValue();
                                            mRef.child(userId).child("favouriteImages").child("images").child(dataSnapshot1.getKey()).child("postNumber").removeValue();
                                            mRef.child(userId).child("favouriteImages").child("numberOfImages").setValue((favouriteNumberOfImages - 1));

                                            favouriteFilled.setVisibility(View.GONE);
                                            favouriteUnfilled.setVisibility(View.VISIBLE);
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        };

                        mRef.child(userId).child("favouriteImages").child("images").addListenerForSingleValueEvent(addFavouriteValueEventListener);

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

//                                    Log.d(TAG, "onClick: It comes in yes part of alert dialog box");
                                    //                    delete from upload of user

                                    deleteUploadedImageValueEventListener = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                try {
                                                    if (dataSnapshot1.child("uploadedImage").getValue().toString().equals(mList.get(position).getWallpaperItemURL())) {

                                                        mRef.child(userId).child("uploadedImages").child("images").child(dataSnapshot1.getKey()).child("uploadedImage").removeValue();
                                                        category = dataSnapshot1.child("category").getValue().toString();
                                                        mRef.child(userId).child("uploadedImages").child("images").child(dataSnapshot1.getKey()).child("category").removeValue();
                                                        mRef.child(userId).child("uploadedImages").child("images").child(dataSnapshot1.getKey()).child("userId").removeValue();
                                                        mRef.child(userId).child("uploadedImages").child("images").child(dataSnapshot1.getKey()).child("postNumber").removeValue();
                                                        mRef.child(userId).child("uploadedImages").child("numberOfImages").setValue(uploadedNumberOfImages-1);

                                                        categoryValueEventListener = new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot categorysnapshot) {
                                                                categoryNumberOfImages = categorysnapshot.getValue(Integer.class);
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        };

                                                        mRef2.child(category).child("numberOfImages").addValueEventListener(categoryValueEventListener);

                                                        //                    delete from images category section
                                                        deleteCategoryImageValueEventListener = new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                                    if (dataSnapshot1.child("thumbnail").getValue().toString().equals(mList.get(position).getWallpaperItemURL())) {
                                                                        mRef2.child(category).child("images").child(dataSnapshot1.getKey()).child("thumbnail").removeValue();
                                                                        mRef2.child(category).child("images").child(dataSnapshot1.getKey()).child("userId").removeValue();
                                                                        mRef2.child(category).child("images").child(dataSnapshot1.getKey()).child("postNumber").removeValue();
                                                                        mRef2.child(category).child("numberOfImages").setValue(categoryNumberOfImages-1);
                                                                        break;
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        };
                                                        mRef2.child(category).child("images").addListenerForSingleValueEvent(deleteCategoryImageValueEventListener);

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
                                    };

                                    mRef.child(userId).child("uploadedImages").child("images").addListenerForSingleValueEvent(deleteUploadedImageValueEventListener);

//                    if user has selected this wallpaper as favourite then we have to also delete form there
                                    deleteFavouriteImageValueEventListener = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                try {
                                                    if (dataSnapshot1.child("thumbnail").getValue().toString().equals(mList.get(position).getWallpaperItemURL())) {

                                                        mRef.child(userId).child("favouriteImages").child("images").child(dataSnapshot1.getKey()).child("thumbnail").removeValue();
                                                        mRef.child(userId).child("favouriteImages").child("images").child(dataSnapshot1.getKey()).child("userId").removeValue();
                                                        mRef.child(userId).child("favouriteImages").child("images").child(dataSnapshot1.getKey()).child("postNumber").removeValue();
                                                        mRef.child(userId).child("favouriteImages").child("numberOfImages").setValue(favouriteNumberOfImages-1);
                                                    }
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    };

                                    mRef.child(userId).child("favouriteImages").child("images").addListenerForSingleValueEvent(deleteFavouriteImageValueEventListener);

//                    delete from recently uploaded
                                    deleteRecentlyUploadedImageValueEventListener = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                                try {

                                                    if (dataSnapshot1.child("thumbnail").getValue().toString().equals(mList.get(position).getWallpaperItemURL())) {
                                                        mRef3.child("images").child(dataSnapshot1.getKey()).child("thumbnail").removeValue();
                                                        mRef3.child("images").child(dataSnapshot1.getKey()).child("userId").removeValue();
                                                        mRef3.child("images").child(dataSnapshot1.getKey()).child("postNumber").removeValue();
                                                        mRef3.child("numberOfImages").setValue(recentNumberOfImages-1);

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
                                    };

                                    mRef3.child("images").addListenerForSingleValueEvent(deleteRecentlyUploadedImageValueEventListener);

//                                    Intent intent = new Intent(mContext, MyUploadsActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                                    mContext.startActivity(intent);

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
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        try {

            mRef.child(userId).child("favouriteImages").removeEventListener(favouriteValueEventListener);

            mRef.child(userId).child("uploadedImages").removeEventListener(uploadedValueEventListener);

            mRef3.child("numberOfImages").removeEventListener(recentValueEventListener);

            mRef.child(userId).child("favouriteImages").child("images").removeEventListener(showFavouriteValueEventListener);

            mRef.child(userId).child("favouriteImages").child("images").removeEventListener(addFavouriteValueEventListener);

            mRef.child(userId).child("favouriteImages").child("images").removeEventListener(deleteFavouriteImageValueEventListener);

            mRef3.child("images").removeEventListener(deleteRecentlyUploadedImageValueEventListener);

            if (!category.equals("")) {

                mRef2.child(category).child("numberOfImages").removeEventListener(categoryValueEventListener);

                mRef2.child(category).child("images").removeEventListener(deleteCategoryImageValueEventListener);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

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
