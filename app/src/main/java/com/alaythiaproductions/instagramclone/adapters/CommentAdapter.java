package com.alaythiaproductions.instagramclone.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.models.Comment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyHolder> {

    private Context context;
    private List<Comment> commentList;
    private String myUid, postId;

    public CommentAdapter(Context context, List<Comment> commentList, String myUid, String postId) {
        this.context = context;
        this.commentList = commentList;
        this.myUid = myUid;
        this.postId = postId;
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
        final String uid = commentList.get(position).getUid();
        String name = commentList.get(position).getName();
        String email = commentList.get(position).getEmail();
        String image = commentList.get(position).getProfile_image();
        String timeStamp = commentList.get(position).getTimestamp();
        final String cid = commentList.get(position).getComment_id();
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
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if this comment is from the currently signed in user
                if (myUid.equals(uid)) {
                    // Show Alert Dialog
                    android.app.AlertDialog.Builder builder = new AlertDialog.Builder(v.getRootView().getContext());
                    builder.setTitle("Delete");
                    builder.setMessage("Are you sure you want to delete the comment?");
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Delete Comment
                            deleteComment(cid);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Dismiss Dialog
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                } else {
                    // Not users comment
                    Toast.makeText(context, "You can only delete your comments", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteComment(String cid) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.child("Comments").child(cid).removeValue();

        // Update the comments count
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String comments = dataSnapshot.child("post_comments").getValue().toString();
                int newCommentTotal = Integer.parseInt(comments) - 1;
                ref.child("post_comments").setValue("" + newCommentTotal);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
