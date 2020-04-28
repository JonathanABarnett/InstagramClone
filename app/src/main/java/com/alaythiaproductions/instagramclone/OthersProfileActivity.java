package com.alaythiaproductions.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alaythiaproductions.instagramclone.adapters.PostAdapter;
import com.alaythiaproductions.instagramclone.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class OthersProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private RecyclerView postsRecyclerView;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userRef;

    private List<Post> postList;
    private PostAdapter postAdapter;
    private String receiverUid;
    private String uid = null;

    private ImageView mCoverImage, mProfileImage;
    private TextView mName, mEmail, mPhone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_others_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mName = findViewById(R.id.profile_name_tv);
        mEmail = findViewById(R.id.profile_email_tv);
        mPhone = findViewById(R.id.profile_phone_tv);
        mProfileImage = findViewById(R.id.profile_image);
        mCoverImage = findViewById(R.id.profile_cover_image);
        postsRecyclerView = findViewById(R.id.recycler_view_posts);

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userRef = firebaseDatabase.getReference("Users");

        Intent intent = getIntent();
        receiverUid = intent.getStringExtra("userUID");

        Query query = userRef.orderByChild("uid").equalTo(receiverUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    String name = ds.child("name").getValue().toString();
                    String email = ds.child("email").getValue().toString();
                    String phone = ds.child("phone").getValue().toString();
                    String image = ds.child("image").getValue().toString();
                    String cover = ds.child("cover").getValue().toString();

                    mName.setText(name);
                    mEmail.setText(email);
                    mPhone.setText(phone);

                    try {
                        Picasso.get().load(image).placeholder(R.drawable.ic_default_img_white).into(mProfileImage);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img_white).into(mProfileImage);
                    }

                    try {
                        Picasso.get().load(cover).into(mCoverImage);
                    } catch (Exception e) {
                        //Picasso.get().load(R.drawable.ic_default_img_white).into(mCoverImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postList = new ArrayList<>();

        checkUserStatus();

        loadOthersPosts();

    }

    private void loadOthersPosts() {
        // Linear Layout for RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(OthersProfileActivity.this);
        // Show Newest Post First
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        // Set layout to RecyclerView
        postsRecyclerView.setLayoutManager(layoutManager);

        // Init posts list
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts");
        // Query to load posts
        Query query = postRef.orderByChild("uid").equalTo(receiverUid);
        // Get all data from ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);

                    // Add to List
                    postList.add(post);
                    // Adapter
                    postAdapter = new PostAdapter(OthersProfileActivity.this, postList);
                    // Set Adapter to Recyclerview
                    postsRecyclerView.setAdapter(postAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OthersProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchOthersPosts(final String search) {
        // Linear Layout for RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(OthersProfileActivity.this);
        // Show Newest Post First
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        // Set layout to RecyclerView
        postsRecyclerView.setLayoutManager(layoutManager);

        // Init posts list
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts");
        // Query to load posts
        Query query = postRef.orderByChild("uid").equalTo(receiverUid);
        // Get all data from ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);

                    if (post.getPost_description().toLowerCase().contains(search.toLowerCase()) ||
                            post.getPost_title().toLowerCase().contains(search.toLowerCase())) {
                        // Add to List
                        postList.add(post);
                    }

                    // Adapter
                    postAdapter = new PostAdapter(OthersProfileActivity.this, postList);
                    // Set Adapter to Recyclerview
                    postsRecyclerView.setAdapter(postAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OthersProfileActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Hide adding post from this activity
        menu.findItem(R.id.action_add_post).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);

        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Called when user presses search
                if (!TextUtils.isEmpty(query)) {
                    searchOthersPosts(query);
                } else {
                    loadOthersPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Called on text input change
                if (!TextUtils.isEmpty(newText)) {
                    searchOthersPosts(newText);
                } else {
                    loadOthersPosts();
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get Item ID
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            mAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
