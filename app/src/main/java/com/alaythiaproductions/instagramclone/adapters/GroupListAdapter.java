package com.alaythiaproductions.instagramclone.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaythiaproductions.instagramclone.GroupChatActivity;
import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.models.GroupList;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.HolderGroupList> {

    private Context context;
    private ArrayList<GroupList> groupLists;

    public GroupListAdapter(Context context, ArrayList<GroupList> groupLists) {
        this.context = context;
        this.groupLists = groupLists;
    }

    @NonNull
    @Override
    public HolderGroupList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_groups, parent, false);

        return new HolderGroupList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupList holder, int position) {

        // Get Data
        GroupList model = groupLists.get(position);
        final String groupId = model.getGroupId();
        String icon = model.getGroupIcon();
        String name = model.getGroupName();

        holder.group_name_textview.setText("");
        holder.group_timestamp_textview.setText("");
        holder.group_message_textview.setText("");

        // Load last message and message time
        loadLastMessage(model, holder);

        // Set Data
        holder.group_name_textview.setText(name);

         try {
             Picasso.get().load(icon).placeholder(R.drawable.ic_group_default).into(holder.group_icon_imageview);
         } catch (Exception e) {
             holder.group_icon_imageview.setImageResource(R.drawable.ic_group_default);
         }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Group Chat
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId", groupId);
                context.startActivity(intent);
            }
        });
    }

    private void loadLastMessage(GroupList model, final HolderGroupList holder) {
        // Get last message from group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String message = ds.child("message").getValue().toString();
                    String timestamp = ds.child("timestamp").getValue().toString();
                    String sender = ds.child("sender").getValue().toString();
                    String messageType = ds.child("type").getValue().toString();

                    // Convert timestamp
                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(timestamp));
                    String time = DateFormat.format("MM/dd/yy hh:mm aa", calendar).toString();

                    if (messageType.equals("image")) {
                        holder.group_message_textview.setText("Sent Photo");
                    } else {
                        holder.group_message_textview.setText(message);
                    }

                    holder.group_timestamp_textview.setText(time);

                    // Get name of last sender of message
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(sender).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                String name = ds.child("name").getValue().toString();
                                holder.group_sender_textview.setText(name);
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
    public int getItemCount() {
        return groupLists.size();
    }

    class HolderGroupList extends RecyclerView.ViewHolder {

        ImageView group_icon_imageview;
        TextView group_name_textview, group_sender_textview, group_message_textview, group_timestamp_textview;

        public HolderGroupList(@NonNull View itemView) {
            super(itemView);

            group_icon_imageview = itemView.findViewById(R.id.group_icon_imageview);
            group_name_textview = itemView.findViewById(R.id.group_name_textview);
            group_sender_textview = itemView.findViewById(R.id.group_sender_textview);
            group_message_textview = itemView.findViewById(R.id.group_message_textview);
            group_timestamp_textview = itemView.findViewById(R.id.group_timestamp_textview);

        }
    }
}
