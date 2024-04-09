package com.example.smartalert;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.common.base.Objects;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class NotificationService extends Service implements LocationListener {
    FirebaseFirestore db;
    LocationManager locationManager;
    String title,content,message;

    public NotificationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = FirebaseFirestore.getInstance();

        Query query = db.collection("alerts");

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED || documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 5, this);

                        if (locationManager != null) {
                            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            assert location != null;
                            if (Objects.equal(Controller.getLocation(location.getLatitude(), location.getLongitude(), this), Controller.getLocation(documentChange.getDocument().getDouble("Latitude"), documentChange.getDocument().getDouble("Longitude"), this))) {
                                if (documentChange.getDocument().getLong("Timestamp") + 60 > System.currentTimeMillis() / 1000) {
                                    title = this.getString(R.string.title);
                                    switch (documentChange.getDocument().getString("Type")) {
                                        case "Fire":
                                            content = this.getString(R.string.content, this.getString(R.string.fire), Controller.getLocation(documentChange.getDocument().getDouble("Latitude"), documentChange.getDocument().getDouble("Longitude"), this));
                                            message = this.getString(R.string.fire_message, String.valueOf(Controller.distanceDifference(location.getLatitude(), location.getLongitude(), documentChange.getDocument().getDouble("Latitude"), documentChange.getDocument().getDouble("Longitude"))),Controller.timestampToDate(documentChange.getDocument().getLong("Timestamp")));
                                            break;
                                        case "Flood":
                                            content = this.getString(R.string.content, this.getString(R.string.flood), Controller.getLocation(documentChange.getDocument().getDouble("Latitude"), documentChange.getDocument().getDouble("Longitude"), this));
                                            message = this.getString(R.string.flood_message, String.valueOf(Controller.distanceDifference(location.getLatitude(), location.getLongitude(), documentChange.getDocument().getDouble("Latitude"), documentChange.getDocument().getDouble("Longitude"))),Controller.timestampToDate(documentChange.getDocument().getLong("Timestamp")));
                                            break;
                                        case "Earthquake":
                                            content = this.getString(R.string.content, this.getString(R.string.earthquake), Controller.getLocation(documentChange.getDocument().getDouble("Latitude"), documentChange.getDocument().getDouble("Longitude"), this));
                                            message = this.getString(R.string.earthquake_message, String.valueOf(Controller.distanceDifference(location.getLatitude(), location.getLongitude(), documentChange.getDocument().getDouble("Latitude"), documentChange.getDocument().getDouble("Longitude"))),Controller.timestampToDate(documentChange.getDocument().getLong("Timestamp")));
                                            break;
                                        case "Other":
                                            content = this.getString(R.string.content, this.getString(R.string.other), Controller.getLocation(documentChange.getDocument().getDouble("Latitude"), documentChange.getDocument().getDouble("Longitude"), this));
                                            message = this.getString(R.string.other_message, String.valueOf(Controller.distanceDifference(location.getLatitude(), location.getLongitude(), documentChange.getDocument().getDouble("Latitude"), documentChange.getDocument().getDouble("Longitude"))),Controller.timestampToDate(documentChange.getDocument().getLong("Timestamp")));
                                            break;
                                    }

                                    createNotification(title, content, message);
                                }


                            }
                        }
                    }
                }
            }

        });





        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotification(String title,String content,String message) {
        String channelId = "CHANNEL_ID";
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
            if (channel==null)
            {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                channel = new NotificationChannel(channelId,"Notification Channel",importance);
                channel.setLightColor(Color.GREEN);
                channel.enableVibration(true);
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        notificationManager.notify(0,builder.build());

    }





    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }
}