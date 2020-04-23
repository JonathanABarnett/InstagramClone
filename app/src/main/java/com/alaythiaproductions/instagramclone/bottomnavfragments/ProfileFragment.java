package com.alaythiaproductions.instagramclone.bottomnavfragments;

import android.media.Image;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alaythiaproductions.instagramclone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userRef;

    private CircleImageView mProfileImage;
    private TextView mName, mEmail, mPhone;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mName = view.findViewById(R.id.profile_name_tv);
        mEmail = view.findViewById(R.id.profile_email_tv);
        mPhone = view.findViewById(R.id.profile_phone_tv);
        mProfileImage = view.findViewById(R.id.profile_image);

        Query query = userRef.orderByChild("email").equalTo(mCurrentUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()) {
                    String name = ds.child("name").getValue().toString();
                    String email = ds.child("email").getValue().toString();
                    String phone = ds.child("phone").getValue().toString();
                    String image = ds.child("image").getValue().toString();

                    mName.setText(name);
                    mEmail.setText(email);
                    mPhone.setText(phone);

                    try {
                        Picasso.get().load(image).into(mProfileImage);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_add_image).into(mProfileImage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;

    }

}
