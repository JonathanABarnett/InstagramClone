package com.alaythiaproductions.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alaythiaproductions.instagramclone.adapters.MessageAdapter;
import com.alaythiaproductions.instagramclone.models.Message;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ImageView mProfileImage;
    private TextView mName, mUserStatus;
    private EditText mMessage;
    private ImageButton mSendBtn;

    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userRef;

    private ValueEventListener seenListener;
    private DatabaseReference userRefForSeen;

    private List<Message> chatList;
    private MessageAdapter adapter;

    private String receiverUID;
    private String receiverName;
    private String receiverImage;
    private String currentUserUID;
    private String currentUserName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Messages");

        mRecyclerView = findViewById(R.id.chat_recycler_view);
        mProfileImage = findViewById(R.id.chat_profile_image);
        mName = findViewById(R.id.chat_name);
        mUserStatus = findViewById(R.id.chat_user_status);
        mMessage = findViewById(R.id.chat_message);
        mSendBtn = findViewById(R.id.send_button);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        // Clicking on the user from the users list passes UID through the intent
        // Use that UID to get the picture, name, and chat
        Intent intent = getIntent();
        receiverUID = intent.getStringExtra("userUID");
        receiverName = intent.getStringExtra("userName");
        mAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        userRef = firebaseDatabase.getReference("Users");

        // Search user to get the receiver's info
        Query userQuery = userRef.orderByChild("uid").equalTo(receiverUID);
        // Get Receiver Profile Image and Name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    receiverName = ds.child("name").getValue().toString();
                    receiverImage = ds.child("image").getValue().toString();
                    String typingStatus = ds.child("typing_status").getValue().toString();

                    // Check typing status
                    if (typingStatus.equals(currentUserUID)) {
                        mUserStatus.setText("Typing...");
                    } else {
                        String onlineStatus = ds.child("online_status").getValue().toString();

                        if (onlineStatus.equals("Online")) {
                            mUserStatus.setText(onlineStatus);
                        } else {
                            // Convert timestamp to Time and Date
                            Calendar calendar = Calendar.getInstance(Locale.US);
                            calendar.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("MM/dd/yy hh:mm aa", calendar).toString();
                            mUserStatus.setText("Last seen online: " + dateTime);
                        }
                    }

                    mName.setText(receiverName);

                    try {
                        Picasso.get().load(receiverImage).placeholder(R.drawable.ic_default_img_blue).into(mProfileImage);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img_blue).into(mProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Send Message
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mMessage.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(ChatActivity.this, "Please enter a message to send", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(message);
                }
            }
        });

        mMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() == 0) {
                    checkTypingStatus("Not Typing");
                } else {
                    checkTypingStatus(receiverUID);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        readMessages();

        seenMessage();
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Messages");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Message message = ds.getValue(Message.class);
                    if (message.getReceiver().equals(currentUserUID) && message.getSender().equals((receiverUID))) {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);
                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Messages");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Message messages = ds.getValue(Message.class);
                    if (messages.getReceiver().equals(currentUserUID) && messages.getSender().equals(receiverUID) ||
                            messages.getReceiver().equals(receiverUID) && messages.getSender().equals(currentUserUID)) {
                        chatList.add(messages);
                    }

                    adapter = new MessageAdapter(ChatActivity.this, chatList, receiverImage);
                    adapter.notifyDataSetChanged();

                    mRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", currentUserUID);
       // hashMap.put("sender_name", currentUserName);
        hashMap.put("receiver", receiverUID);
      //  hashMap.put("receiver_name", receiverName);
        hashMap.put("message", message);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("isSeen", false);
        databaseReference.child("Messages").push().setValue(hashMap);

        // Set Message Edit Text to empty
        mMessage.setText("");
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            currentUserUID = user.getUid();

            Query userQuery = userRef.orderByChild("uid").equalTo(currentUserUID);
            // Get Receiver Profile Image and Name
            userQuery.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        currentUserName = ds.child("name").getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserUID);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online_status", status);

        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserUID);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typing_status", typing);

        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        checkOnlineStatus("Online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        checkOnlineStatus(timeStamp);
        checkTypingStatus("No One");

        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("Online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);

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
