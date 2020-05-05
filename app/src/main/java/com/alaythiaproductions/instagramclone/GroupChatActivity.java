package com.alaythiaproductions.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alaythiaproductions.instagramclone.adapters.GroupChatAdapter;
import com.alaythiaproductions.instagramclone.models.GroupChat;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private String groupId, groupRole = "";

    private Toolbar toolbar;
    private ImageView groupIconIV;
    private ImageButton attachBtn, sendBtn;
    private TextView groupNameET;
    private EditText messageET;
    private RecyclerView chatRV;

    private ArrayList<GroupChat> chatArrayList;
    private GroupChatAdapter groupChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        toolbar = findViewById(R.id.toolbar);
        groupIconIV = findViewById(R.id.group_chat_icon_imageview);
        groupNameET = findViewById(R.id.group_chat_name);
        attachBtn = findViewById(R.id.attach_button);
        sendBtn = findViewById(R.id.send_button);
        messageET = findViewById(R.id.group_message_edittext);
        chatRV = findViewById(R.id.chat_recycler_view);

        setSupportActionBar(toolbar);

        chatArrayList = new ArrayList<>();

        // Get ID of the group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");

        mAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadGroupMessages();
        loadGroupRole();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Input Data
                String message = messageET.getText().toString().trim();
                //Validate
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(GroupChatActivity.this, "Enter a message", Toast.LENGTH_SHORT).show();
                } else {
                    // Send Message
                    sendMessage(message);
                }
            }
        });

    }

    private void loadGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members").orderByChild("uid").equalTo(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    groupRole = ds.child("role").getValue().toString();

                    invalidateOptionsMenu();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadGroupMessages() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatArrayList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    GroupChat model = ds.getValue(GroupChat.class);
                    chatArrayList.add(model);
                }
                // Adapter
                groupChatAdapter = new GroupChatAdapter(GroupChatActivity.this, chatArrayList);
                // Set to Recycler View
                chatRV.setAdapter(groupChatAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String message) {

        // Timestamp
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Set Message Data
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", mAuth.getUid().toString());
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("type", "text");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Messages").child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Message Sent
                messageET.setText("");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Message Send Failure
                Toast.makeText(GroupChatActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    String groupName = ds.child("groupName").getValue().toString();
                    String groupDescription = ds.child("groupDescription").getValue().toString();
                    String groupIcon = ds.child("groupIcon").getValue().toString();
                    String timestamp = ds.child("timestamp").getValue().toString();
                    String createdBy = ds.child("createdBy").getValue().toString();

                    groupNameET.setText(groupName);
                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_default).into(groupIconIV);
                    } catch (Exception e) {
                        groupIconIV.setImageResource(R.drawable.ic_group_default);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);

        if (groupRole.equals("creator") || groupRole.equals("admin")) {
            menu.findItem(R.id.action_add_member).setVisible(true);
        } else {
            menu.findItem(R.id.action_add_member).setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == (R.id.action_add_member)) {
            Intent intent = new Intent(this, GroupMemberAddActivity.class);
            intent.putExtra("groupId", groupId);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
