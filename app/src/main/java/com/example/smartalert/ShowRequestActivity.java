package com.example.smartalert;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShowRequestActivity extends AppCompatActivity {

    TextView title,date,uploaded,recyclerTextview;
    ImageView imageView;
    Button approveButton,rejectButton;
    String id;

    FirebaseFirestore db;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    RecyclerView recyclerView;
    ArrayList<MoreEventsModel> MoreEventsArrayList;
    MoreEventsAdapter moreEventsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_request);

        title = findViewById(R.id.textView6);
        date = findViewById(R.id.textView7);
        uploaded = findViewById(R.id.textView8);
        imageView = findViewById(R.id.imageView);
        recyclerTextview = findViewById(R.id.textView9);
        approveButton = findViewById(R.id.approveButton);
        approveButton.setOnClickListener(v -> approveRequest(id));
        rejectButton = findViewById(R.id.rejectButton);


        db = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        recyclerView = findViewById(R.id.otherusersrecyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));

        MoreEventsArrayList = new ArrayList<>();
        moreEventsAdapter = new MoreEventsAdapter(ShowRequestActivity.this,MoreEventsArrayList);
        recyclerView.setAdapter(moreEventsAdapter);



        Intent intent = getIntent();
        id =  intent.getStringExtra("Id");

        db.collection("requests").document(id).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.exists()) {
                            Request request = document.toObject(Request.class);
                            assert request != null;
                            String location = Controller.getLocation(request.getLatitude(), request.getLongitude(), this);
                            String type = null;
                            switch (Objects.requireNonNull(request.getType())){
                                case "Fire":
                                    type = this.getString(R.string.fire);
                                    break;
                                case "Flood":
                                    type = this.getString(R.string.flood);
                                    break;
                                case "Earthquake":
                                    type = this.getString(R.string.earthquake);
                                    break;
                                case "Other":
                                    type = this.getString(R.string.other);
                                    break;
                            }

                            title.setText(getString(R.string.event_on_this_location,type,location));
                            date.setText(getString(R.string.date,Controller.timestampToDate(request.getTimestamp())));

                            Controller.uidToUsername(request.getUser(), username -> uploaded.setText(getString(R.string.uploaded_by,username)));
                            uploaded.setText(getString(R.string.uploaded_by,request.getUser()));

                            Controller.loadImage(id,storageReference, bitmap -> imageView.setImageBitmap(bitmap));

                            Query query = db.collection("requests").document(id).collection("requests");

                            query.addSnapshotListener((value, error) -> {
                                if (error != null ) {
                                    return;
                                }
                                for(DocumentChange documentChange: value.getDocumentChanges())
                                {
                                    if (documentChange.getType() == DocumentChange.Type.ADDED)
                                    {

                                        Controller.uidToUsername(documentChange.getDocument().getString("User"), username ->
                                        Controller.loadImage(documentChange.getDocument().getId(), storageReference, bitmap -> {
                                            MoreEventsModel model = new MoreEventsModel();


                                            model.setImage(bitmap);
                                            model.setUser(username);
                                            model.setKmDiffernce(Controller.distanceDifference(documentChange.getDocument().getDouble("Latitude"),documentChange.getDocument().getDouble("Longitude"),request.getLatitude(),request.getLongitude()));

                                            MoreEventsArrayList.add(model);

                                            if (MoreEventsArrayList.size() == value.getDocumentChanges().size()) {

                                                moreEventsAdapter.notifyDataSetChanged();

                                                if (moreEventsAdapter.getItemCount() > 0) {
                                                    recyclerTextview.setVisibility(View.VISIBLE);
                                                } else {
                                                    recyclerTextview.setVisibility(View.GONE);
                                                }
                                            }

                                        }));
                                    }
                                }
                            });





                        }
                    }
                });

        rejectButton.setOnClickListener(v -> db.collection("requests").document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    StorageReference imageRef = storageReference.child("requests/" + id);
                    imageRef.delete().addOnSuccessListener(unused -> openManageRequestsActivity()).addOnFailureListener(e -> Log.w(TAG, "Error deleting image", e));
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e)));

    }

    private void approveRequest(String id) {
        db.collection("requests").document(id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null) {
                    db.collection("alerts").document(id).set(Objects.requireNonNull(document.getData()))
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                db.collection("requests").document(id).delete()
                                        .addOnSuccessListener(aVoid1 -> {
                                            Log.d(TAG, "DocumentSnapshot successfully deleted!");

                                            Map<String, Object> update = new HashMap<>();
                                            update.put("Timestamp",System.currentTimeMillis()/1000);

                                            db.collection("alerts").document(document.getId())
                                                    .update(update).addOnSuccessListener(unused -> {
                                                        Toast.makeText(ShowRequestActivity.this,"Alert approved succesfully!",Toast.LENGTH_LONG).show();
                                                        Intent intent1 = new Intent(ShowRequestActivity.this, ManageRequestsActivity.class);
                                                        startActivity(intent1);
                                                    });
                                        })
                                        .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
                            })
                            .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
                }
            }
        });
    }

    private void openManageRequestsActivity() {
        Intent intent = new Intent(this,ManageRequestsActivity.class);
        startActivity(intent);
    }


}