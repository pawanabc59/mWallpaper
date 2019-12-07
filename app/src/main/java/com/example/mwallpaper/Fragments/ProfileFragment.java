package com.example.mwallpaper.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mwallpaper.MainActivity;
import com.example.mwallpaper.MyUploadsActivity;
import com.example.mwallpaper.R;
import com.example.mwallpaper.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    ImageView profileImage;
    SwitchCompat nightSwitch;
    TextView showEmailText,txtMyUploads;
    Button btnLogout, btnEditPhoto;
    String userId;

    SessionManager sessionManager;
    ContextThemeWrapper contextThemeWrapper;
    Bitmap bitmap;

    FirebaseUser user;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef,mRef2;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    String TAG = "my";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        
        sessionManager = new SessionManager(getContext());
        if (sessionManager.loadNightModeState()==true){
            contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.darktheme);
        }
        else{
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
        txtMyUploads = view.findViewById(R.id.txtmyUploads);

        mRef.child(userId).addValueEventListener(new ValueEventListener() {
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
        });

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

        if (sessionManager.loadNightModeState()==true){
            nightSwitch.setChecked(true);
        }
        else {
            nightSwitch.setChecked(false);
        }

        nightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    sessionManager.setNightModeState(true);
                    nightSwitch.setChecked(true);

                }
                else {
                    sessionManager.setNightModeState(false);
                    nightSwitch.setChecked(false);

                }

                ReCreateApp();

            }
        });

        txtMyUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), MyUploadsActivity.class);
                getActivity().startActivity(intent);
            }
        });

        return view;
    }

    public void ReCreateApp(){
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null){
            Uri filepath = data.getData();
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filepath );
                profileImage.setImageBitmap(bitmap);
                final StorageReference storageReference1 = storageReference.child("profileImages").child(userId).child(filepath.getLastPathSegment());

                storageReference1.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
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

            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}
