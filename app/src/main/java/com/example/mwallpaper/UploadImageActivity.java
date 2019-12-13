package com.example.mwallpaper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

public class UploadImageActivity extends AppCompatActivity {

    ImageView uploadImagePreview;
    Button btnUploadImage,btnChooseImage;
    Spinner categorySpinner;
    ProgressBar uploadProgressBar;

    String categorySelected;
    String userId;
    String TAG = "my";

    Bitmap bitmap;
    Uri filepath, filePath2;
    SessionManager sessionManager;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef;
    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(getApplicationContext());
        if (sessionManager.loadNightModeState() == true) {

            setTheme(R.style.darktheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        setContentView(R.layout.activity_upload_image);

        uploadImagePreview = findViewById(R.id.uploadImagePreview);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnChooseImage = findViewById(R.id.btnChooseImage);
        categorySpinner = findViewById(R.id.categorySpinner);
        uploadProgressBar = findViewById(R.id.uploadProgressBar);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mRef = firebaseDatabase.getReference("wallpaper");

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        userId = user.getUid();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference("wallpaper");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.categoryArray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        categorySelected = "others";
                        break;
                    case 1:
                        categorySelected = "anime";
                        break;
                    case 2:
                        categorySelected = "nature";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 2);
            }
        });

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnUploadImage.setVisibility(View.GONE);
                uploadProgressBar.setVisibility(View.VISIBLE);

////                File imageFile = new File(filepath.getPath());
//                File compressedImageFile = null;
//                try {
//                    compressedImageFile = new Compressor(getApplicationContext()).compressToFile(new File(filepath.getLastPathSegment()));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                filePath2 = Uri.fromFile(compressedImageFile);

                final StorageReference storageReference1 = storageReference.child("categories").child(categorySelected).child(filepath.getLastPathSegment());

                storageReference1.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        storageReference1.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                String pushId = mRef.push().getKey();

//                                upload to firebase database in user
                                mRef.child("users").child(userId).child("uploadedImages").child(pushId).child("uploadedImage").setValue(task.getResult().toString());
                                mRef.child("users").child(userId).child("uploadedImages").child(pushId).child("category").setValue(categorySelected);
                                mRef.child("users").child(userId).child("uploadedImages").child(pushId).child("userId").setValue(userId);

//                                upload to database
                                mRef.child("images").child(categorySelected).child(pushId).child("thumbnail").setValue(task.getResult().toString());
                                mRef.child("images").child(categorySelected).child(pushId).child("userId").setValue(userId);

//                                upload to firebase database recently uploaded
                                mRef.child("recentlyUploadedImages").child(pushId).child("thumbnail").setValue(task.getResult().toString());
                                mRef.child("recentlyUploadedImages").child(pushId).child("userId").setValue(userId);

                                uploadProgressBar.setVisibility(View.GONE);
                                btnUploadImage.setVisibility(View.VISIBLE);

                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){

            filepath = data.getData();
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath );
                uploadImagePreview.setImageBitmap(bitmap);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}
