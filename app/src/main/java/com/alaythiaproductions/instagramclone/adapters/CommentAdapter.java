package com.alaythiaproductions.instagramclone.adapters;

import android.content.Context;
import android.media.Image;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.models.Comment;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyHolder> {

    private Context context;
    private List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Bind the row_comment layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_comments, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // Get the data
        String uid = commentList.get(position).getUid();
        String name = commentList.get(position).getName();
        String email = commentList.get(position).getEmail();
        String image = commentList.get(position).getProfile_image();
        String timeStamp = commentList.get(position).getTimestamp();
        String cid = commentList.get(position).getId();
        String comment = commentList.get(position).getComment();

        // Convert Timestamp
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String time = DateFormat.format("MM/dd/yy hh:mm aa", calendar).toString();

        holder.nameTV.setText(name);
        holder.commentTV.setText(comment);
        holder.timestampTV.setText(time);
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img_blue).into(holder.profileIV);

        } catch (Exception e) {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img_blue).into(holder.profileIV);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        // Views from row_comments
        ImageView profileIV;
        TextView nameTV, commentTV, timestampTV;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIV = itemView.findViewById(R.id.comment_profile_image);
            nameTV = itemView.findViewById(R.id.comment_profile_name);
            commentTV = itemView.findViewById(R.id.comment_text_view);
            timestampTV = itemView.findViewById(R.id.comment_timestamp);
        }
    }

}
