package com.example.smartalert;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LocationListener{

    private FirebaseAuth auth;

    LocationManager locationManager;

    TextView welcome;
    Button manageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcome = findViewById(R.id.welcomeView);
        manageButton = findViewById(R.id.button2);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        SharedPreferences sp = getSharedPreferences("CachedUsername" , Context.MODE_PRIVATE);
        String cachedUsername = sp.getString("CachedUsername","");
        String cachedRole = sp.getString("CachedRole","");
        if(auth.getUid() == null)
        {
            openRegisterActivity();
        } else {
            if(!cachedUsername.isEmpty() && !cachedRole.isEmpty()) {
                welcome.setText(getString(R.string.welcome,cachedUsername));
                if (Objects.equals(cachedRole, "user")){manageButton.setVisibility(View.GONE);}
                ask_for_permissions();
            }
            else {
                db.collection("users").document(auth.getUid()).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();

                                if (document.exists()) {
                                    User user = new User();
                                    user.setUsername(document.getString("Username"));
                                    user.setRole(document.getString("Role"));
                                    welcome.setText(getString(R.string.welcome,user.getUsername()));
                                    Controller.EditUsername(user.getUsername(),sp);
                                    Controller.EditRole(user.getRole(),sp);
                                    if (Objects.equals(user.getRole(), "user")){manageButton.setVisibility(View.GONE);}
                                    ask_for_permissions();
                                }
                            }
                        });
            }
        }


    }

    private void ask_for_permissions() {
            requestLocation();
            requestBackground();
            requestNotifications();
    }



    private void requestNotifications() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 125);
            }

        }



    }



    private void requestBackground() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(
                        this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 124);
            }
        }


    }




    private void  requestLocation()  {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);

        }
        }



    private void openRegisterActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }


    public void logout(View view) {
        try{
            auth.signOut();
            openRegisterActivity();
        }catch(Exception E){
            Toast.makeText(this,E.getMessage(),Toast.LENGTH_LONG).show();
        }

    }

    public void openrequest(View view) { openRequestActivity();}
    private void openRequestActivity() {
        Intent intent = new Intent(this, RequestActivity.class);
        startActivity(intent);
    }

    public void openmanagerequests(View view) { openManageRequestsActivity();}
    private void openManageRequestsActivity() {
        Intent intent = new Intent(this, ManageRequestsActivity.class);
        startActivity(intent);
    }

    public void openstatistics(View view) { openStatisticsActivity(); }
    private void openStatisticsActivity() {
        Intent intent = new Intent(this,StatisticsActivity.class);
        startActivity(intent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            this.recreate();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 5, this);
        if (locationManager != null) {
            boolean isGPSEnabled = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                isGPSEnabled = locationManager.isLocationEnabled();
            }
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 5, this);
                if (!Controller.isServiceRunning(this, NotificationService.class)) {
                    Intent serviceIntent = new Intent(MainActivity.this, NotificationService.class);
                    startService(serviceIntent);
                    Log.e("run", "Service started");
                }

            }
        }


    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }
}