package com.example.smartalert;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Objects;

public class StatisticsActivity extends AppCompatActivity {
    FirebaseFirestore db;
    Integer fire,flood,earthquake,other;
    TextView fireTextView,floodTextView,earthquakeTextView,otherTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        db = FirebaseFirestore.getInstance();
        fireTextView= findViewById(R.id.fireIncidentsTextView);
        floodTextView = findViewById(R.id.floodIncidentsTextView);
        earthquakeTextView = findViewById(R.id.earthquakeIncidentsTextView);
        otherTextView = findViewById(R.id.otherIncidentsTextView);
        fire = 0;
        flood = 0;
        earthquake = 0;
        other = 0;

        Query query = db.collection("alerts");

        query.addSnapshotListener((value, error) -> {
            if (error != null ) {
                return;
            }
            assert value != null;
            for(DocumentChange documentChange: value.getDocumentChanges())
            {
                if (documentChange.getType() == DocumentChange.Type.ADDED)
                {
                    switch (Objects.requireNonNull(documentChange.getDocument().getString("Type"))){
                        case "Fire":
                            fire ++;
                            break;
                        case "Flood":
                            flood++;
                            break;
                        case "Earthquake":
                            earthquake++;
                            break;
                        case "Other":
                            other++;
                            break;
                    }
                }
            }

            fireTextView.setText(getString(R.string.fire_incidents_number,fire));
            floodTextView.setText(getString(R.string.flood_incidents_number,flood));
            earthquakeTextView.setText(getString(R.string.earthquake_incidents_number,earthquake));
            otherTextView.setText(getString(R.string.other_incidents_number,other));
        });
    }
}