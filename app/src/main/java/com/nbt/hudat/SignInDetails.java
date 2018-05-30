package com.nbt.hudat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nbt.hudat.identity_classes.User;

import me.anwarshahriar.calligrapher.Calligrapher;

public class SignInDetails extends AppCompatActivity {

        protected boolean nameIsEmpty;
        protected boolean introIsEmpty;
        protected Button done;
        private static final int REQUEST_IMAGE = 1;
        private static final String TAG = "SignInDetails";
        private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
        private EditText name;
        private EditText intro;
        private Uri uri;
        private TextWatcher twName;
        private TextWatcher twIntro;
        private de.hdodenhof.circleimageview.CircleImageView image;
        private String method;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_details);

        //font customisation
        Calligrapher cl = new Calligrapher(this);
        cl.setFont(this, "pnsb.ttf", true);

        name = (EditText) findViewById(R.id.instruction);
        intro = (EditText) findViewById(R.id.description);
        image = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.image);
        uri = null;

        Intent i = getIntent();
        method = i.getStringExtra("method");
        if(method.equals("facebook")) {
            name.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            Glide.with(this)
                    .load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString())
                    .into(image);
            uri = Uri.parse("x");
            nameIsEmpty = false;
        }


        //to invoke keyboard
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(name, InputMethodManager.SHOW_FORCED);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        nameIsEmpty = true;
        introIsEmpty = true;
        done = (Button) findViewById(R.id.done);

        twName = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
               if(!s.toString().equals(""))
                   nameIsEmpty = false;
               else {
                   done.setBackground(getDrawable(R.drawable.round_grey));
                   nameIsEmpty = true;
               }
                if(!nameIsEmpty && !introIsEmpty && uri != null)
                    done.setBackground(getDrawable(R.drawable.round));

            }
        };

        twIntro = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().equals(""))
                    introIsEmpty = false;
                else {
                    done.setBackground(getDrawable(R.drawable.round_grey));
                    introIsEmpty = true;
                }
                if(!nameIsEmpty && !introIsEmpty && uri != null)
                    done.setBackground(getDrawable(R.drawable.round));
            }

        };

        name.addTextChangedListener(twName);
        intro.addTextChangedListener(twIntro);

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
                    de.hdodenhof.circleimageview.CircleImageView image = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.image);
                    image.setImageURI(uri);
                    twIntro.afterTextChanged(intro.getText());
                }
            }
        }
    }

    protected void createUser(View view) {
        ConstraintLayout cl = (ConstraintLayout) findViewById(R.id.loading);
        cl.setVisibility(View.VISIBLE);
        findViewById(R.id.creating).setVisibility(View.VISIBLE);
        com.wang.avi.AVLoadingIndicatorView loadingView = (com.wang.avi.AVLoadingIndicatorView ) findViewById(R.id.loadingView);
        loadingView.animate();
        //upload the photo
        StorageReference storageReference =
                FirebaseStorage.getInstance()
                        .getReference("user_profile_images/").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        if(method.equals("facebook")) {
            User newUser = new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(), intro.getText().toString(), name.getText().toString(), FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
            DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
            mFirebaseDatabaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(newUser, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError,
                                       DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        Toast.makeText(SignInDetails.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignInDetails.this, MainActivity.class));
                    } else {
                        Log.w(TAG, "Unable to write message to database.",
                                databaseError.toException());
                    }
                }
            });
        }
        else
            putImageInStorage(storageReference, uri);

    }

    private void putImageInStorage(StorageReference storageReference, Uri uri) {
        storageReference.putFile(uri).addOnCompleteListener(SignInDetails.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.i("status", "pre-user instantiation");
                            User newUser = new User(FirebaseAuth.getInstance().getCurrentUser().getEmail(), intro.getText().toString(), name.getText().toString(), task.getResult().getMetadata().getDownloadUrl().toString());
                            DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
                            mFirebaseDatabaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(newUser, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError,
                                                               DatabaseReference databaseReference) {
                                            if (databaseError == null) {
                                                Toast.makeText(SignInDetails.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(SignInDetails.this, MainActivity.class));
                                            } else {
                                                Log.w(TAG, "Unable to write message to database.",
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
}
