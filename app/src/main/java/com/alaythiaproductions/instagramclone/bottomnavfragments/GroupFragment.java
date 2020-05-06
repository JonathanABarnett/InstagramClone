package com.alaythiaproductions.instagramclone.bottomnavfragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alaythiaproductions.instagramclone.CreateGroupActivity;
import com.alaythiaproductions.instagramclone.MainActivity;
import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.adapters.GroupListAdapter;
import com.alaythiaproductions.instagramclone.models.GroupList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;


public class GroupFragment extends Fragment {

    private RecyclerView recyclerView;

    private FirebaseAuth mAuth;

    private ArrayList<GroupList> groupLists;
    private GroupListAdapter groupListAdapter;

    public GroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        groupLists = new ArrayList<>();

        recyclerView = view.findViewById(R.id.groups_recyclerview);
        mAuth = FirebaseAuth.getInstance();

        loadGroupsLists();

        return view;
    }

    private void loadGroupsLists() {

        groupLists.clear();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupLists.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    // If current user's uid exists in the members field of group lists show that group
                    if (ds.child("Members").child(mAuth.getUid()).exists()) {
                        GroupList model = ds.getValue(GroupList.class);
                        groupLists.add(model);
                    }
                }
                groupListAdapter = new GroupListAdapter(getActivity(), groupLists);
                recyclerView.setAdapter(groupListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void searchGroupsLists(final String query) {

        groupLists.clear();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupLists.size();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    // If current user's uid exists in the members field of group lists show that group
                    if (ds.child("Members").child(mAuth.getUid()).exists()) {
                        // Search by Group Name
                        if (ds.child("groupName").toString().toLowerCase().contains(query.toLowerCase())) {
                            GroupList model = ds.getValue(GroupList.class);
                            groupLists.add(model);
                        }

                    }
                }
                groupListAdapter = new GroupListAdapter(getActivity(), groupLists);
                recyclerView.setAdapter(groupListAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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

        // Hide Add Post Icon from fragment
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_member).setVisible(false);
        menu.findItem(R.id.action_group_info).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Called when user presses return/search
                if (!TextUtils.isEmpty(query.trim())) {
                    searchGroupsLists(query);

                } else {
                    // If searching an empty string return all groups
                    loadGroupsLists();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Called when user presses any letter
                if (!TextUtils.isEmpty(newText.trim())) {
                    searchGroupsLists(newText);

                } else {
                    // If searching an empty string return all groups
                    loadGroupsLists();
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
        } else if (id == R.id.action_create_group) {
            startActivity(new Intent(getContext(), CreateGroupActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {

        } else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}
