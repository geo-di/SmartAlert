package com.example.smartalert;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Request> RequestArrayList;

    public RequestAdapter(Context context, ArrayList<Request> RequestArrayList) {
        this.context = context;
        this.RequestArrayList = RequestArrayList;
    }

    @NonNull
    @Override
    public RequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ViewHolder(view );
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.ViewHolder holder, int position) {
        Request model = RequestArrayList.get(position);
        holder.timeTextView.setText(Controller.timestampToDate(model.getTimestamp()));
        switch (Objects.requireNonNull(model.getType())){
            case "Fire":
                holder.iconImageView.setImageResource(R.drawable.fire);
                holder.typeTextView.setText(R.string.fire);
                break;
            case "Flood":
                holder.iconImageView.setImageResource(R.drawable.flood);
                holder.typeTextView.setText(R.string.flood);
                break;
            case "Earthquake":
                holder.iconImageView.setImageResource(R.drawable.earthquake);
                holder.typeTextView.setText(R.string.earthquake);
                break;
            case "Other":
                holder.iconImageView.setImageResource(R.drawable.danger);
                holder.typeTextView.setText(R.string.other);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ShowRequestActivity.class);
            intent.putExtra("Id",model.getId());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return RequestArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView iconImageView;
        private final TextView typeTextView;
        private final TextView timeTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.iconImageView);
            typeTextView = itemView.findViewById(R.id.typeTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);

        }
    }
}
