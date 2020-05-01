package com.alaythiaproductions.instagramclone.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
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
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alaythiaproductions.instagramclone.AddPostActivity;
import com.alaythiaproductions.instagramclone.OthersProfileActivity;
import com.alaythiaproductions.instagramclone.PostDetailsActivity;
import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyHolder> {

    private Context context;
    List<Post> postList;
    private String currentUserUid, currentName;

    private DatabaseReference likesRef;
    private DatabaseReference postsRef;
    private DatabaseReference userRef;

    private boolean mProcessLike = false;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference("Users");

        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout row_post
        View view = LayoutInflater.from(context).inflate(R.layout.row_posts, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {
        // Get Data
        final String uid = postList.get(position).getUid();
        String email = postList.get(position).getEmail();
        final String name = postList.get(position).getName();
        String image = postList.get(position).getImage();
        final String postId = postList.get(position).getPost_id();
        final String postTitle = postList.get(position).getPost_title();
        final String postDescription = postList.get(position).getPost_description();
        final String postPicture = postList.get(position).getPost_image();
        String postTimeStamp = postList.get(position).getPost_time();
        final String postLikes = postList.get(position).getPost_likes();
        String postComments = postList.get(position).getPost_comments();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(postTimeStamp));
        String time = DateFormat.format("MM/dd/yy hh:mm aa", calendar).toString();

        // Set Data
        holder.postName.setText(name);
        holder.postTime.setText(time);
        holder.postTitle.setText(postTitle);
        holder.postDescription.setText(postDescription);
        holder.postLikes.setText(postLikes + " Likes");
        holder.postComments.setText(postComments + " Comments");
        // Set Likes for Each Post
        setLikes(holder, postId);

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
                // Get total number of likes for the post. If user has not like it before
                // increase like by 1 otherwise decrease by 1
                final int post_likes = Integer.parseInt(postList.get(position).getPost_likes());
                mProcessLike = true;
                // Get of of the post clicked
                final String postId = postList.get(position).getPost_id();
                likesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (mProcessLike) {
                            if (dataSnapshot.child(postId).hasChild(currentUserUid) && (dataSnapshot.child(postId).child(currentUserUid).getValue().toString().equals("1"))) {
                                // Already Liked - remove Like
                                postsRef.child(postId).child("post_likes").setValue("" + (post_likes - 1));
                                likesRef.child(postId).child(currentUserUid).setValue("0");
                                mProcessLike = false;
                            } else {
                                // Has Not been liked
                                postsRef.child(postId).child("post_likes").setValue("" + (post_likes + 1));
                                likesRef.child(postId).child(currentUserUid).setValue("1");
                                mProcessLike = false;

                                addToOthersNotifications(uid, postId, "Liked your post");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        
        // Comment Button Click
        holder.commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send postId to PostDetailsActivity
                Intent intent = new Intent(context, PostDetailsActivity.class);
                intent.putExtra("postId", postId);
                context.startActivity(intent);

            }
        });

        // Share Button Click
        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get Image from ImageView
                BitmapDrawable bitmapDrawable = (BitmapDrawable)holder.postPicture.getDrawable();
                if (bitmapDrawable == null) {
                    // Post Without Image
                    shareTextOnly(postTitle, postDescription);
                } else {
                    // Post With Image
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(postTitle, postDescription, bitmap);

                }
            }
        });

        holder.profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Click to go to OthersProfileActivity
                Intent intent = new Intent(context, OthersProfileActivity.class);
                intent.putExtra("userUID", uid); //Check Again
                context.startActivity(intent);
            }
        });
    }

    private void addToOthersNotifications(String otherUid, String post_id, String message) {
        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("post_id", post_id);
        hashMap.put("post_uid", otherUid);
        hashMap.put("notification", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", currentUserUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(otherUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void shareTextOnly(String postTitle, String postDescription) {
        String shareBody = postTitle + "\n" + postDescription;

        // Share Intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        context.startActivity(Intent.createChooser(intent, "Share Via"));
    }

    private void shareImageAndText(String postTitle, String postDescription, Bitmap bitmap) {
        String shareBody = postTitle + "\n" + postDescription;

        Uri uri = saveImageToShare(bitmap);

        // Share Intent
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Insert Subject");
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        intent.setType("image/png");
        context.startActivity(Intent.createChooser(intent, "Share Via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs(); // Create if doesn't exist
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.alaythiaproductions.instagramclone.fileprovider", file);
        } catch (Exception e) {
            Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void setLikes(final MyHolder holder, final String postKey) {
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postKey).hasChild(currentUserUid) && (dataSnapshot.child(postKey).child(currentUserUid).getValue().toString().equals("1"))) {
                    // User has liked this post
                    holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    holder.likeBtn.setText("Liked");
                } else {
                    // User has not like this post
                    holder.likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    holder.likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showMoreOptions(ImageButton postMoreBtn, String uid, String currentUserId, final String postId, final String postPicture) {
        // Create Menu for more options
        final PopupMenu popupMenu = new PopupMenu(context, postMoreBtn, Gravity.END);

        // Show Delete option for posts of current user
        if (uid.equals(currentUserId)) {
            // Add items to Menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Edit");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Delete");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "View Details");

        //Item Click Listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    // Edit Clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    context.startActivity(intent);
                } else if (id == 1) {
                    // Delete Clicked
                    beginDelete(postId, postPicture);
                } else if (id == 2) {
                    // Details Clicked
                    Intent intent = new Intent(context, PostDetailsActivity.class);
                    intent.putExtra("postId", postId);
                    context.startActivity(intent);
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
        TextView postName, postTime, postTitle, postDescription, postLikes, postComments;
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
            postComments = itemView.findViewById(R.id.post_comments);
            postMoreBtn = itemView.findViewById(R.id.post_more_button);
            likeBtn = itemView.findViewById(R.id.post_like_button);
            commentBtn = itemView.findViewById(R.id.post_comment_button);
            shareBtn = itemView.findViewById(R.id.post_share_button);
            profileLayout = itemView.findViewById(R.id.post_profile_layout);

        }
    }
}
