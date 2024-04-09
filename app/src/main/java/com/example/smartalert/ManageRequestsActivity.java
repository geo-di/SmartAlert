package com.example.smartalert;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class ManageRequestsActivity extends AppCompatActivity{

    RecyclerView recyclerView;
    FirebaseFirestore db;
    ArrayList<Request> RequestArrayList;
    RequestAdapter requestAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_requests);

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));


        RequestArrayList = new ArrayList<>();


        db = FirebaseFirestore.getInstance();

        requestAdapter = new RequestAdapter(ManageRequestsActivity.this,RequestArrayList );

        recyclerView.setAdapter(requestAdapter);

        ShowRequests();
    }

    private void ShowRequests() {

        Query query = db.collection("requests").orderBy("Points", Query.Direction.DESCENDING);

        query.addSnapshotListener((value, error) -> {
            if (error != null ) {
                return;
            }
            for(DocumentChange documentChange: value.getDocumentChanges())
            {
                if (documentChange.getType() == DocumentChange.Type.ADDED)
                {
                    RequestArrayList.add(documentChange.getDocument().toObject(Request.class));
                    RequestArrayList.get(RequestArrayList.size()-1).setId(documentChange.getDocument().getId());
                }
                requestAdapter.notifyDataSetChanged();
            }
        });




    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}