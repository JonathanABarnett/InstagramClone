package com.alaythiaproductions.instagramclone.bottomnavfragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.alaythiaproductions.instagramclone.CreateGroupActivity;
import com.alaythiaproductions.instagramclone.MainActivity;
import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.adapters.ChatListAdapter;
import com.alaythiaproductions.instagramclone.models.ChatList;
import com.alaythiaproductions.instagramclone.models.Message;
import com.alaythiaproductions.instagramclone.models.User;
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
public class ChatListFragment extends Fragment {

    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private List<ChatList> chatListList;
    private List<User> userList;
    private DatabaseReference reference;
    private FirebaseUser currentUser;
    private ChatListAdapter chatListAdapter;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getInstance().getCurrentUser();

        chatListList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatListList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ChatList chatList = ds.getValue(ChatList.class);
                    chatListList.add(chatList);
                }
                loadChats();
             }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        recyclerView = view.findViewById(R.id.chatlist_recycler_view);

        return view;
    }

    private void loadChats() {
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    for (ChatList chatList : chatListList) {
                        if (user.getUid() != null &&  user.getUid().equals(chatList.getId())) {
                            userList.add(user);
                            break;
                        }
                    }
                    // Adapter
                    chatListAdapter = new ChatListAdapter(getContext(), userList);
                    // Set Adapter
                    recyclerView.setAdapter(chatListAdapter);
                    // Set Last Message
                    for (int i = 0; i < userList.size(); i++) {
                        lastMessage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMessage(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String lastMessage = "default";
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Message message = ds.getValue(Message.class);
                    if (message == null) {
                        continue;
                    }
                    String sender = message.getSender();
                    String receiver = message.getReceiver();
                    if (sender == null || receiver == null) {
                        continue;
                    }
                    if (message.getReceiver().equals(currentUser.getUid()) && message.getSender().equals(userId)
                    || message.getReceiver().equals(userId) && message.getSender().equals(currentUser.getUid())) {
                        if (message.getType().equals("image")) {
                            lastMessage = "Sent an image";
                        } else {
                            lastMessage = message.getMessage();
                        }
                    }
                }
                chatListAdapter.setLastMessageMap(userId, lastMessage);
                chatListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

        // Hide Add Post Icon from fragment
        menu.findItem(R.id.action_add_post).setVisible(false);

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
}
