package com.example.smartalert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RequestActivity extends AppCompatActivity implements LocationListener {

    private FirebaseAuth auth;

    ImageView imageView;
    Button button;
    Spinner spinner;
    FirebaseFirestore db;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        auth = FirebaseAuth.getInstance();
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.type_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        imageView = findViewById(R.id.imageView);
        button = findViewById(R.id.picButton);
        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();


        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.CAMERA}, 125);
        }

        button.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 125);
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != 0) {
            if (requestCode == 125) {
                assert data != null;
                Bitmap bitmap = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                imageView.setImageBitmap(bitmap);
                button.setVisibility(View.GONE);
            }
        }

    }

    public void sendrequest(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 5, this);

            if (locationManager != null) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


                if (imageView.getDrawable() != null) {
                    Request request = new Request();
                    switch (spinner.getSelectedItemPosition())
                    {
                        case 0:
                            request.setType("Flood");
                            break;
                        case 1:
                            request.setType("Fire");
                            break;
                        case 2:
                            request.setType("Earthquake");
                            break;
                        case 3:
                            request.setType("Other");
                            break;
                    }

                    assert location != null;
                    request.setLatitude(location.getLatitude());
                    request.setLongitude(location.getLongitude());
                    request.setTimestamp(location.getTime() / 1000);
                    request.setUser(auth.getUid());

                    uploadRequest(request);


                }
            } else {
                Toast.makeText(this, R.string.unable_to_retrieve_location, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.unable_to_retrieve_location, Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadRequest(Request request) {
        Map<String, Object> upload = new HashMap<>();
        upload.put("Type", request.getType());
        upload.put("Latitude", request.getLatitude());
        upload.put("Longitude", request.getLongitude());
        upload.put("Timestamp", request.getTimestamp());
        upload.put("User", request.getUser());


        db.collection("requests")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult())
                        {
                            if(Objects.equals(document.getString("Type"), request.getType())
                                    && timeDifference(document.getLong("Timestamp"),request.getTimestamp())
                                    && Objects.equals(Controller.getLocation(document.getDouble("Latitude"), document.getDouble("Longitude"), this),
                                                      Controller.getLocation(request.getLatitude(), request.getLongitude(), this))){
                                double points = document.getDouble("Points");
                                points += 900 - Math.abs(document.getLong("Timestamp")-request.getTimestamp());
                                points += 10/Controller.distanceDifference(request.getLatitude(),request.getLongitude(),document.getDouble("Latitude"),document.getDouble("Longitude"));
                                if(!Objects.equals(request.getUser(), document.getString("User"))){
                                    points +=10;
                                }

                                Map<String, Object> update = new HashMap<>();
                                update.put("Points",points);
                                update.put("Timestamp",request.getTimestamp());


                                db.collection("requests").document(document.getId())
                                        .update(update).addOnSuccessListener(unused -> db.collection("requests").document(document.getId()).collection("requests")
                                                .add(upload)
                                                .addOnSuccessListener(documentReference -> uploadPic(documentReference.getId(),db.collection("requests").document(document.getId()).collection("requests"))));
                                return;
                            }
                        }
                        upload.put("Points", 0);

                        db.collection("requests")
                                .add(upload)
                                .addOnSuccessListener(documentReference -> uploadPic(documentReference.getId(),db.collection("requests")));

                    }
                }
        );
    }



    private boolean timeDifference(Long t1, Long t2) {
        long diff = Math.abs(t2-t1);
        return Math.abs(diff) <= 900;

    }


    private void uploadPic(String id , CollectionReference collectionReference) {

        StorageReference requestsRef = storageReference.child("requests/" + id);

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = requestsRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> requestsRef.getDownloadUrl().addOnSuccessListener(uri -> {
            collectionReference.document(id).update("Path", uri.toString());
            Toast.makeText(RequestActivity.this, R.string.request_uploaded_succesfully, Toast.LENGTH_LONG).show();
            openMainActivity();

        })).addOnFailureListener(e -> Toast.makeText(RequestActivity.this, R.string.failed_to_upload_picture, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }


    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}