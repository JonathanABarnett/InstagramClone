package com.alaythiaproductions.instagramclone.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ReceiverCallNotAllowedException;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.alaythiaproductions.instagramclone.PostDetailsActivity;
import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.models.Notification;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.HolderNotification> {

    private Context context;
    private ArrayList<Notification> notificationList;
    private FirebaseAuth mAuth;

    public NotificationAdapter(Context context, ArrayList<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderNotification onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate view row_notification
        View view = LayoutInflater.from(context).inflate(R.layout.row_notifications, parent, false);
        return new HolderNotification(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderNotification holder, int position) {
        // Get Data
        final Notification model = notificationList.get(position);
        String name = model.getUsername();
        String notification = model.getNotification();
        final String timestamp = model.getTimestamp();
        String image = model.getProfile_image();
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String time = DateFormat.format("MM/dd/yy hh:mm aa", calendar).toString();
        String senderUid = model.getUid();
        final String postId = model.getPost_id();

        // Get Name, Email and Image of the User from their UID
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(senderUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String name = ds.child("name").getValue().toString();
                    String image = ds.child("image").getValue().toString();
                    String email = ds.child("email").getValue().toString();;

                    model.setEmail(email);
                    model.setUsername(name);
                    model.setProfile_image(image);

                    holder.nameTV.setText(name);
                    try {
                        Picasso.get().load(image).placeholder(R.drawable.ic_default_img_blue).into(holder.profileIV);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img_blue).into(holder.profileIV);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Set to Views
        holder.notificationTV.setText(notification);
        holder.timeTV.setText(time);

        // Click notification to open Post
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
// Details Clicked
                Intent intent = new Intent(context, PostDetailsActivity.class);
                intent.putExtra("postId", postId);
                context.startActivity(intent);
            }
        });

        // Long press to show delete notification option
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Show Confirmation Delete Dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete the notification?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete Notification
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                        ref.child(mAuth.getUid()).child("Notification").child(timestamp).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                               // Deleted Successfully
                                Toast.makeText(context, "Notification Deleted", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cancel
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    // Holder Class for Views of row_notification
    class HolderNotification extends RecyclerView.ViewHolder {

        // Declare Views
        ImageView profileIV;
        TextView nameTV, notificationTV, timeTV;


        public HolderNotification(@NonNull View itemView) {
            super(itemView);

            profileIV = itemView.findViewById(R.id.notification_profile_image);
            nameTV = itemView.findViewById(R.id.notification_nameTV);
            notificationTV = itemView.findViewById(R.id.notification_notifcationTV);
            timeTV = itemView.findViewById(R.id.notification_timestampTV);

        }
    }
}
