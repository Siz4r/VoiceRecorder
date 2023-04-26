package com.example.voicerecorder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.AudioViewHolder> {
    private final List<File> records;
    private TimeAgo timeAgo;
    private final OnItemListClick onItemListClick;

    public AudioListAdapter(List<File> records, OnItemListClick onItemListClick) {
        this.records = records;
        this.onItemListClick = onItemListClick;
    }


    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_record, parent, false);
        this.timeAgo = new TimeAgo();
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        holder.list_title.setText(records.get(position).getName());
        holder.list_date.setText(timeAgo.getTimeAgo(records.get(position).lastModified()));
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView list_title;
        private final TextView list_date;
        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);

            list_title = itemView.findViewById(R.id.list_title);
            list_date = itemView.findViewById(R.id.list_date);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemListClick.onClickListener(records.get(getAdapterPosition()), getAdapterPosition());
        }
    }

    public interface OnItemListClick {
        void onClickListener(File file, int position);
    }
}
