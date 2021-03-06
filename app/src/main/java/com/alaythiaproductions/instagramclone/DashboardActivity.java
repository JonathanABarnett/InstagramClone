package com.alaythiaproductions.instagramclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import com.alaythiaproductions.instagramclone.bottomnavfragments.ChatListFragment;
import com.alaythiaproductions.instagramclone.bottomnavfragments.GroupFragment;
import com.alaythiaproductions.instagramclone.bottomnavfragments.HomeFragment;
import com.alaythiaproductions.instagramclone.bottomnavfragments.NotificationsFragment;
import com.alaythiaproductions.instagramclone.bottomnavfragments.ProfileFragment;
import com.alaythiaproductions.instagramclone.bottomnavfragments.UsersFragment;
import com.alaythiaproductions.instagramclone.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActionBar actionBar;
    private String mUserUID;

    private BottomNavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        // Bottom Navigation
        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        // Home Fragment - Default On Start
        actionBar.setTitle("Home");
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction homeFragmentTransaction = getSupportFragmentManager().beginTransaction();
        homeFragmentTransaction.replace(R.id.content, homeFragment, "");
        homeFragmentTransaction.commit();

        checkUserStatus();

    }

    @Override
    protected void onResume() {
        checkUserStatus();

        super.onResume();
    }

    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUserUID).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // Handle Item Clicks
            switch (item.getItemId()) {
                case R.id.nav_home:
                    // Home Fragment
                    actionBar.setTitle("Home");
                    HomeFragment homeFragment = new HomeFragment();
                    FragmentTransaction homeFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    homeFragmentTransaction.replace(R.id.content, homeFragment, "");
                    homeFragmentTransaction.commit();
                    return true;
                case R.id.nav_profile:
                    // Profile Fragment
                    actionBar.setTitle("Profile");
                    ProfileFragment profileFragment = new ProfileFragment();
                    FragmentTransaction profileFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    profileFragmentTransaction.replace(R.id.content, profileFragment, "");
                    profileFragmentTransaction.commit();
                    return true;
                case R.id.nav_users:
                    // Users Fragment
                    actionBar.setTitle("Users");
                    UsersFragment usersFragment = new UsersFragment();
                    FragmentTransaction userFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    userFragmentTransaction.replace(R.id.content, usersFragment, "");
                    userFragmentTransaction.commit();
                    return true;
                case R.id.nav_chat:
                    // Users Fragment
                    actionBar.setTitle("Messages");
                    ChatListFragment chatFragment = new ChatListFragment();
                    FragmentTransaction chatFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    chatFragmentTransaction.replace(R.id.content, chatFragment, "");
                    chatFragmentTransaction.commit();
                    return true;
                case R.id.nav_more:
                    // Show More Options
                    showMoreOptions();
                    return true;
            }

            return false;
        }
    };

    private void showMoreOptions() {
        // Popup Menu to Show More Options
        PopupMenu popupMenu = new PopupMenu(this, navigationView, Gravity.END);
        // Items to Show
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "Notifications");
        popupMenu.getMenu().add(Menu.NONE, 1, 0, "Groups");

        // Menu Clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0) {
                    // Notifications Clicked
                    actionBar.setTitle("Notifications");
                    NotificationsFragment notificationsFragment = new NotificationsFragment();
                    FragmentTransaction notificationsFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    notificationsFragmentTransaction.replace(R.id.content, notificationsFragment, "");
                    notificationsFragmentTransaction.commit();
                    return true;

                } else if (id == 1) {
                    // Groups Clicked
                    actionBar.setTitle("Groups");
                    GroupFragment groupFragment = new GroupFragment();
                    FragmentTransaction groupFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    groupFragmentTransaction.replace(R.id.content, groupFragment, "");
                    groupFragmentTransaction.commit();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void checkUserStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mUserUID = user.getUid();

            // Save UID of currently signed in user in shared preferences
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUserUID);
            editor.apply();

            // Update Token
            updateToken(FirebaseInstanceId.getInstance().getToken());
        } else {
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

}
