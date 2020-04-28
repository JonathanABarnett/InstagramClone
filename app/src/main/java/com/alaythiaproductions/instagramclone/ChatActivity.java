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
import com.alaythiaproductions.instagramclone.models.User;
import com.alaythiaproductions.instagramclone.notifications.APIService;
import com.alaythiaproductions.instagramclone.notifications.Client;
import com.alaythiaproductions.instagramclone.notifications.Data;
import com.alaythiaproductions.instagramclone.notifications.Response;
import com.alaythiaproductions.instagramclone.notifications.Sender;
import com.alaythiaproductions.instagramclone.notifications.Token;
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

import retrofit2.Call;
import retrofit2.Callback;

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

    private APIService apiService;
    private boolean notify = false;

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

        // Create API Service
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

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
                notify = true;

                String message = mMessage.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(ChatActivity.this, "Please enter a message to send", Toast.LENGTH_SHORT).show();
                } else {
                    sendMessage(message);
                }

                // Set Message Edit Text to empty
                mMessage.setText("");
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

    private void sendMessage(final String message) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timeStamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", currentUserUID);
        hashMap.put("receiver", receiverUID);
        hashMap.put("message", message);
        hashMap.put("timestamp",timeStamp);
        hashMap.put("isSeen", false);
        databaseReference.child("Messages").push().setValue(hashMap);

        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(currentUserUID);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotification(receiverUID, user.getName(), message);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Create ChatList Node for DB
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUserUID).child(receiverUID);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef1.child("id").setValue(receiverUID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Create ChatList Node for DB
        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList").child(receiverUID).child(currentUserUID);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef2.child("id").setValue(currentUserUID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(final String receiverUID, final String name, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(receiverUID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(currentUserUID, name + " : " + message, "New Message", receiverUID, R.drawable.ic_default_img_blue);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(ChatActivity.this, "" + response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
        checkTypingStatus("Not Typing");

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
        // Hide SearchView and Add Posts
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);

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
