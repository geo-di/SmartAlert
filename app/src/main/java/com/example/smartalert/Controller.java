package com.example.smartalert;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Controller {



    public static String timestampToDate(Long timestamp){
            Date date = new Date(timestamp*1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            return sdf.format(date);
    }

    public static void EditUsername(String name , SharedPreferences sp ){

        SharedPreferences.Editor editor = sp.edit();
        editor.putString("CachedUsername",name);
        editor.apply();
    }

    public static void EditRole(String role , SharedPreferences sp ){

        SharedPreferences.Editor editor = sp.edit();
        editor.putString("CachedRole",role);
        editor.apply();
    }

    public static String getLocation(Double latitude, Double longitude, Context context){
        Geocoder geocoder = new Geocoder(context);
        try {
            List<Address> address =  geocoder.getFromLocation(latitude,longitude,1);
            if( address.get(0).getLocality() != null){
                return address.get(0).getLocality();
            } else {
                return address.get(0).getSubAdminArea();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public interface UsernameCallback {
        void onUsernameReceived(String username);
    }

    public static void uidToUsername(String uid, UsernameCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String username = document.getString("Username");
                            callback.onUsernameReceived(username);
                        } else {
                            callback.onUsernameReceived(null);
                        }
                    } else {
                        callback.onUsernameReceived(null);
                    }
                });
    }

    public interface BitmapCallback {
        void onBitmapReceived(Bitmap bitmap);
    }

    public static void loadImage(String id,StorageReference storageReference, BitmapCallback callback) {
        StorageReference imageRef = storageReference.child("requests/" + id);
        final long ONE_MEGABYTE = 1024 * 1024;
        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            callback.onBitmapReceived(bitmap);
        }).addOnFailureListener(e -> callback.onBitmapReceived(null));
    }

    public static double distanceDifference(Double lat1, Double long1, Double lat2, Double long2) {

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(long2 - long1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.max(6371 * c, 0.1);
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

}
