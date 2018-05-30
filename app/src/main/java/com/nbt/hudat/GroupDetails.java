package com.nbt.hudat;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nbt.hudat.identity_classes.Group;
import com.nbt.hudat.identity_classes.User;
import com.nbt.hudat.identity_classes.userLocation;

import java.util.ArrayList;
import java.util.Set;

import me.anwarshahriar.calligrapher.Calligrapher;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GroupDetails extends AppCompatActivity {
    public static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView description;
        de.hdodenhof.circleimageview.CircleImageView image;
        TextView arrow;
        TextView admin;
        LinearLayout canvas;


        public MemberViewHolder(View v) {
            super(v);
            name = (TextView) itemView.findViewById(R.id.instruction);
            description = (TextView) itemView.findViewById(R.id.description);
            image = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.image);
            arrow = (TextView) itemView.findViewById(R.id.arrow);
            admin = (TextView) itemView.findViewById(R.id.Admin);
            canvas = (LinearLayout) itemView.findViewById(R.id.canvas);
        }
    }

    protected DatabaseReference dr;
    protected Group g;

    protected TextView name;
    protected TextView description;
    protected TextView numMem;
    protected de.hdodenhof.circleimageview.CircleImageView groupImage;
    protected RecyclerView members;
    protected LinearLayoutManager mLinearLayoutManager;
    protected FirebaseRecyclerAdapter<userLocation, MemberViewHolder> mFirebaseAdapter;
    protected DatabaseReference mFirebaseReference;
    protected String groupId;
    protected String passcode;
    protected LayoutInflater li;
    protected de.hdodenhof.circleimageview.CircleImageView cv;
    protected PopupWindow pw;
    protected ConstraintLayout cl;
    protected String selectedMemberId;
    protected TextView title;
    protected boolean isAdmin;
    protected ArrayList<String> nonAdmins;
    protected String uid;
    protected Menu menu;
    protected User u;
    protected ArrayList<String> chatsToDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("pnsb.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        //font customisation
        Calligrapher cl = new Calligrapher(this);
        cl.setFont(this, "pnsb.ttf", true);

        Intent i = getIntent();
        groupId = i.getStringExtra("groupId");
        Log.i("groupId", groupId);

        dr = FirebaseDatabase.getInstance().getReference().child("groups").child(groupId);

        description = (TextView) findViewById(R.id.description);
        title = (TextView) findViewById(R.id.toolbar_title);
        groupImage = (de.hdodenhof.circleimageview.CircleImageView) findViewById(R.id.image);
        numMem = (TextView) findViewById(R.id.numMem);
        members = (RecyclerView) findViewById(R.id.members);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);


        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("detected", "onDataChanged");
                g = dataSnapshot.getValue(Group.class);
                title.setText(g.name);
                description.setText(g.getDescription());
                Glide.with(groupImage.getContext())
                        .load(g.getGroupImageUrl())
                        .into(groupImage);
                passcode = g.getPasscode();
                int x = g.getMembers().size();
                if (x == 1)
                    numMem.setText("1 member");
                else
                    numMem.setText(x + " members");
                isAdmin = g.admins.containsKey(uid);
                if(!isAdmin) {
                    hideOption(R.id.edit);
                    hideOption(R.id.add);
                    Log.i("hid", "irrelevant options");
                }
                Set<String> admins = g.admins.keySet();
                Set<String> members = g.members.keySet();
                members.removeAll(admins);
                nonAdmins = new ArrayList<String>(members);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        members.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mLinearLayoutManager.setReverseLayout(true);
        members.setLayoutManager(mLinearLayoutManager);
        mFirebaseReference = FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).child("members");

        //populate List View
        mFirebaseAdapter = new FirebaseRecyclerAdapter<userLocation,
                MemberViewHolder>(
                userLocation.class,
                R.layout.item_member,
                MemberViewHolder.class,
                mFirebaseReference) {
            @Override
            protected void populateViewHolder(final MemberViewHolder viewHolder,
                                              userLocation ul, int position) {
                //mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                final String memberuid = ul.userLocation;
                DatabaseReference dref = FirebaseDatabase.getInstance().getReference().child("users").child(memberuid).child("name");
                dref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i("name", dataSnapshot.getValue(String.class));
                        viewHolder.name.setText(dataSnapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                dref = FirebaseDatabase.getInstance().getReference().child("users").child(memberuid).child("intro");
                dref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.i("description", dataSnapshot.getValue(String.class));
                        viewHolder.description.setText(dataSnapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                dref = FirebaseDatabase.getInstance().getReference().child("users").child(memberuid).child("photoUrl");
                dref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Glide.with(viewHolder.image.getContext())
                                .load(dataSnapshot.getValue(String.class))
                                .into(viewHolder.image);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                if (memberuid.equals(uid)) {
                    viewHolder.arrow.setText("");
                }
                dref = FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).child("admins").child(ul.userLocation);
                dref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists())
                            viewHolder.admin.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                Query q = FirebaseDatabase.getInstance().getReference().child("chats").orderByChild("outgoingUserId").equalTo(uid);
                q.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            if (ds.child("group").getValue(String.class).equals(groupId) && ds.child("incomingUserId").getValue(String.class).equals(memberuid))
                                viewHolder.arrow.setText("C");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                viewHolder.canvas.setTag(memberuid);
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
                    members.scrollToPosition(positionStart);
                }
            }
        });

        members.setLayoutManager(mLinearLayoutManager);
        members.setAdapter(mFirebaseAdapter);
    }

    protected void invite(View view) {
        Intent i = new Intent(GroupDetails.this, invite.class);
        i.putExtra("name", name.getText().toString());
        i.putExtra("passcode", passcode);
        i.putExtra("justCreated", false);
        startActivity(i);
    }

    @Override
    public void finish() {
        Intent returnIntent = new Intent();
        setResult(RESULT_OK);
        super.finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected void selectMember(View view) {
        selectedMemberId = (String) view.getTag();
        if (!((TextView) view.findViewById(R.id.arrow)).getText().toString().equals("C") && !((TextView) view.findViewById(R.id.arrow)).getText().toString().equals("")) {

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            cl = (ConstraintLayout) findViewById(R.id.cl);
            li = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            View container = li.inflate(R.layout.member_pop, null);

            Button join = (Button) container.findViewById(R.id.join);
            cv = (de.hdodenhof.circleimageview.CircleImageView) container.findViewById(R.id.image);
            name = (TextView) container.findViewById(R.id.instruction);
            description = (TextView) container.findViewById(R.id.description);

            selectedMemberId = (String) view.getTag();

            DatabaseReference dr1 = FirebaseDatabase.getInstance().getReference().child("users").child(selectedMemberId);
            dr1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Glide.with(cv.getContext())
                            .load(dataSnapshot.child("photoUrl").getValue(String.class))
                            .into(cv);
                    name.setText(dataSnapshot.child("name").getValue(String.class));
                    description.setText(dataSnapshot.child("intro").getValue(String.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            pw = new PopupWindow(container, 900, 1000, true);
            pw.showAtLocation(cl, Gravity.CENTER, 0, 0);

            join.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(), createChat.class);
                    i.putExtra("groupId", groupId);
                    i.putExtra("memberId", selectedMemberId);
                    i.putExtra("groupName", name.getText());
                    startActivity(i);
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_details_menu_admin, menu);
        this.menu = menu;
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.leave:
                //update Group
                FirebaseDatabase.getInstance().getReference().child("groups").child(groupId).child("members").child(uid).setValue(null);

                //update users-groups
                FirebaseDatabase.getInstance().getReference().child("users").child(uid).child(groupId).setValue(null);

                //update incoming chats
                FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("chats").child("incoming").orderByChild("groupLocation").equalTo(groupId).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        FirebaseDatabase.getInstance().getReference().child("chats").child(dataSnapshot.getKey()).child("isChatDeleted").setValue(true);
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("incoming").child(dataSnapshot.getKey()).setValue(null);
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

                //update outgoing chats
                FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("chats").child("outgoing").orderByChild("groupLocation").equalTo(groupId).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        FirebaseDatabase.getInstance().getReference().child("chats").child(dataSnapshot.getKey()).child("isChatDeleted").setValue(true);
                        FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("outgoing").child(dataSnapshot.getKey()).setValue(null);
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
                startActivity(new Intent(this, MainActivity.class));
            case R.id.edit:
                Intent i = new Intent(getApplicationContext(), EditGroup.class);
                i.putExtra("groupId", groupId);
                startActivity(i);
        }

        return true;
    }

    private void hideOption(int id)
    {
        MenuItem item = menu.findItem(id);
        item.setVisible(false);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }

}
