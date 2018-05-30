package com.nbt.hudat;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nbt.hudat.identity_classes.Group;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditGroup extends AppCompatActivity {
    protected String groupId;
    protected EditText name;
    protected EditText description;
    protected EditText passcode;
    protected EditText hint;
    protected Uri uri;
    protected de.hdodenhof.circleimageview.CircleImageView image;
    protected boolean imageChanged;
    protected Group g;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        Intent i = getIntent();
        groupId = i.getStringExtra("groupId");
        Log.i("groupId", groupId);
        name = (EditText) findViewById(R.id.instruction);
        description = (EditText) findViewById(R.id.description);
        passcode = (EditText) findViewById(R.id.passcode);
        hint = (EditText) findViewById(R.id.hint);
        image = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.image);
        imageChanged = false;

        FirebaseDatabase.getInstance().getReference().child("groups").orderByKey().equalTo(groupId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                g = dataSnapshot.getValue(Group.class);
                name.setText(g.name);
                if(name == null)
                    Log.i("name is", "cull");
                description.setText(g.description);
                passcode.setText(g.description);
                hint.setText(g.hint);
                uri = Uri.parse(g.getGroupImageUrl());
                Glide.with(getApplicationContext())
                        .load(g.getGroupImageUrl())
                        .into(image);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    protected void editGroup(View view) {
        g.name = name.getText().toString();
        g.description = description.getText().toString();
        g.passcode = passcode.getText().toString();
        g.hint = hint.getText().toString();
        if(!imageChanged) {
            FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).setValue(g);
            Intent i = new Intent(getApplicationContext(), GroupDetails.class);
            i.putExtra("groupId", groupId);
            startActivity(i);
        }
        else {
            //upload the photo
            StorageReference storageReference =
                    FirebaseStorage.getInstance()
                            .getReference("group_images/").child(new Random().toString());
            putImageInStorage(storageReference, uri);
        }
    }

    private Group putImageInStorage(StorageReference storageReference, Uri uri) {
        storageReference.putFile(uri).addOnCompleteListener(EditGroup.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.i("status", "pre-user instantiation");
                            g.groupImageUrl = task.getResult().getMetadata().getDownloadUrl().toString();
                            FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).setValue(g);
                            Intent i = new Intent(getApplicationContext(), GroupDetails.class);
                            i.putExtra("groupId", groupId);
                            startActivity(i);
                        } else {
                            Log.w("Edit Group", "Image upload task was not successful.",
                                    task.getException());
                            Toast.makeText(EditGroup.this, "Error. Try again later.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        return null;
    }

    protected void chooseImage(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("editGroup", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    uri = data.getData();
                    CircleImageView image = (CircleImageView) findViewById(R.id.image);
                    image.setImageURI(uri);
                    imageChanged = true;
                }
            }
        }
    }
}
