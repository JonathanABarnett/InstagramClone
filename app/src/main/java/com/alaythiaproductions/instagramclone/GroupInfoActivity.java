package com.alaythiaproductions.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.style.TtsSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alaythiaproductions.instagramclone.adapters.MemberAddAdapter;
import com.alaythiaproductions.instagramclone.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {

    private String groupId;
    private String groupRole = "";

    private FirebaseAuth mAuth;

    private ActionBar actionBar;

    private ImageView groupIconIV;
    private TextView descriptionTV, createdByTV, editGroupTV, addMemberTV, leaveGroupTV, numMembersTV;
    private RecyclerView recyclerView;

    private ArrayList<User> userList;
    private MemberAddAdapter memberAddAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        groupIconIV = findViewById(R.id.group_icon);
        descriptionTV = findViewById(R.id.description_textview);
        createdByTV = findViewById(R.id.createdby_textview);
        editGroupTV = findViewById(R.id.edit_group_textview);
        addMemberTV = findViewById(R.id.add_member_textview);
        leaveGroupTV = findViewById(R.id.leave_group_textview);
        numMembersTV = findViewById(R.id.no_of_members_textview);
        recyclerView = findViewById(R.id.group_info_recyclerview);

        // Get Intent from GroupChatActivity
        groupId = getIntent().getStringExtra("groupId");

        mAuth = FirebaseAuth.getInstance();

        loadGroupInfo();
        loadGroupRole();

        addMemberTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send intent to GroupMemberActivity
                Intent intent = new Intent(GroupInfoActivity.this, GroupMemberAddActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        leaveGroupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the user is a member/admin - leave group
                // If the user is the creator - delete gorup
                String dialogTitle = "";
                String dialogDescription = "";
                String positiveButtonTitle = "";
                if (groupRole.equals("Creator")) {
                    dialogTitle = "Delete Group";
                    dialogDescription = "Are you sure you want to delete the group permanently?";
                    positiveButtonTitle = "Delete";
                } else {
                    dialogTitle = "Leave Group";
                    dialogDescription = "Are you sure you want to leave the group?";
                    positiveButtonTitle = "Leave";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle).setMessage(dialogDescription).setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (groupRole.equals("Creator")) {
                            // Creator: Delete Group
                            deleteGroup();
                        } else {
                            // Admin/Member: Leave Group
                            leaveGroup();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }
        });

        editGroupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send intent to GroupMemberActivity
                Intent intent = new Intent(GroupInfoActivity.this, GroupEditActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });
    }

    private void leaveGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members").child(mAuth.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Left Group Successfully
                Toast.makeText(GroupInfoActivity.this, "You have left the group", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(GroupInfoActivity.this, DashboardActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error Leaving Group
                Toast.makeText(GroupInfoActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteGroup() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Group Deleted Successfully
                Toast.makeText(GroupInfoActivity.this, "Deleted Group", Toast.LENGTH_SHORT).show();

                Toast.makeText(GroupInfoActivity.this, "You have left the group", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error Leaving Group
                Toast.makeText(GroupInfoActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    // Get Group Info
                    //String groupId = ds.child("groupId").getValue().toString();
                    String groupName = ds.child("groupName").getValue().toString();
                    String groupDescription = ds.child("groupDescription").getValue().toString();
                    String groupIcon = ds.child("groupIcon").getValue().toString();
                    String createdBy = ds.child("createdBy").getValue().toString();
                    String timestamp = ds.child("timestamp").getValue().toString();

                    // Set Group Info
                    actionBar.setTitle(groupName);
                    descriptionTV.setText(groupDescription);

                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_default).into(groupIconIV);
                    } catch (Exception e) {
                        groupIconIV.setImageResource(R.drawable.ic_group_default);
                    }

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(timestamp));
                    String time = DateFormat.format("MM/dd/yy hh:mm aa", calendar).toString();

                    loadCreatedBy(createdBy, time);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadCreatedBy(String createdBy, final String time) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = ds.child("name").getValue().toString();
                    createdByTV.setText("Created by " + name + " on " + time);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members").orderByChild("uid").equalTo(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    groupRole = ds.child("role").getValue().toString();
                    actionBar.setSubtitle(mAuth.getCurrentUser().getEmail() + " (" + groupRole + ")");

                    if (groupRole.equals("Member")) {
                        editGroupTV.setVisibility(View.GONE);
                        addMemberTV.setVisibility(View.GONE);
                        leaveGroupTV.setText("Leave Group");
                    } else if (groupRole.equals("Admin")) {
                        editGroupTV.setVisibility(View.GONE);
                        addMemberTV.setVisibility(View.VISIBLE);
                        leaveGroupTV.setText("Leave Group");
                    } else if (groupRole.equals("Creator")) {
                        editGroupTV.setVisibility(View.VISIBLE);
                        addMemberTV.setVisibility(View.VISIBLE);
                        leaveGroupTV.setText("Delete Group");
                    }
                }

                loadMembers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMembers() {
        userList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    // Get UID from Group
                    String uid = ds.child("uid").getValue().toString();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                User user = ds.getValue(User.class);

                                userList.add(user);
                            }

                            // Adapter
                            memberAddAdapter = new MemberAddAdapter(GroupInfoActivity.this, userList, groupId, groupRole);
                            // Set Adapter
                            recyclerView.setAdapter(memberAddAdapter);
                            numMembersTV.setText("Members: " + userList.size());

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
