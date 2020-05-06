package com.alaythiaproductions.instagramclone.bottomnavfragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alaythiaproductions.instagramclone.AddPostActivity;
import com.alaythiaproductions.instagramclone.MainActivity;
import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.adapters.PostAdapter;
import com.alaythiaproductions.instagramclone.models.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private FirebaseAuth mAuth;

    private RecyclerView recyclerView;
    private List<Post> postList;
    private PostAdapter postAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();

        recyclerView = view.findViewById(R.id.home_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        // Show newest post first
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        // Set Layout to Recyclerview
        recyclerView.setLayoutManager(layoutManager);
        
        // Init Post List
        postList = new ArrayList<>();
        
        loadPosts();

        return view;
    }

    private void loadPosts() {
        // All posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        // Get all data from ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);
                    postList.add(post);
                    
                    // Adapter
                    postAdapter = new PostAdapter(getActivity(), postList);
                    // Set Adapter to RecyclerView
                    recyclerView.setAdapter(postAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // In case of error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPosts(final String searchQuery) {
        // All posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        // Get all data from ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);

                    if (post.getPost_title().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            post.getPost_description().toLowerCase().contains(searchQuery.toLowerCase())) {
                        postList.add(post);
                    }
                    // Adapter
                    postAdapter = new PostAdapter(getActivity(), postList);
                    // Set Adapter to RecyclerView
                    recyclerView.setAdapter(postAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // In case of error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {

        } else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    /**
     * Inflate the options menus
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_member).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);

        // SearchView to search post by title/description
        MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        // Search Listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Called when user presses search
                if (!TextUtils.isEmpty(query)) {
                    searchPosts(query);
                } else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Called when user presses key in search field
                if (!TextUtils.isEmpty(newText)) {
                    searchPosts(newText);
                } else {
                    loadPosts();
                }
                return false;
            }
        });


        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Handle Menu Item Click
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get Item ID
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            mAuth.signOut();
            checkUserStatus();
        }
        if (id == R.id.action_add_post) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}
