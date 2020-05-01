package com.alaythiaproductions.instagramclone.bottomnavfragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.adapters.NotificationAdapter;
import com.alaythiaproductions.instagramclone.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private ArrayList<Notification> notificationList;
    private NotificationAdapter notificationAdapter;
    // RecyclerView
    private RecyclerView notifcation_recyclerview;


    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        notifcation_recyclerview = view.findViewById(R.id.notification_recyclerview);

        mAuth = FirebaseAuth.getInstance();

        getAllNotifications();

        return view;
    }

    private void getAllNotifications() {
        notificationList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(mAuth.getUid()).child("Notifications").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                notificationList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    // Get Notifications
                    Notification model = ds.getValue(Notification.class);
                    // Add to List
                    notificationList.add(model);
                }

                // Adapter
                notificationAdapter = new NotificationAdapter(getActivity(), notificationList);
                notifcation_recyclerview.setAdapter(notificationAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
