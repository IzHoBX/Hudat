package com.nbt.hudat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.media.Rating;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nbt.hudat.identity_classes.Chat;
import com.nbt.hudat.identity_classes.FriendlyMessage;
import com.nbt.hudat.identity_classes.Message;
import com.nbt.hudat.identity_classes.User;
import com.nbt.hudat.tools.ChatRecyclerAdapter;
import com.nbt.hudat.tools.CodelabPreferences;
import com.nbt.hudat.tools.MessageAdapter;

import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.rockerhieu.emojiconize.Emojiconize;
import me.anwarshahriar.calligrapher.Calligrapher;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.R.attr.key;

public class ChatRoom extends AppCompatActivity {
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView messageImageView;
        TextView messengerTextView;
        CircleImageView messengerImageView;
        CircleImageView mask;
        TextView time;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
            mask = (CircleImageView) itemView.findViewById(R.id.mask);
            time = (TextView) itemView.findViewById(R.id.time);
        }
    }

    private static final String TAG = "ChatRoomActivity";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    private static final int REQUEST_IMAGE = 2;
    private static final String LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;

    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;

    private String chatId;
    private boolean outgoingFromMe;
    private String peerName;
    private String myProfileImageUrl;
    private String peerProfileImageUrl;
    private String myName;
    private String myMask;
    private Chat c;
    private User me;
    private Boolean isChatDeleted;
    private Boolean isIdRevealed;
    private Boolean isUserRated;
    private Menu menu;
    protected PopupWindow pw;
    protected LayoutInflater li;
    protected RelativeLayout rl;
    protected boolean justCreated;
    protected String outgoingUserId;
    protected String peerId;
    protected String groupId;
    protected String groupName;
    protected MessageAdapter ma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Emojiconize.activity(this).go();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        //font customisation
        Calligrapher cl = new Calligrapher(this);
        cl.setFont(this, "pnsb.ttf", true);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("pnsb.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);


        Intent i = getIntent();
        chatId = i.getStringExtra("chatId");
        outgoingFromMe = i.getBooleanExtra("isOutgoing", false);
        groupName = i.getStringExtra("groupName");
        justCreated = i.getBooleanExtra("justCreated", false);

        Query q = FirebaseDatabase.getInstance().getReference().child("chats").orderByKey().equalTo(chatId);
        Log.i("chatId", chatId);
        q.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                c = dataSnapshot.getValue(Chat.class);
                if(outgoingFromMe) {
                    Query dr = FirebaseDatabase.getInstance().getReference().child("users").orderByKey().equalTo(c.incomingUserId);
                    dr.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            User u = dataSnapshot.getValue(User.class);
                            peerName = u.name;
                            peerProfileImageUrl = u.profileImageUrl;
                            ((TextView) findViewById(R.id.toolbar_title)).setText(peerName);
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
                else {
                    peerName = c.outgoingUsername;
                    peerProfileImageUrl = c.outgoingUserProfileImageUrl;
                    ((TextView) findViewById(R.id.toolbar_title)).setText(peerName);
                }
                if(outgoingFromMe) {
                    myName = c.outgoingUsername;
                    myMask = c.outgoingUserProfileImageUrl;
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
        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                me = dataSnapshot.getValue(User.class);
                if(!outgoingFromMe) {
                    myName = me.name;
                    myMask = null;
                }
                myProfileImageUrl = me.profileImageUrl;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.rv);
        if(mMessageRecyclerView == null)
            Log.i("recycler view", "cull");
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mLinearLayoutManager.setReverseLayout(false);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        // New child entries; for real time update with database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("messages").child(chatId);
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Message,
                MessageViewHolder>(
                Message.class,
                R.layout.item_message,
                MessageViewHolder.class,
                mFirebaseDatabaseReference) {

            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder,
                                              Message m, int position) {
                if(m.contentType.equals("image")){
                    String imageUrl = m.content;
                    if (imageUrl != null && imageUrl.startsWith("gs://")) {
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(imageUrl);
                        storageReference.getDownloadUrl().addOnCompleteListener(
                                new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String downloadUrl = task.getResult().toString();
                                            Glide.with(viewHolder.messageImageView.getContext())
                                                    .load(downloadUrl)
                                                    .into(viewHolder.messageImageView);
                                        } else {
                                            Log.w(TAG, "Getting download url was not successful.",
                                                    task.getException());
                                        }
                                    }
                                });
                    } else {
                        Glide.with(viewHolder.messageImageView.getContext())
                                .load(m.content)
                                .into(viewHolder.messageImageView);
                    }
                    viewHolder.messageImageView.setVisibility(ImageView.VISIBLE);
                    viewHolder.messageTextView.setVisibility(TextView.GONE);
                } else {
                    viewHolder.messageTextView.setText(m.content);
                    viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
                    viewHolder.messageImageView.setVisibility(ImageView.GONE);
                }

                if(m.contentType.equals("intro")) {
                    viewHolder.messengerTextView.setVisibility(View.GONE);
                    viewHolder.messengerImageView.setVisibility(View.GONE);
                    viewHolder.mask.setVisibility(View.GONE);
                    viewHolder.time.setVisibility(View.GONE);
                    return;
                }
                else if(m.messageType.equals("outgoing") && outgoingFromMe) {
                    viewHolder.messengerTextView.setText(myName);
                    Glide.with(ChatRoom.this)
                            .load(myProfileImageUrl)
                            .into(viewHolder.messengerImageView);
                    Glide.with(ChatRoom.this)
                            .load(myMask)
                            .into(viewHolder.mask);
                    viewHolder.mask.setVisibility(View.VISIBLE);
                }
                else if(m.messageType.equals("incoming") && !outgoingFromMe) {
                    viewHolder.messengerTextView.setText(myName);
                    Glide.with(ChatRoom.this)
                            .load(myProfileImageUrl)
                            .into(viewHolder.messengerImageView);
                }
                else {
                    viewHolder.messengerTextView.setText(peerName);
                    Glide.with(ChatRoom.this)
                            .load(peerProfileImageUrl)
                            .into(viewHolder.messengerImageView);
                }
                viewHolder.time.setText(new SimpleDateFormat("dd/MM/yyyy hh:mm aa").format(m.sentDate*1000));
                mProgressBar.setVisibility(View.GONE);
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, final int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mMessageRecyclerView.scrollToPosition(mLinearLayoutManager.findLastVisibleItemPosition());
                if ( bottom < oldBottom) {
                    mMessageRecyclerView.smoothScrollToPosition(bottom);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        //real time update codes end


        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);
        mAddMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Select image for image message on click.
            }
        });

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

        //implementing message sending
        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("messages").child(chatId);
                String key = mFirebaseDatabaseReference.push().getKey();
                String type;
                if(outgoingFromMe)
                    type = "outgoing";
                else
                    type = "incoming";
                Message friendlyMessage = new Message(mMessageEditText.getText().toString(), "text", FirebaseAuth.getInstance().getCurrentUser().getUid(), key, type, myName, myProfileImageUrl, groupName);
                mFirebaseDatabaseReference.child(key).setValue(friendlyMessage);
                mMessageEditText.setText("");
            }
        });

        //to implement photo sending
        //to invoke photo selecting
        mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);
        mAddMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });

        FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("isChatDeleted").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("chats").child(chatId).child("isIdRevealed").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.toString());
                    StorageReference storageReference = FirebaseStorage.getInstance()
                            .getReference("chat_messageImages").child(new Random().toString());
                    putImageInStorage(storageReference, uri);
                }
                else {
                    Log.w(TAG, "Unable to write message to database.");
                }

            }
        }
    }

    private void putImageInStorage(StorageReference storageReference, Uri uri) {
        storageReference.putFile(uri).addOnCompleteListener(ChatRoom.this,
                new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("messages").child(chatId);
                            String key = mFirebaseDatabaseReference.push().getKey();
                            String type;
                            if(outgoingFromMe)
                                type = "outgoing";
                            else
                                type = "incoming";
                            Message m = new Message(task.getResult().getMetadata().getDownloadUrl().toString(), "image", FirebaseAuth.getInstance().getCurrentUser().getUid(), key, type, myName, myProfileImageUrl, groupName);
                            mFirebaseDatabaseReference.child(key).setValue(m);
                        } else {
                            Log.w(TAG, "Image upload task was not successful.",
                                    task.getException());
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_room_menu, menu);
        this.menu = menu;
        if(!outgoingFromMe)
            hideOption(R.id.reveal);
        else
            hideOption(R.id.rate);
        return true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        if(justCreated)
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        else
            super.onBackPressed();
    }

    private void hideOption(int id)
    {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    private void showOption(int id)
    {
        MenuItem item = menu.findItem(id);
        item.setVisible(true);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.rate:
            case R.id.reveal:
        }
        return true;

    }
}

