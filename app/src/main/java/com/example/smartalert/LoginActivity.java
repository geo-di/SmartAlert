package com.example.smartalert;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    EditText email,password;
    FirebaseAuth auth;
    FirebaseUser FireUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        auth = FirebaseAuth.getInstance();
        FireUser = auth.getCurrentUser();
    }

    public void login(View view){
        if(!email.getText().toString().isEmpty() &&
                !password.getText().toString().isEmpty()){
            auth.signInWithEmailAndPassword(email.getText().toString(),
                    password.getText().toString()).addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            openMainActivity();
                        }
                    });
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        SharedPreferences sp = getSharedPreferences("CachedUsername" , Context.MODE_PRIVATE);
        Controller.EditUsername("", sp);
        Controller.EditRole("",sp);
        startActivity(intent);
    }

    public void openSignup(View view){
        openSignupActivity();
    }

    void openSignupActivity() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}