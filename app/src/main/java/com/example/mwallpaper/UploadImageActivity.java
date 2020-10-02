package com.example.mwallpaper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UploadImageActivity extends AppCompatActivity {

    ImageView uploadImagePreview;
    MaterialButton btnUploadImage, btnChooseImage;
    Spinner categorySpinner;
    ProgressBar uploadProgressBar;

    String categorySelected = "others";
    String userId;
    String TAG = "my";

    Bitmap bitmap;
    Uri filepath, filePath2, uri;
    byte[] image_byte_data;
    SessionManager sessionManager;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef;
    FirebaseAuth firebaseAuth;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    FirebaseUser user;
    int categoryNumberOfImages, uplodedNumberOfImages, recentNumberOfImages, imageUploaded = 0;
    ValueEventListener categoryValueEventListener, uploadValueEventListener, recentValueEventListener;

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
                switch (i) {
                    case 0:
                        categorySelected = "others";
                        break;
                    case 1:
                        categorySelected = "animals";
                        break;
                    case 2:
                        categorySelected = "anime";
                        break;
                    case 3:
                        categorySelected = "art";
                        break;
                    case 4:
                        categorySelected = "cartoon";
                        break;
                    case 5:
                        categorySelected = "cinematic";
                        break;
                    case 6:
                        categorySelected = "fantasy";
                        break;
                    case 7:
                        categorySelected = "flowers";
                        break;
                    case 8:
                        categorySelected = "food";
                        break;
                    case 9:
                        categorySelected = "games";
                        break;
                    case 10:
                        categorySelected = "love";
                        break;
                    case 11:
                        categorySelected = "nature";
                        break;
                    case 12:
                        categorySelected = "religious";
                        break;
                    case 13:
                        categorySelected = "scenery";
                        break;
                    case 14:
                        categorySelected = "sea";
                        break;
                    case 15:
                        categorySelected = "sky";
                        break;
                    case 16:
                        categorySelected = "space";
                        break;
                    case 17:
                        categorySelected = "sports";
                        break;
                    case 18:
                        categorySelected = "superhero";
                        break;
                    case 19:
                        categorySelected = "vehicle";
                        break;
                    case 20:
                        categorySelected = "words";
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        categoryValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryNumberOfImages = dataSnapshot.child("numberOfImages").getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        recentValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recentNumberOfImages = dataSnapshot.child("numberOfImages").getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        uploadValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                uplodedNumberOfImages = dataSnapshot.child("numberOfImages").getValue(Integer.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mRef.child("recentlyUploadedImages").addValueEventListener(recentValueEventListener);

        mRef.child("users").child(userId).child("uploadedImages").addValueEventListener(uploadValueEventListener);

        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.startPickImageActivity(UploadImageActivity.this);
//                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityForResult(intent, 2);
            }
        });

        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnUploadImage.setVisibility(View.GONE);
                uploadProgressBar.setVisibility(View.VISIBLE);
                if (imageUploaded == 1) {

                    mRef.child("images").child(categorySelected).addValueEventListener(categoryValueEventListener);

                    final StorageReference storageReference1 = storageReference.child("categories").child(categorySelected).child(filepath.getLastPathSegment());

                    UploadTask uploadTask = storageReference.child("categories").child(categorySelected).child(filepath.getLastPathSegment()).putBytes(image_byte_data);

                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getApplicationContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            storageReference1.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    String pushId = mRef.push().getKey();

//                                upload to firebase database in user
                                    mRef.child("users").child(userId).child("uploadedImages").child("images").child(pushId).child("uploadedImage").setValue(task.getResult().toString());
                                    mRef.child("users").child(userId).child("uploadedImages").child("images").child(pushId).child("category").setValue(categorySelected);
                                    mRef.child("users").child(userId).child("uploadedImages").child("images").child(pushId).child("userId").setValue(userId);
                                    mRef.child("users").child(userId).child("uploadedImages").child("images").child(pushId).child("postNumber").setValue((-(uplodedNumberOfImages + 1)));
                                    mRef.child("users").child(userId).child("uploadedImages").child("numberOfImages").setValue((uplodedNumberOfImages + 1));

//                                upload to database
                                    mRef.child("images").child(categorySelected).child("images").child(pushId).child("thumbnail").setValue(task.getResult().toString());
                                    mRef.child("images").child(categorySelected).child("images").child(pushId).child("userId").setValue(userId);
                                    mRef.child("images").child(categorySelected).child("images").child(pushId).child("postNumber").setValue((-(categoryNumberOfImages + 1)));
                                    mRef.child("images").child(categorySelected).child("numberOfImages").setValue((categoryNumberOfImages + 1));

//                                upload to firebase database recently uploaded
                                    mRef.child("recentlyUploadedImages").child("images").child(pushId).child("thumbnail").setValue(task.getResult().toString());
                                    mRef.child("recentlyUploadedImages").child("images").child(pushId).child("userId").setValue(userId);
                                    mRef.child("recentlyUploadedImages").child("images").child(pushId).child("postNumber").setValue((-(recentNumberOfImages + 1)));
                                    mRef.child("recentlyUploadedImages").child("numberOfImages").setValue((recentNumberOfImages + 1));

                                    uploadProgressBar.setVisibility(View.GONE);
                                    btnUploadImage.setVisibility(View.VISIBLE);
                                    imageUploaded = 0;

                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Please select an image to upload", Toast.LENGTH_SHORT).show();
                    uploadProgressBar.setVisibility(View.GONE);
                    btnUploadImage.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageuri = CropImage.getPickImageResultUri(this, data);
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageuri)) {
                uri = imageuri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            } else {
                startCrop(imageuri);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filepath = result.getUri();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                    image_byte_data = baos.toByteArray();
                    imageUploaded = 1;
                    uploadImagePreview.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

//        IF YOU DON'T WANT TO CROP THE IMAGE
//        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
//
//            filepath = data.getData();
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
//                image_byte_data = baos.toByteArray();
//                imageUploaded = 1;
//                uploadImagePreview.setImageBitmap(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

    private void startCrop(Uri imageuri) {
        CropImage.activity(imageuri).setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRef.child("images").child(categorySelected).removeEventListener(categoryValueEventListener);

        mRef.child("recentlyUploadedImages").removeEventListener(recentValueEventListener);

        mRef.child("users").child(userId).child("uploadedImages").removeEventListener(uploadValueEventListener);

    }
}

