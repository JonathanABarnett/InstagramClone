package com.alaythiaproductions.instagramclone.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaythiaproductions.instagramclone.GroupChatActivity;
import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.models.GroupChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.HolderGroupChat> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<GroupChat> groupChatList;

    private FirebaseAuth mAuth;

    public GroupChatAdapter() {}

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_right, parent, false);
            return new HolderGroupChat(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_groupchat_left, parent, false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {
        GroupChat model = groupChatList.get(position);

        String timestamp = model.getTimestamp();
        String message = model.getMessage();
        String senderUid = model.getSender();

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String time = DateFormat.format("MM/dd/yy hh:mm aa", calendar).toString();

        holder.messageTV.setText(message);
        holder.timeTV.setText(time);

        setUserName(model, holder);
    }

    private void setUserName(GroupChat model, final HolderGroupChat holder) {
        // Get Sender Info from UID in model
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = ds.child("name").getValue().toString();

                    holder.nameTV.setText(name); // Hide self name?
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return groupChatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (groupChatList.get(position).getSender().equals(mAuth.getUid())) {
            return MSG_TYPE_RIGHT;
        }
        else {
            return MSG_TYPE_LEFT;
        }
    }

    public GroupChatAdapter(Context context, ArrayList<GroupChat> groupChatList) {
        this.context = context;
        this.groupChatList = groupChatList;

        mAuth = FirebaseAuth.getInstance();
    }

    class HolderGroupChat extends RecyclerView.ViewHolder {

        private TextView nameTV, messageTV, timeTV;

        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);

            nameTV = itemView.findViewById(R.id.name_textview);
            messageTV = itemView.findViewById(R.id.message_textview);
            timeTV = itemView.findViewById(R.id.message_timestmp);
        }
    }
}
