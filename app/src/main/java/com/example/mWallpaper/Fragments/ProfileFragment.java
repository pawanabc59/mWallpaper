package com.example.mWallpaper.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.mWallpaper.DisclaimerActivity;
import com.example.mWallpaper.MainActivity;
import com.example.mWallpaper.MyUploadsActivity;
import com.example.mWallpaper.R;
import com.example.mWallpaper.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class ProfileFragment extends Fragment {

    ImageView profileImage;
    SwitchCompat nightSwitch;
    TextView showEmailText, txtMyUploads;
    Button btnmyUploads, btnDisclaimer, btnVerifyEmail;
    String userId;
    MaterialButton btnLogout, btnEditPhoto;
    ProgressBar editPhotoProgressBar;

    SessionManager sessionManager;
    ContextThemeWrapper contextThemeWrapper;
    Bitmap bitmap;

    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef, mRef2;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    String TAG = "my";
    ValueEventListener profileValueEventListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        sessionManager = new SessionManager(getContext());
        if (sessionManager.loadNightModeState() == true) {
            contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.darktheme);
        } else {
            contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.AppTheme);
        }

        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

        // Inflate the layout for this fragment
        View view = localInflater.inflate(R.layout.fragment_profile, container, false);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mRef = firebaseDatabase.getReference("wallpaper").child("users");
        user = firebaseAuth.getCurrentUser();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("wallpaper");

        userId = user.getUid();

        profileImage = view.findViewById(R.id.profile_image);
        nightSwitch = view.findViewById(R.id.night_switch);
        showEmailText = view.findViewById(R.id.showEmail);
        btnEditPhoto = view.findViewById(R.id.btnEditPhoto);
        btnLogout = view.findViewById(R.id.btnlogout);
        btnmyUploads = view.findViewById(R.id.btnmyUploads);
        btnDisclaimer = view.findViewById(R.id.btnDisclaimer);
        editPhotoProgressBar = view.findViewById(R.id.editPhotoProgressBar);
        btnVerifyEmail = view.findViewById(R.id.btnVerifyEmail);

        btnEditPhoto.setVisibility(View.VISIBLE);
        editPhotoProgressBar.setVisibility(View.GONE);

        profileValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).into(profileImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                showEmailText.setText(dataSnapshot.child("email").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mRef.child(userId).addValueEventListener(profileValueEventListener);

        btnEditPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                getActivity().startActivityForResult(intent, 1);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                ReCreateApp();
            }
        });

        if (sessionManager.loadNightModeState() == true) {
            nightSwitch.setChecked(true);
        } else {
            nightSwitch.setChecked(false);
        }

        nightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sessionManager.setNightModeState(true);
                    nightSwitch.setChecked(true);

                } else {
                    sessionManager.setNightModeState(false);
                    nightSwitch.setChecked(false);

                }

                ReCreateApp();

            }
        });

        btnVerifyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Mail sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Some error occurred", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        btnmyUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MyUploadsActivity.class);
                getActivity().startActivity(intent);
            }
        });

        btnDisclaimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), DisclaimerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mRef.child(userId).removeEventListener(profileValueEventListener);
    }

    public void ReCreateApp() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getActivity().startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        btnEditPhoto.setVisibility(View.GONE);
        editPhotoProgressBar.setVisibility(View.VISIBLE);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Uri filepath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filepath);
                profileImage.setImageBitmap(bitmap);
                final StorageReference storageReference1 = storageReference.child("profileImages").child(userId).child(filepath.getLastPathSegment());

                storageReference1.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        editPhotoProgressBar.setVisibility(View.GONE);
                        btnEditPhoto.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
//                        Task<Uri> downloadUri = storageReference1.getDownloadUrl();
//                        Log.d(TAG, "onSuccess: "+downloadUri);
//                        mRef.child(userId).child("profileImage").setValue(downloadUri);
                        storageReference1.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
//                                Map<String, Object>updateValue = new HashMap<>();
//                                updateValue.put("profileImage",task.getResult().toString());
                                mRef.child(userId).child("profileImage").setValue(task.getResult().toString());

                            }
                        });
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            editPhotoProgressBar.setVisibility(View.GONE);
            btnEditPhoto.setVisibility(View.VISIBLE);
        }
    }

}
