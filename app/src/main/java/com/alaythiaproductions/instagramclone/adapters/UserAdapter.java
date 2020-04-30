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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyHolder> {

    private Context context;
    private List<User> userList;

    // Get Current UserId
    private FirebaseAuth mAuth;
    private String currentUID;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;

        mAuth = FirebaseAuth.getInstance();
        currentUID = mAuth.getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate row_user.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_users, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {
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

        holder.mBlockIV.setImageResource(R.drawable.ic_unblocked_green);
        // Check if each user is blocked or not
        checkIsBlocked(userUID, holder, position);

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
                            inBlockedList(userUID);
                        }
                    }
                });
                builder.create().show();
            }
        });

        // Click to Block/Unblock User
        holder.mBlockIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userList.get(position).getIsBlocked()) {
                    unblockUser(userUID);
                } else {
                    blockUser(userUID);
                }
            }
        });
    }

    private void inBlockedList(final String userUID) {
        // If current user is blocked by other user
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(userUID).child("BlockedUsers").orderByChild("uid").equalTo(currentUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.exists()) {
                                Toast.makeText(context, "You are currently blocked by that user", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        // Not blocked
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("userUID", userUID);
                        context.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void checkIsBlocked(String userUID, final MyHolder holder, final int position) {
        // Check each user if they are blocked
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid()).child("BlockedUsers").orderByChild("uid").equalTo(userUID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.exists()) {
                                holder.mBlockIV.setImageResource(R.drawable.ic_blocked_red);
                                userList.get(position).setIsBlocked(true);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void blockUser(String userUID) {
        // Block the user and add to blocked users node

        // Put Values in Hashmap
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", userUID);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(currentUID).child("BlockedUsers").child(userUID).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Block Successfully
                Toast.makeText(context, "Blocked Successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to Block
                Toast.makeText(context, "Error  " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unblockUser(String userUID) {
        // Unblock the user and add to blocked users node
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(currentUID).child("BlockedUsers").orderByChild("uid").equalTo(userUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.exists()) {
                        ds.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Unblocked Successfully
                                Toast.makeText(context, "Unblocked Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed to Unblock
                                Toast.makeText(context, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        ImageView mProfileImage, mBlockIV;
        TextView mName, mEmail;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            mProfileImage = itemView.findViewById(R.id.row_users_image);
            mBlockIV = itemView.findViewById(R.id.blocked_imageview);
            mName = itemView.findViewById(R.id.row_users_name);
            mEmail = itemView.findViewById(R.id.row_users_email);
        }
    }
}
