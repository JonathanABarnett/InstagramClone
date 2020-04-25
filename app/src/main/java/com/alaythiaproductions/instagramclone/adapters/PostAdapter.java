package com.alaythiaproductions.instagramclone.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alaythiaproductions.instagramclone.OthersProfileActivity;
import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder> {

    private Context context;
    List<Post> postList;
    private String currentUserUid;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout row_post
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, int position) {
        // Get Data
        final String uid = postList.get(position).getUid();
        String email = postList.get(position).getEmail();
        String name = postList.get(position).getName();
        String image = postList.get(position).getImage();
        final String postId = postList.get(position).getPost_id();
        String postTitle = postList.get(position).getPost_title();
        String postDescription = postList.get(position).getPost_description();
        final String postPicture = postList.get(position).getPost_image();
        String postTimeStamp = postList.get(position).getPost_time();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(postTimeStamp));
        String time = DateFormat.format("MM/dd/yy hh:mm aa", calendar).toString();

        // Set Data
        holder.postName.setText(name);
        holder.postTime.setText(time);
        holder.postTitle.setText(postTitle);
        holder.postDescription.setText(postDescription);

        // User profile image
        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img_blue).into(holder.postProfileImage);
        } catch (Exception e) {

        }

        // Post Picture
        if (postPicture.equals("noImage")) {
            // Hide Imageview
            holder.postPicture.setVisibility(View.GONE);
        } else {
            // Show Imageview
            holder.postPicture.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(postPicture).into(holder.postPicture);
            } catch (Exception e) {

            }
        }

        // More Button Click
        holder.postMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(holder.postMoreBtn, uid, currentUserUid, postId, postPicture);
            }
        });
        
        // Like Button Click
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Like...", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Comment Button Click
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Comment...", Toast.LENGTH_SHORT).show();
            }
        });

        // Share Button Click
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Share...", Toast.LENGTH_SHORT).show();
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Click to go to OthersProfileActivity
                Intent intent = new Intent(context, OthersProfileActivity.class);
                intent.putExtra("uid", uid);
                context.startActivity(intent);
            }
        });
    }

    private void showMoreOptions(ImageButton postMoreBtn, String uid, String currentUserId, final String postId, final String postPicture) {
        // Create Menu for more options
        final PopupMenu popupMenu = new PopupMenu(context, postMoreBtn, Gravity.END);

        // Show Delete option for posts of current user
        if (uid.equals(currentUserId)) {
            // Add items to Menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Delete");
        }

        //Item Click Listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    // Delete Selected
                    beginDelete(postId, postPicture);
                }
                return false;
            }
        });
        // Show Menu
        popupMenu.show();
    }

    private void beginDelete(String postId, String postPicture) {
        // Post with or without Image
        if (postPicture.equals("noImage")) {
            deleteWithoutImage(postId);
        } else {
            deleteWithImage(postId, postPicture);
        }
    }

    private void deleteWithImage(final String postId, String postPicture) {
        // Progress Dialog
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting Post");

        StorageReference imgRef = FirebaseStorage.getInstance().getReferenceFromUrl(postPicture);
        imgRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Image Deleted from Storage - Delete from DB
                Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("post_id").equalTo(postId);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(context, "Post Deleted", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage(String postId) {
        // Progress Dialog
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Deleting Post");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("post_id").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
                Toast.makeText(context, "Post Deleted", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        // Views from row_posts
        ImageView postPicture, postProfileImage;
        TextView postName, postTime, postTitle, postDescription, postLikes;
        ImageButton postMoreBtn;
        Button likeBtn, commentBtn, shareBtn;
        LinearLayout profileLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            // Init views
            postPicture = itemView.findViewById(R.id.post_picture);
            postProfileImage = itemView.findViewById(R.id.post_profile_image);
            postName = itemView.findViewById(R.id.post_user_name);
            postTime = itemView.findViewById(R.id.post_time);
            postTitle = itemView.findViewById(R.id.post_title);
            postDescription = itemView.findViewById(R.id.post_description);
            postLikes = itemView.findViewById(R.id.post_likes);
            postMoreBtn = itemView.findViewById(R.id.post_more_button);
            likeBtn = itemView.findViewById(R.id.post_like_button);
            commentBtn = itemView.findViewById(R.id.post_comment_button);
            shareBtn = itemView.findViewById(R.id.post_share_button);
            profileLayout = itemView.findViewById(R.id.post_profile_layout);

        }
    }
}
