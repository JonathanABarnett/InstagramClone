package com.alaythiaproductions.instagramclone.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaythiaproductions.instagramclone.ChatActivity;
import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.models.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.MyHolder> {

    private Context context;
    private List<User> userList;
    private HashMap<String, String> lastMessageMap;

    public ChatListAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        lastMessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final String otherUid = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        String userName = userList.get(position).getName();
        String lastMessage = lastMessageMap.get(otherUid);

        holder.userNameTV.setText(userName);
        if (lastMessage == null || lastMessage.equals("default")) {
            holder.lastMessageTV.setVisibility(View.GONE);
        } else {
            holder.lastMessageTV.setVisibility(View.VISIBLE);
            holder.lastMessageTV.setText(lastMessage);
        }
        try {
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img_blue).into(holder.profileIV);
        } catch (Exception e) {
            Picasso.get().load(R.drawable.ic_default_img_blue).into(holder.profileIV);
        }
        if (userList.get(position).getOnline_status().equals("Online")) {
            // Online
            holder.onlineStatusIV.setImageResource(R.drawable.circle_online);
        } else {
            // Offline
            holder.onlineStatusIV.setImageResource(R.drawable.circle_offline);
        }

        // Click on Chatlist User
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Chat Activity
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("userUID", otherUid);
                context.startActivity(intent);
            }
        });

    }

    public void setLastMessageMap(String userId, String lastMessage) {
        lastMessageMap.put(userId, lastMessage);

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        // Views from row_chatlist
        ImageView profileIV, onlineStatusIV;
        TextView userNameTV, lastMessageTV;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            profileIV = itemView.findViewById(R.id.chatlist_profile_image_iv);
            onlineStatusIV = itemView.findViewById(R.id.chatlist_online_status_iv);
            userNameTV = itemView.findViewById(R.id.chatlist_username_tv);
            lastMessageTV = itemView.findViewById(R.id.chatlist_last_message_tv);
        }
    }
}
