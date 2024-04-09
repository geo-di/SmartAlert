package com.example.smartalert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    EditText email,password,name,admincodeinput;
    CheckBox check;
    FirebaseAuth auth;
    FirebaseUser fireUser;

    String admincode = "0000" , role ;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    User user = new User();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        name = findViewById(R.id.editTextName);
        admincodeinput = findViewById(R.id.editTextAdminId);
        check = findViewById(R.id.checkBox);
        check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                admincodeinput.setVisibility(View.VISIBLE);
            }
            else
            {
                admincodeinput.setVisibility(View.INVISIBLE);
            }

        });
        auth = FirebaseAuth.getInstance();
        fireUser = auth.getCurrentUser();
        if (fireUser!=null){
           openMainActivity();
        }
    }

    public void signup(View view){
        if(!email.getText().toString().isEmpty() &&
            !password.getText().toString().isEmpty() &&
            !name.getText().toString().isEmpty()){
                if (check.isChecked()){
                    if(admincodeinput.getText().toString().equals(admincode)){
                        role = "cpe";
                    } else{
                        Toast.makeText(this, "Wrong Admin Key!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    role = "user";
                }
                auth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            user.setUsername(name.getText().toString());
                            user.setEmail(email.getText().toString());
                            user.setPassword(password.getText().toString());
                            user.setRole(role);
                            user.setId(auth.getUid());

                            createUser(user);
                        }else {
                            Toast.makeText(this, Objects.requireNonNull(task.getException()).getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }else {
            Toast.makeText(this, "Please provide all info!", Toast.LENGTH_SHORT).show();
        }
    }

    private void createUser(User user) {
        Map<String, Object> upload = new HashMap<>();
        upload.put("Username",user.getUsername());
        upload.put("Email",user.getEmail());
        upload.put("Password",user.getPassword());
        upload.put("Role",user.getRole());

        db.collection("users").document(user.getId())
                .set(upload)
                .addOnSuccessListener(unused -> openMainActivity());

    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        SharedPreferences sp = getSharedPreferences("CachedUsername" , Context.MODE_PRIVATE);
        Controller.EditUsername(user.getUsername(), sp);
        Controller.EditRole(user.getRole(),sp);
        startActivity(intent);
    }

    public void openLogin(View view){
        openLoginActivity();
    }

    public void openLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}