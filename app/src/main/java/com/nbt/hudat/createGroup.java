package com.nbt.hudat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nbt.hudat.identity_classes.Group;
import com.nbt.hudat.identity_classes.GroupLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.rockerhieu.emojiconize.Emojiconize;
import me.anwarshahriar.calligrapher.Calligrapher;

public class createGroup extends AppCompatActivity {
        protected  EditText name;
        protected  EditText description;
        protected EditText passcode;
        protected EditText hint;
        protected  Uri uri;
        protected  boolean nameIsEmpty;
        protected boolean descriptionIsEmpty;
        protected boolean passcodeIsEmpty;
        protected Button create;
        private TextWatcher twName;
        private TextWatcher twDescription;
        private TextWatcher twPasscode;
        protected TextView charCount;
        protected boolean inputValid;

        private static final int REQUEST_IMAGE = 1;
        private static final String TAG = "SignInDetails";
        private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            Emojiconize.activity(this).go();
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_create_group);

            //font customisation
            Calligrapher cl = new Calligrapher(this);
            cl.setFont(this, "pnsb.ttf", true);

            name = (EditText) findViewById(R.id.instruction);
            description = (EditText) findViewById(R.id.description);
            passcode = (EditText) findViewById(R.id.passcode);
            hint = (EditText) findViewById(R.id.hint);
            uri = null;
            inputValid = false;

            //to invoke keyboard
            ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(name, InputMethodManager.SHOW_FORCED);

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            nameIsEmpty = true;
            descriptionIsEmpty = true;
            passcodeIsEmpty = true;
            create = (Button) findViewById(R.id.create);

            twName = new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(!s.toString().equals("")) {
                        nameIsEmpty = false;
                    }
                    else {
                        create.setBackground(getDrawable(R.drawable.round_grey));
                        nameIsEmpty = true;
                        inputValid = false;
                    }
                    if(!nameIsEmpty && !passcodeIsEmpty && !descriptionIsEmpty && uri != null) {
                        create.setBackground(getDrawable(R.drawable.round));
                        inputValid = true;
                    }

                }
            };

            charCount = (TextView) findViewById(R.id.charCount);

            twDescription = new TextWatcher() {
                CharSequence old = "";
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    charCount.setText("" + s.length() + "/80");
                    if(s.length() > 80) {
                        charCount.setTextColor(getResources().getColor(R.color.red));
                        inputValid = false;
                    }
                    else if(charCount.getTextColors() == getResources().getColorStateList(R.color.red)) {
                        charCount.setTextColor(getResources().getColorStateList(R.color.white));
                        inputValid = true;
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(!s.toString().equals("")) {
                        descriptionIsEmpty = false;
                    }
                    else {
                        create.setBackground(getDrawable(R.drawable.round_grey));
                        descriptionIsEmpty = true;
                        inputValid = false;
                    }
                    if(!nameIsEmpty && !passcodeIsEmpty && !descriptionIsEmpty && uri != null) {
                        create.setBackground(getDrawable(R.drawable.round));
                        inputValid = true;
                    }
                }

            };

            twPasscode = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(s.toString().equals("")){
                        create.setBackground(getDrawable(R.drawable.round_grey));
                        passcodeIsEmpty = true;
                        inputValid = false;
                    }
                    else
                        passcodeIsEmpty = false;
                    if(!nameIsEmpty && !descriptionIsEmpty && !passcodeIsEmpty && uri != null) {
                        create.setBackground(getDrawable(R.drawable.round));
                        inputValid = true;
                    }
                }
            };

            name.addTextChangedListener(twName);
            description.addTextChangedListener(twDescription);
            passcode.addTextChangedListener(twPasscode);

            DatabaseReference dr = FirebaseDatabase.getInstance().getReference();
            dr = dr.child("groups");
            dr.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Group g = dataSnapshot.getValue(Group.class);
                    Toast.makeText(createGroup.this, "fetched group", Toast.LENGTH_SHORT).show();
                    if(g.name.equals("Amcisa ")) {
                        g.addAdmin(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        Map<String, Object> updates = new HashMap<String, Object>();
                        updates.put("/groups/" + dataSnapshot.getKey(), g);
                        FirebaseDatabase.getInstance().getReference().updateChildren(updates);
                        Toast.makeText(createGroup.this, "completed", Toast.LENGTH_SHORT).show();
                    }
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

        protected void chooseImage(View view) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_IMAGE);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

            if (requestCode == REQUEST_IMAGE) {
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        uri = data.getData();
                        Log.d(TAG, "Uri: " + uri.toString());
                        CircleImageView image = (CircleImageView) findViewById(R.id.image);
                        image.setImageURI(uri);
                        twDescription.afterTextChanged(description.getText());
                    }
                }
            }
        }

        protected void createGroup(View view) {
            if(!inputValid) {
                Toast.makeText(this, "input invalid", Toast.LENGTH_SHORT).show();
                return;
            }
            if (charCount.getTextColors() == getResources().getColorStateList(R.color.red)) {
                Toast.makeText(this, "Description is too long", Toast.LENGTH_SHORT).show();
                return;
            }
            //upload the photo
            StorageReference storageReference =
                    FirebaseStorage.getInstance()
                            .getReference("group_images/").child(new Random().toString());
            putImageInStorage(storageReference, uri);

        }

        private void putImageInStorage(StorageReference storageReference, Uri uri) {
            storageReference.putFile(uri).addOnCompleteListener(createGroup.this,
                    new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.i("status", "pre-user instantiation");
                                Group g = new Group(name.getText().toString(), description.getText().toString(), task.getResult().getMetadata().getDownloadUrl().toString(), passcode.getText().toString(), hint.getText().toString());
                                g.addAdmin(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                g.addMember(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
                                String key = mFirebaseDatabaseReference.child("groups").push().getKey();
                                FirebaseDatabase.getInstance().getReference().child("users").child("groups").child(key).setValue(new GroupLocation(key));
                                mFirebaseDatabaseReference.child("groups").child(key).setValue(g, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError,
                                                           DatabaseReference databaseReference) {
                                        if (databaseError == null) {
                                            Toast.makeText(createGroup.this, "Group created successfully", Toast.LENGTH_SHORT).show();
                                            Intent i = new Intent(createGroup.this, invite.class);
                                            i.putExtra("name", name.getText().toString());
                                            i.putExtra("passcode", passcode.getText().toString());
                                            i.putExtra("justCreated", true);
                                            startActivity(i);
                                        } else {
                                            Log.w(TAG, "Unable to write group to database.",
                                                    databaseError.toException());
                                        }
                                    }
                                });
                            } else {
                                Log.w(TAG, "Image upload task was not successful.",
                                        task.getException());
                            }
                        }
                    });
        }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        return Actions.newView("createGroup", "http://[ENTER-YOUR-URL-HERE]");
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().start(getIndexApiAction());
    }

    @Override
    public void onStop() {

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        FirebaseUserActions.getInstance().end(getIndexApiAction());
        super.onStop();
    }
}
