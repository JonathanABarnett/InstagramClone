package com.alaythiaproductions.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alaythiaproductions.instagramclone.adapters.CommentAdapter;
import com.alaythiaproductions.instagramclone.models.Comment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostDetailsActivity extends AppCompatActivity {

    private String uid, myUid, myEmail, myName, myProfileImage, postId, postLikes, commenterImage, commenterName, pImage;

    // Progress Bar
    private ProgressDialog progressDialog;

    // Post Views
    private ImageView userProfileImage, post_image;
    private TextView userNameTextView, post_time, post_title, post_description, post_likes, post_comments;
    private ImageButton moreBtn;
    private Button likeBtn, shareBtn;
    private LinearLayout profileLayout;
    private RecyclerView recyclerView;

    private List<Comment> commentList;
    private CommentAdapter commentAdapter;

    // Add Comment Views
    private EditText commentEditText;
    private ImageButton sendBtn;
    private ImageView commenterProfileImage;

    private boolean mProcessComment = false;
    private boolean mProcessLike = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);

        // Action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Details");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Get Post Info from AdapterPost
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");
        //myName = intent.getStringExtra("name");

        init();

        checkUserStatus();

        loadPostInfo();

        loadUserInfo();

        setLikes();

        // Set subtitle
        actionBar.setSubtitle("Signed in as: " + myEmail);

        loadComments();

        // Send Comment
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        // Like Button
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        // More Button
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = post_time.getText().toString().trim();
                String description = post_description.getText().toString().trim();

                BitmapDrawable bitmapDrawable = (BitmapDrawable)post_image.getDrawable();
                if (bitmapDrawable == null) {
                    // Post Without Image
                    shareTextOnly(title, description);
                } else {
                    // Post With Image
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    shareImageAndText(title, description, bitmap);

                }
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
        hashMap.put("uid", myUid);

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
        startActivity(Intent.createChooser(intent, "Share Via"));
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
        startActivity(Intent.createChooser(intent, "Share Via"));

    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs(); // Create if doesn't exist
            File file = new File(imageFolder, "shared_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "com.alaythiaproductions.instagramclone.fileprovider", file);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return uri;
    }

    private void loadComments() {
        // Layout for RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        // Set Layout to RecyclerView
        recyclerView.setLayoutManager(layoutManager);

        // Init Comments List
        commentList = new ArrayList<>();

        // Firebase Post Path
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                commentList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Comment comment = ds.getValue(Comment.class);
                    commentList.add(comment);

                    // Setup Adapter
                    commentAdapter = new CommentAdapter(getApplicationContext(), commentList, myUid, postId);
                    recyclerView.setAdapter(commentAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showMoreOptions() {
        // Create Menu for more options
        final PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        // Show Delete option for posts of current user
        if (uid.equals(myUid)) {
            // Add items to Menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "Edit");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "Delete");
        }

        //Item Click Listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    // Edit Clicked
                    Intent intent = new Intent(PostDetailsActivity.this, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);
                } else if (id == 1) {
                    // Delete Clicked
                    beginDelete();
                }
                return false;
            }
        });
        // Show Menu
        popupMenu.show();
    }

    private void beginDelete() {
        // Post with or without Image
        if (pImage.equals("noImage")) {
            deleteWithoutImage();
        } else {
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        // Progress Dialog
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Deleting Post");

        StorageReference imgRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
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
                        Toast.makeText(PostDetailsActivity.this, "Post Deleted", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PostDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage() {
        // Progress Dialog
        final ProgressDialog progressDialog = new ProgressDialog(PostDetailsActivity.this);
        progressDialog.setMessage("Deleting Post");

        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("post_id").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ds.getRef().removeValue();
                }
                Toast.makeText(PostDetailsActivity.this, "Post Deleted", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setLikes() {
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postId).hasChild(myUid) && (dataSnapshot.child(postId).child(myUid).getValue().toString().equals("1"))) {
                    // User has liked this post
                    likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_liked, 0, 0, 0);
                    likeBtn.setText("Liked");
                } else {
                    // User has not like this post
                    likeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like_black, 0, 0, 0);
                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void likePost() {
// Get total number of likes for the post. If user has not like it before
        // increase like by 1 otherwise decrease by 1
        mProcessLike = true;
        // Get of of the post clicked
        final DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        final DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessLike) {
                    if (dataSnapshot.child(postId).hasChild(myUid) && (dataSnapshot.child(postId).child(myUid).getValue().toString().equals("1"))) {
                        // Already Liked - remove Like
                        postsRef.child(postId).child("post_likes").setValue("" + (Integer.parseInt(postLikes) - 1));
                        likesRef.child(postId).child(myUid).setValue("0");
                        mProcessLike = false;
                    } else {
                        // Has Not been liked
                        postsRef.child(postId).child("post_likes").setValue("" + (postLikes + 1));
                        likesRef.child(postId).child(myUid).setValue("1");
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

    private void postComment() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting comment");

        // Get Data from Comment
        final String comment = commentEditText.getText().toString().trim();

        // Validate comment
        if (TextUtils.isEmpty(comment)) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Each post will have a child "Comments" that will contain comments to that post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment_id", timestamp);
        hashMap.put("comment", comment);
        hashMap.put("timestamp", timestamp);
        hashMap.put("uid", myUid);
        hashMap.put("email", myEmail);
        hashMap.put("profile_image", myProfileImage);
        hashMap.put("name", myName);
        
        ref.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Posted Successfully
                progressDialog.dismiss();
                Toast.makeText(PostDetailsActivity.this, "Comment Added", Toast.LENGTH_SHORT).show();
                commentEditText.setText("");
                updateCommentCount();

                addToOthersNotifications(uid, myUid, "Commented on your post");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(PostDetailsActivity.this, "" + e.getMessage() , Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCommentCount() {
        mProcessComment= true;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mProcessComment) {
                    String comments = dataSnapshot.child("post_comments").getValue().toString();
                    int newCommentTotal = Integer.parseInt(comments) + 1;
                    ref.child("post_comments").setValue("" + newCommentTotal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadUserInfo() {
        // Get Current user Info
        Query query = FirebaseDatabase.getInstance().getReference("Users");
        query.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    myName = ds.child("name").getValue().toString();
                    myProfileImage = ds.child("image").getValue().toString();

                    try {
                        // If image is received
                        Picasso.get().load(myProfileImage).placeholder(R.drawable.ic_default_img_blue).into(commenterProfileImage);
                    } catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img_blue).into(commenterProfileImage);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadPostInfo() {
        // Get Post from DB with PostId
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("post_id").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check the post for all post information
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String pTitle = ds.child("post_title").getValue().toString();
                    String pDescription = ds.child("post_description").getValue().toString();
                    postLikes = ds.child("post_likes").getValue().toString();
                    String pTimestamp = ds.child("post_time").getValue().toString();
                    pImage = ds.child("post_image").getValue().toString();
                    commenterImage = ds.child("image").getValue().toString();
                    uid = ds.child("uid").getValue().toString();
                    String email = ds.child("email").getValue().toString();
                    commenterName = ds.child("name").getValue().toString();
                    String commentCount = ds.child("post_comments").getValue().toString();

                    // Convert Timestamp
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimestamp));
                    String pTime = DateFormat.format("MM/dd/yy hh:mm aa", calendar).toString();

                    // Set Data
                    post_title.setText(pTitle);
                    post_description.setText(pDescription);
                    post_likes.setText(postLikes + " Likes");
                    post_time.setText(pTime);
                    userNameTextView.setText(commenterName);
                    post_comments.setText(commentCount + " Comments");

                    // Set Profile Image
                    if (pImage.equals("noImage")) {
                        post_image.setVisibility(View.GONE);
                    } else {
                        post_image.setVisibility(View.VISIBLE);
                        try {
                            Picasso.get().load(pImage).into(post_image);
                        } catch (Exception e) {

                        }
                    }

                    // Set User Image in Comment
                    try {
                        Picasso.get().load(commenterImage).placeholder(R.drawable.ic_default_img_blue).into(userProfileImage);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img_blue).into(userProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            myEmail = user.getEmail();
            myUid = user.getUid();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Hide menu items
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void init() {
        userProfileImage = findViewById(R.id.post_profile_image);
        post_image = findViewById(R.id.post_picture);
        userNameTextView = findViewById(R.id.post_user_name);
        post_time = findViewById(R.id.post_time);
        post_title = findViewById(R.id.post_title);
        post_description = findViewById(R.id.post_description);
        post_likes = findViewById(R.id.post_likes);
        post_comments = findViewById(R.id.post_comments);
        moreBtn = findViewById(R.id.post_more_button);
        likeBtn = findViewById(R.id.post_like_button);
        shareBtn = findViewById(R.id.post_share_button);
        profileLayout = findViewById(R.id.post_profile_layout);
        recyclerView = findViewById(R.id.recycler_view_comments);

        commentEditText = findViewById(R.id.post_details_comment_edit_text);
        sendBtn = findViewById(R.id.post_details_send_button);
        commenterProfileImage = findViewById(R.id.post_details_profile_image);
    }
}
