package com.alaythiaproductions.instagramclone.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaythiaproductions.instagramclone.ChatActivity;
import com.alaythiaproductions.instagramclone.OthersProfileActivity;
import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyHolder> {

    private Context context;
    private List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate row_user.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        // Get Data
        final String userUID = userList.get(position).getUid();
        String userImage = userList.get(position).getImage();
        final String userName = userList.get(position).getName();
        String userEmail = userList.get(position).getEmail();

        holder.mName.setText(userName);
        holder.mEmail.setText(userEmail);

        try{
            Picasso.get().load(userImage).placeholder(R.drawable.ic_default_img_blue).into(holder.mProfileImage);
        } catch (Exception e) {

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Profile", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Profile Clicked
                            // Click to go to OthersProfileActivity
                            Intent intent = new Intent(context, OthersProfileActivity.class);
                            intent.putExtra("userUID", userUID);
                            context.startActivity(intent);
                        }
                        if (which == 1) {
                            // Chat clicked
                            // Go to ChatActivity
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("userUID", userUID);
                            intent.putExtra("userName", userName);
                            context.startActivity(intent);
                        }
                    }
                });
                builder.create().show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        ImageView mProfileImage;
        TextView mName, mEmail;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            mProfileImage = itemView.findViewById(R.id.row_users_image);
            mName = itemView.findViewById(R.id.row_users_name);
            mEmail = itemView.findViewById(R.id.row_users_email);
        }
    }
}
