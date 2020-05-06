package com.alaythiaproductions.instagramclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class GroupEditActivity extends AppCompatActivity {

    private String groupId;

    // Permission Constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    // Image Pick Constants
    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    // Permissions Array
    private String[] cameraPermissions;
    private String[] storagePermissions;

    // Picked Image Uri
    private Uri image_uri = null;

    // Actionbar
    private ActionBar actionBar;

    // Firebase Auth
    private FirebaseAuth mAuth;

    // Views
    private ImageView groupIconIV;
    private EditText groupNameTV, groupDescriptionTV;
    private FloatingActionButton editGroupBtn;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Edit Group");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        groupId = getIntent().getStringExtra("groupId");

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Group Info");
        

        groupIconIV = findViewById(R.id.create_group_icon_imageview);
        groupNameTV = findViewById(R.id.create_group_name_textview);
        groupDescriptionTV = findViewById(R.id.create_group_description_textview);
        editGroupBtn = findViewById(R.id.edit_group_button);

        // Init Permissions Array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        mAuth = FirebaseAuth.getInstance();
        checkUser();
        loadGroupInfo();

        startEditingGroup();


        // Pick Image
        groupIconIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        editGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditingGroup();
            }
        });

    }

    private void startEditingGroup() {
        // Input Data
        final String gName = groupNameTV.getText().toString().trim();
        final String gDescription = groupDescriptionTV.getText().toString().trim();
        
        // Validate Data
        if (TextUtils.isEmpty(gName)) {
            Toast.makeText(this, "A Group Name is Required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (image_uri == null) {
            // Update group without icon
            progressDialog.show();
            
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("groupName", gName);
            hashMap.put("groupDescription", gDescription);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
            ref.child(groupId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    progressDialog.dismiss();
                    Toast.makeText(GroupEditActivity.this, "Group Updated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(GroupEditActivity.this, GroupInfoActivity.class);
                    intent.putExtra("groupId", groupId);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(GroupEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            progressDialog.show();
            // Update group with icon
            String timestamp = String.valueOf(System.currentTimeMillis());

            String filePathAndName = "Group_Imgs/image" + timestamp;

            // Upload Image to Firebase
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Image Uploaded
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());
                    Uri downloadUri = uriTask.getResult();
                    if (uriTask.isSuccessful()) {

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("groupName", gName);
                        hashMap.put("groupDescription", gDescription);
                        hashMap.put("groupIcon", downloadUri.toString());

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                        ref.child(groupId).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                progressDialog.dismiss();
                                Toast.makeText(GroupEditActivity.this, "Group Updated", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(GroupEditActivity.this, GroupInfoActivity.class);
                                intent.putExtra("groupId", groupId);
                                startActivity(intent);
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(GroupEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Error
                    progressDialog.dismiss();
                    Toast.makeText(GroupEditActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    // Get Group Info
                    //String groupId = ds.child("groupId").getValue().toString();
                    String groupName = ds.child("groupName").getValue().toString();
                    String groupDescription = ds.child("groupDescription").getValue().toString();
                    String groupIcon = ds.child("groupIcon").getValue().toString();
                    String createdBy = ds.child("createdBy").getValue().toString();
                    String timestamp = ds.child("timestamp").getValue().toString();

                    // Set Group Info
                    groupNameTV.setText(groupName);
                    groupDescriptionTV.setText(groupDescription);

                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_default).into(groupIconIV);
                    } catch (Exception e) {
                        groupIconIV.setImageResource(R.drawable.ic_group_default);
                    }

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(timestamp));
                    String time = DateFormat.format("MM/dd/yy hh:mm aa", calendar).toString();

                    //loadCreatedBy(createdBy, time);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void showImagePickDialog() {
        // Options to Pick Image from
        String[] options = {"Camera", "Gallery"};
        // Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image").setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Camera
                    if (!checkCameraPermissions()) {
                        requestCameraPermissions();
                    } else {
                        pickFromCamera();
                    }
                } else {
                    // Gallery
                    if (!checkStoragePermissions()) {
                        requestStoragePermissions();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        }).show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE, "Group Icon Title");
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Group Icon Description");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermissions() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions() {
        boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermissions() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void checkUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            actionBar.setSubtitle(user.getEmail());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Handle Permission Result
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        // Permissions Allowed
                        pickFromCamera();
                    } else {
                        // Missing Permission
                        Toast.makeText(this, "Camera & Storage Permissions are Required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        // Permissions Allowed
                        pickFromGallery();
                    } else {
                        // Missing Permission
                        Toast.makeText(this, "Storage Permission is required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Handle Image Pick Result
        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                // Image Picked from Gallery
                image_uri = data.getData();
                // Set to ImageView
                groupIconIV.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                // Image Picked from Camera
                image_uri = data.getData();
                // Set to ImageView
                groupIconIV.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
