package com.alaythiaproductions.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.alaythiaproductions.instagramclone.adapters.MemberAddAdapter;
import com.alaythiaproductions.instagramclone.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupMemberAddActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private ActionBar actionBar;

    private FirebaseAuth mAuth;

    private String groupId, groupRole;

    private ArrayList<User> userList;
    private MemberAddAdapter memberAddAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_member_add);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add Members");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();


        recyclerView = findViewById(R.id.add_member_recyclerview);

        groupId = getIntent().getStringExtra("groupId");
        loadGroupInfo();

    }

    private void getAllUsers() {
        userList = new ArrayList<>();

        // Load users from DB
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);

                    // Get all users
                    if (!mAuth.getUid().equals(user.getUid())) {
                        userList.add(user);
                    }
                }
                // Setup Adapter
                memberAddAdapter = new MemberAddAdapter(GroupMemberAddActivity.this, userList, groupId, groupRole);

                // Set Adapter to Recyclerview
                recyclerView.setAdapter(memberAddAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadGroupInfo() {
        final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                    String groupId = ds.child("groupId").getValue().toString();
                    final String groupName = ds.child("groupName").getValue().toString();
                    String groupDescription = ds.child("groupDescription").getValue().toString();
                    String groupIcon = ds.child("groupIcon").getValue().toString();
                    String createdBy = ds.child("createdBy").getValue().toString();
                    String timestamp = ds.child("timestamp").getValue().toString();
                    actionBar.setTitle("Add Member");

                    ref1.child(groupId).child("Members").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                groupRole = dataSnapshot.child("role").getValue().toString();
                                actionBar.setTitle(groupName + "(" + groupRole + ")");

                                getAllUsers();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
