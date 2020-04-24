package com.alaythiaproductions.instagramclone.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private Context context;
    private List<Message> chatList;
    private String imageUrl;

    private FirebaseUser currentUser;

    public MessageAdapter() {}

    public MessageAdapter(Context context, List<Message> chatList, String imageUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new MyHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String message = chatList.get(position).getMessage();
        String timeStamp = chatList.get(position).getTimestamp();

        Calendar calendar = Calendar.getInstance(Locale.US);
        calendar.setTimeInMillis(Long.parseLong(timeStamp));
        String dateTime = DateFormat.format("MM/dd/yy hh:mm aa", calendar).toString();

        holder.messageTV.setText(message);
        holder.timeTV.setText(dateTime);

        try {
            Picasso.get().load(imageUrl).into(holder.profileImage);
        } catch (Exception e) {

        }

        if (position == chatList.size() - 1) {
            if (chatList.get(position).isSeen()) {
                holder.isSeenTV.setText("Seen");
            } else {
                holder.isSeenTV.setText("Delivered");
            }
        } else {
            holder.isSeenTV.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        // Get Current User
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (chatList.get(position).getSender().equals(currentUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return  MSG_TYPE_LEFT;
        }
    }

    // View Holder
    class MyHolder extends RecyclerView.ViewHolder {

        ImageView profileImage;
        TextView messageTV, timeTV, isSeenTV;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.chat_profile_image);
            messageTV = itemView.findViewById(R.id.chat_message);
            timeTV = itemView.findViewById(R.id.chat_time);
            isSeenTV = itemView.findViewById(R.id.chat_seen);
        }
    }
}
