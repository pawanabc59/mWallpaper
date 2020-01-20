package com.example.mwallpaper;

import android.Manifest;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.github.chrisbanes.photoview.PhotoView;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SingleWallpaperActivity extends AppCompatActivity {

//    ImageView wallpaperImage;
    PhotoView wallpaperImage;
    SessionManager sessionManager;

    FloatingActionMenu floatingActionMenu;
    FloatingActionButton fbtnSetBackground, fbtnAddFavourite, fbtnRemoveFavourite, fbtnDownload, fbtnShare;
    //    com.google.android.material.floatingactionbutton.FloatingActionButton fbtnInformation;
    ImageButton btnInfo;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    String userId;
    String pushKey;

    String TAG = "my";
    String wallpaper_path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(getApplicationContext());
        if (sessionManager.loadNightModeState() == true) {

            setTheme(R.style.darktheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        setContentView(R.layout.activity_single_wallpaper);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mRef = firebaseDatabase.getReference("wallpaper");

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        final Intent intent = getIntent();
        wallpaper_path = intent.getExtras().getString("image_path");
        final String anotherUserId = intent.getExtras().getString("anotherUserId");

        wallpaperImage = findViewById(R.id.single_wallpaper_show);

        floatingActionMenu = findViewById(R.id.floatingMenu);
        fbtnSetBackground = findViewById(R.id.fbtnSetBackground);
        fbtnAddFavourite = findViewById(R.id.fbtnAddFavourite);
        fbtnRemoveFavourite = findViewById(R.id.fbtnRemoveFavourite);
        fbtnDownload = findViewById(R.id.fbtnDownload);
        fbtnShare = findViewById(R.id.fbtnShare);

        btnInfo = findViewById(R.id.btnInfo);

        floatingActionMenu.setClosedOnTouchOutside(true);

//        This code is for transparent status bar in android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        Picasso.get().load(wallpaper_path).into(wallpaperImage);

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(getApplicationContext(), AnotherUserProfileActivity.class);
                intent1.putExtra("parentImagePath", wallpaper_path);
                intent1.putExtra("anotherUserId", anotherUserId);
                startActivity(intent1);
            }
        });

        fbtnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(getApplicationContext(),"download is clicked", Toast.LENGTH_SHORT).show();
                Picasso.get().load(wallpaper_path).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Intent intent1 = new Intent(Intent.ACTION_VIEW);
                        Uri uri = saveWallpaperAndGetUri(bitmap);

                        if (uri != null) {
                            intent1.setDataAndType(uri, "image/*");
                            startActivity(Intent.createChooser(intent1, "mWallpaper"));
                        }
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            }
        });

        fbtnSetBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String[] listItems = new String[]{"Set as Home Screen", "Set as Lock Screen", "Both"};
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(SingleWallpaperActivity.this);
                mBuilder.setTitle("Choose an option");
                mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setWallpaperOnDevice(i);
                        dialogInterface.dismiss();
                    }
                });
                mBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                AlertDialog mDialog = mBuilder.create();
                mDialog.show();

            }
        });

        fbtnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Picasso.get().load(wallpaper_path).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Intent intent1 = new Intent(Intent.ACTION_SEND);
                        intent1.setType("images/*");
//                        intent.addFlags( Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION );
                        intent1.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap));
                        startActivity(Intent.createChooser(intent1, "mWallpaper"));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            }
        });

//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_IMMERSIVE
//                        // Set the content to appear under the system bars so that the
//                        // content doesn't resize when the system bars hide and show.
//                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        // Hide the nav bar and status bar
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        if (user == null) {
            fbtnRemoveFavourite.setVisibility(View.GONE);
            fbtnAddFavourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(SingleWallpaperActivity.this)
                            .setTitle("Login First!")
                            .setMessage("You need to login first to add the wallpapers to your favourites.")
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

            mRef.child("users").child(userId).child("favouriteImages").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mRef.child("users").child(userId).child("favouriteImages").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                    if (dataSnapshot1.child("thumbnail").getValue().toString().equals(wallpaper_path)) {
                                        Log.d(TAG, "onDataChange: favourite is same");
                                        fbtnRemoveFavourite.setVisibility(View.VISIBLE);
                                        fbtnAddFavourite.setVisibility(View.GONE);
                                        break;
                                    } else {
                                        Log.d(TAG, "onDataChange: favourite is not same");
                                        fbtnAddFavourite.setVisibility(View.VISIBLE);
                                        fbtnRemoveFavourite.setVisibility(View.GONE);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        fbtnAddFavourite.setVisibility(View.VISIBLE);
                        fbtnRemoveFavourite.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            fbtnAddFavourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String key = mRef.child("users").push().getKey();
                    mRef.child("users").child(userId).child("favouriteImages").child(key).child("thumbnail").setValue(wallpaper_path);
                    mRef.child("users").child(userId).child("favouriteImages").child(key).child("userId").setValue(anotherUserId);
                    Toast.makeText(getApplicationContext(), "Wallpaper is added to favourite", Toast.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent1);
                    finish();
                }
            });

            fbtnRemoveFavourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mRef.child("users").child(userId).child("favouriteImages").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                if (dataSnapshot1.child("thumbnail").getValue().toString().equals(wallpaper_path)) {
                                    mRef.child("users").child(userId).child("favouriteImages").child(dataSnapshot1.getKey()).child("thumbnail").removeValue();
                                    mRef.child("users").child(userId).child("favouriteImages").child(dataSnapshot1.getKey()).child("userId").removeValue();
                                    Toast.makeText(getApplicationContext(), "Wallpaper is removed from favourite", Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent1);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });

        }
    }

    private void setWallpaperOnDevice(final int position) {
        Picasso.get().load(wallpaper_path).into(new Target() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());

//                        this is show the image as full display wallpaper.
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int height = metrics.heightPixels;
                int width = metrics.widthPixels;
                Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, width, height, true);
                try {
//                            wallpaperManager.setBitmap(bitmap);
//                            wallpaperManager.suggestDesiredDimensions(wallpaperManager.getDesiredMinimumWidth(), wallpaperManager.getDesiredMinimumHeight());
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                wallpaperManager.setBitmap(bitmap,,false, WallpaperManager.FLAG_SYSTEM);
//                            }
//                            wallpaperManager.setBitmap(bitmap1);
                    if (position == 0) {
                        wallpaperManager.setBitmap(bitmap1, null, false, WallpaperManager.FLAG_SYSTEM);
                        Toast.makeText(getApplicationContext(), "Wallpaper set as Home Screen Wallpaper", Toast.LENGTH_SHORT).show();
                    } else if (position == 1) {
                        wallpaperManager.setBitmap(bitmap1, null, false, WallpaperManager.FLAG_LOCK);
                        Toast.makeText(getApplicationContext(), "Wallpaper set as Lock Screen Wallpaper", Toast.LENGTH_SHORT).show();
                    } else if (position == 2) {
                        wallpaperManager.setBitmap(bitmap1);
                        Toast.makeText(getApplicationContext(), "Wallpaper set as Home and Lock Screen Wallpaper", Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error occurred", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }

    private Uri getLocalBitmapUri(Bitmap bitmap) {
        Uri uri = null;
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "mWallpaper_" + System.currentTimeMillis() + ".png");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
            uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uri;
    }

    private Uri saveWallpaperAndGetUri(Bitmap bitmap) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);

                startActivity(intent);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
            return null;
        }
        File folder = new File(Environment.getExternalStorageDirectory().toString() + "/mWallpaper");
        folder.mkdir();

        pushKey = mRef.push().getKey();

        File file = new File(folder, pushKey + ".jpg");
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(getApplicationContext(), "Image Downloaded", Toast.LENGTH_SHORT).show();

            return FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }
}
