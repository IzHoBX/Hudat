package com.nbt.hudat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.nbt.hudat.identity_classes.User;
import com.nbt.hudat.tools.EmojiGetter;

/**
 * Created by user on 08-Aug-17.
 */

public class Tab3Profile extends Fragment {
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab3profile, container, false);
        return rootView;
    }

    public void onStart() {
        super.onStart();
        TextView likes = (TextView) getView().findViewById(R.id.likes);
        TextView rate = (TextView) getView().findViewById(R.id.rate);
        TextView contact = (TextView) getView().findViewById(R.id.contact);
        TextView log = (TextView) getView().findViewById(R.id.log);

        Log.i("detected uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

        FirebaseDatabase.getInstance().getReference().child("users").orderByKey().equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                user = dataSnapshot.getValue(User.class);
                ((TextView) getView().findViewById(R.id.instruction)).setText(user.name);
                ((TextView) getView().findViewById(R.id.description)).setText(user.intro);
                de.hdodenhof.circleimageview.CircleImageView image = (de.hdodenhof.circleimageview.CircleImageView) getView().findViewById(R.id.image);
                Glide.with(getContext())
                        .load(user.profileImageUrl)
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
        likes.setText(EmojiGetter.getEmoji(0x2764) + " likes");
        rate.setText(rate.getText().toString() + " " + EmojiGetter.getEmoji(0x1f44d));
        contact.setText(contact.getText().toString() + " " + EmojiGetter.getEmoji(0x2709));
        log.setText(log.getText().toString() + " " + EmojiGetter.getEmoji(0x2620));
    }
}
