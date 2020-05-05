package com.alaythiaproductions.instagramclone.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class MemberAddAdapter extends RecyclerView.Adapter<MemberAddAdapter.HolderMemberAdd> {

    private Context context;
    private ArrayList<User> userList;
    private String groupId, groupRole;

    public MemberAddAdapter() {
    }

    public MemberAddAdapter(Context context, ArrayList<User> userList, String groupId, String groupRole) {
        this.context = context;
        this.userList = userList;
        this.groupId = groupId;
        this.groupRole = groupRole;
    }

    @NonNull
    @Override
    public HolderMemberAdd onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_member_add, parent, false);

        return new HolderMemberAdd(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderMemberAdd holder, int position) {
        // Get Data
        final User user = userList.get(position);
        String name = user.getName();
        String email = user.getEmail();
        String image = user.getImage();
        final String uid = user.getUid();

        // Set Data
        holder.row_users_name.setText(name);
        holder.row_users_email.setText(email);

        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img_white).into(holder.row_users_image);
        } catch (Exception e) {
            holder.row_users_image.setImageResource(R.drawable.ic_default_img_blue);
        }

        checkIfAlreadyExists(user, holder);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                ref.child(groupId).child("Members").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User exists and part of the group
                            String previousRole = dataSnapshot.child("role").getValue().toString();

                            // Options to display in dialog
                            String[] options;

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Choose Option");

                            if (groupRole.equals("creator")) {
                                // Current user is the group creator and someone else is the Admin
                                if (previousRole.equals("admin")) {
                                    options = new String[]{"Remove Admin", "Remove Member"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                // Remove Admin
                                                removeAdmin(user);
                                            } else {
                                                // Remove Member
                                                removeMember(user);
                                            }
                                        }
                                    }).show();
                                    // Current User is group creator and other is a member
                                } else if (previousRole.equals("member")) {
                                    options = new String[]{"Make Admin", "Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                // Make Admin
                                                makeAdmin(user);
                                            } else {
                                                // Remove Member
                                                removeMember(user);
                                            }
                                        }
                                    }).show();
                                    // Current User is admin and other is creator
                                }
                            } else if (groupRole.equals("admin")) {
                                if (previousRole.equals("creator")) {
                                    Toast.makeText(context, "Creator of Group", Toast.LENGTH_SHORT).show();
                                } else if (previousRole.equals("admin")) {
                                    options = new String[]{"Make Admin", "Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                // Make Admin
                                                removeAdmin(user);
                                            } else {
                                                // Remove Member
                                                removeMember(user);
                                            }
                                        }
                                    }).show();
                                    // Current User is admin and other is a member
                                } else if (previousRole.equals("member")) {
                                    options = new String[]{"Make Admin", "Remove User"};
                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (which == 0) {
                                                // Make Admin
                                                makeAdmin(user);
                                            } else {
                                                // Remove Member
                                                removeMember(user);
                                            }
                                        }
                                    }).show();
                                }
                            }

                        } else {
                            // User isn't part of the group
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Add Member").setMessage("Add this user to the group?")
                                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            addMember(user);
                                        }
                                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void addMember(User user) {
        // Setup User in DB
        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uid", user.getUid());
        hashMap.put("role", "member");
        hashMap.put("timestamp", timestamp);

        // Add User to Group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members").child(user.getUid()).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Added User to Group
                Toast.makeText(context, "Added Member", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error adding user
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeAdmin(User user) {
        // Setup Data
        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "admin");

        // Update User Role
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members").child(user.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Make Admin
                Toast.makeText(context, "User Updated to Admin", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error making Admin
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeMember(User user) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members").child(user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Remove Member
                Toast.makeText(context, "Member Removed", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error making Admin
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeAdmin(User user) {
        // Setup Data
        String timestamp = String.valueOf(System.currentTimeMillis());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("role", "member");

        // Update User Role
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members").child(user.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Make Admin
                Toast.makeText(context, "User Updated to Member", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error making Admin
                Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfAlreadyExists(User user, final HolderMemberAdd holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Members").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User already part of group
                    String role = dataSnapshot.child("role").getValue().toString();
                    holder.status_textview.setText(role);
                } else {
                    // User not part of group
                    holder.status_textview.setText("");
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

    class HolderMemberAdd extends RecyclerView.ViewHolder {

        private ImageView row_users_image;
        private TextView row_users_name, row_users_email, status_textview;

        public HolderMemberAdd(@NonNull View itemView) {
            super(itemView);

            row_users_image = itemView.findViewById(R.id.row_users_image);
            row_users_name = itemView.findViewById(R.id.row_users_name);
            row_users_email = itemView.findViewById(R.id.row_users_email);
            status_textview = itemView.findViewById(R.id.status_textview);
        }
    }
}
