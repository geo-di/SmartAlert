package com.example.smartalert;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MoreEventsAdapter extends RecyclerView.Adapter<MoreEventsAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<MoreEventsModel> MoreEventsModelList;

    public MoreEventsAdapter(Context context, ArrayList<MoreEventsModel> moreEventsModelList) {
        this.context = context;
        this.MoreEventsModelList = moreEventsModelList;
    }

    @NonNull
    @Override
    public MoreEventsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_users_item, parent, false);
        return new MoreEventsAdapter.ViewHolder(view );
    }

    @Override
    public void onBindViewHolder(@NonNull MoreEventsAdapter.ViewHolder holder, int position) {
        MoreEventsModel model = MoreEventsModelList.get(position);
        holder.userTextView.setText(model.getUser());
        holder.kmTextView.setText(context.getString(R.string.kms_away,model.getKmDiffernce().toString()));
        holder.photoImageView.setImageBitmap(model.getImage());
    }

    @Override
    public int getItemCount() {
        return MoreEventsModelList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView photoImageView;
        private final TextView userTextView;
        private final TextView kmTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            userTextView = itemView.findViewById(R.id.userTextView);
            kmTextView = itemView.findViewById(R.id.kmTextView);

        }
    }
}
