package com.nbt.hudat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nbt.hudat.identity_classes.Chat;
import com.nbt.hudat.identity_classes.ChatUnit;
import com.nbt.hudat.identity_classes.Message;
import com.nbt.hudat.tools.EmojiGetter;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Random;

import me.anwarshahriar.calligrapher.Calligrapher;

public class createChat extends AppCompatActivity {

    private boolean inputValid;
    private boolean nameIsEmpty;
    private TextWatcher tw1;
    private TextWatcher tw2;
    private Button start;
    private EditText name;
    private EmojiPopup emojiPopup;
    private String groupId;
    private String memberId;
    private String groupName;
    private final String TAG = "Create chat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EmojiManager.install(new IosEmojiProvider());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat);

        //font customisation
        Calligrapher cl = new Calligrapher(this);
        cl.setFont(this, "pnsb.ttf", true);

        Intent i = getIntent();
        groupId = i.getStringExtra("groupId");
        memberId = i.getStringExtra("memberId");
        groupName = i.getStringExtra("groupName");

        inputValid = false;
        nameIsEmpty = true;

        name = (EditText) findViewById(R.id.instruction);
        start = (Button) findViewById(R.id.start);

        final com.vanniktech.emoji.EmojiEditText emojiEditText = (com.vanniktech.emoji.EmojiEditText) findViewById(R.id.emojiEditText);
        emojiEditText.setText(EmojiGetter.getEmoji(0x1F31A));
        emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(android.R.id.content)).build(emojiEditText);
        emojiPopup.dismiss();

        emojiEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                emojiEditText.setText("");
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                emojiPopup.toggle(); // Toggles visibility of the Popup.
                return false;
            }
        });

        name.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                emojiPopup.dismiss();
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                    .showSoftInput(name, InputMethodManager.SHOW_FORCED);
                return false;
            }
        });

        tw1 = new TextWatcher() {
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
                    tw2.afterTextChanged(emojiEditText.getText());
                }
                else {
                    nameIsEmpty = true;
                    start.setBackground(getDrawable(R.drawable.round_grey));
                }
            }
        };

        tw2 = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!s.toString().equals("") && !nameIsEmpty) {
                    inputValid = true;
                    start.setBackground(getDrawable(R.drawable.round));
                }
                else {
                    inputValid = false;
                    start.setBackground(getDrawable(R.drawable.round_grey));
                }
            }
        };

        name.addTextChangedListener(tw1);
        emojiEditText.addTextChangedListener(tw2);

    }

    @Override
    public void onBackPressed() {
        if (emojiPopup.isShowing()) {
            emojiPopup.dismiss();
            return;
        }
        else {
            super.onBackPressed();
        }
    }

    protected void join(View view) {
        view = findViewById(R.id.emojiEditText);
        view.setDrawingCacheEnabled(true);
        Bitmap b = view.getDrawingCache();
        try {
            b.compress(Bitmap.CompressFormat.JPEG, 95, new FileOutputStream(getApplicationContext().getFilesDir().toString() + "/mask.jpg"));
            Log.i("exported", getApplicationContext().getFilesDir().toString());
            Uri uri = Uri.fromFile(new File(getApplicationContext().getFilesDir().toString() + "/mask.jpg"));
            //upload the photo
            StorageReference storageReference =
                    FirebaseStorage.getInstance()
                            .getReference("chat_fakeProfileImages").child(new Random().toString());
            putImageInStorage(storageReference, uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("export", "failed");
        }

    }

    private void putImageInStorage(StorageReference storageReference, Uri uri) {
        storageReference.putFile(uri).addOnCompleteListener(createChat.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.i("status", "chat instantiation");
                            DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
                            final String chatId = mFirebaseDatabaseReference.child("chats").push().getKey();
                            String messageId = mFirebaseDatabaseReference.child("messages").child(chatId).push().getKey();
                            FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("chats").child("outgoing").child(chatId).setValue(new ChatUnit(chatId, groupId));
                            Message m = new Message("This is the very beginning of the conversation. Please be mindful of your word and happy chatting!\nFYI, " + name.getText().toString() + " has received 0 likes in total",
                                    "intro", FirebaseAuth.getInstance().getCurrentUser().getUid(), messageId, "outgoing", name.getText().toString(), task.getResult().getMetadata().getDownloadUrl().toString(), groupName);
                            mFirebaseDatabaseReference.child("messages").child(chatId).child(messageId).setValue(m);
                            messageId = mFirebaseDatabaseReference.child("messages").child(chatId).push().getKey();
                            m = new Message(name.getText().toString() + " initiated the chat.", "text", FirebaseAuth.getInstance().getCurrentUser().getUid(), messageId, "outgoing", name.getText().toString(), task.getResult().getMetadata().getDownloadUrl().toString(), groupName);
                            mFirebaseDatabaseReference.child("messages").child(chatId).child(messageId).setValue(m);
                            Chat c = new Chat(groupId, memberId, FirebaseAuth.getInstance().getCurrentUser().getUid(), task.getResult().getMetadata().getDownloadUrl().toString(), name.getText().toString(), m);
                            mFirebaseDatabaseReference.child("chats").child(chatId).setValue(c, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError,
                                                       DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        Toast.makeText(createChat.this, "Chat started successfully", Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(createChat.this, ChatRoom.class);
                                        i.putExtra("chatId", chatId);
                                        i.putExtra("isOutgoing", true);
                                        i.putExtra("justCreated", true);
                                        i.putExtra("groupName", groupName);
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
}
