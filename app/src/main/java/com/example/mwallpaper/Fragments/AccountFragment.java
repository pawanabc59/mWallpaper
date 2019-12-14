package com.example.mwallpaper.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mwallpaper.MainActivity;
import com.example.mwallpaper.R;
import com.example.mwallpaper.RegisterActivity;
import com.example.mwallpaper.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountFragment extends Fragment {

    TextInputLayout textEmail, textPassword;
    TextInputEditText editEmail, editPassword;
    MaterialButton btnLogin;
    TextView textRegister;
    ProgressBar loginProgressBar;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference mRef, mRef2;
    FirebaseUser firebaseUser;

    SessionManager sessionManager;
    ContextThemeWrapper contextThemeWrapper;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

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
        View view = localInflater.inflate(R.layout.fragment_account, container, false);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mRef = firebaseDatabase.getReference("wallpaper").child("users");

        textEmail = view.findViewById(R.id.textEmail);
        textPassword = view.findViewById(R.id.textPassword);

        editEmail = view.findViewById(R.id.editEmail);
        editPassword = view.findViewById(R.id.editPassword);

        btnLogin = view.findViewById(R.id.btnLogin);
        loginProgressBar = view.findViewById(R.id.loginProgressBar);

        textRegister = view.findViewById(R.id.textRegister);

        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnLogin.setVisibility(View.GONE);
                loginProgressBar.setVisibility(View.VISIBLE);

                String memail = editEmail.getText().toString().trim();
                String mpassword = editPassword.getText().toString().trim();
                if (memail.isEmpty()) {
                    editEmail.setError("Please insert email");
                } else if (mpassword.isEmpty()) {
                    editPassword.setError("Please insert password");
                } else if (!memail.matches(emailPattern)) {
                    editEmail.setError("Please provide valid email address");
                } else if (mpassword.length() < 6) {
                    editPassword.setError("Password is too short");
                } else {
                    Login(memail, mpassword);
                }
            }
        });

        return view;
    }

    public void Login(final String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Welcome User", Toast.LENGTH_SHORT).show();
                            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = firebaseUser.getUid();
                            mRef2 = mRef.child(uid);
                            mRef2.child("email").setValue(email);

//                            this is to add the profile of the user if user has uploaded the profile image earlier then that url will be loaded otherwise the null value will be set.
                            mRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    try {
                                        String profileImagePath = dataSnapshot.child("profileImage").getValue().toString();
                                        mRef2.child("profileImage").setValue(profileImagePath);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        mRef2.child("profileImage").setValue("null");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

//                          this was earlier code :  mRef2.child("profileImage").setValue("null");

                            loginProgressBar.setVisibility(View.GONE);
                            btnLogin.setVisibility(View.VISIBLE);
                            Intent intent = new Intent(getContext(), MainActivity.class);
                            getActivity().startActivity(intent);

                        } else {
                            loginProgressBar.setVisibility(View.GONE);
                            btnLogin.setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), "Sorry! Login failed", Toast.LENGTH_SHORT);
                        }
                    }
                });
    }

}
