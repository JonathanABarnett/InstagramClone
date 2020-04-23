package com.alaythiaproductions.instagramclone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ImageView mProfileImage;
    private TextView mName, mUserStatus;
    private EditText mMessage;
    private ImageButton mSendBtn;

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
    }
}
