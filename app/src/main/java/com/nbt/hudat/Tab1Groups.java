package com.nbt.hudat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nbt.hudat.identity_classes.Group;
import com.nbt.hudat.tools.EmojiGetter;
import com.nbt.hudat.tools.GridSpacingItemDecoration;
import com.nbt.hudat.tools.RecyclerAdapter;

import java.util.ArrayList;

import me.anwarshahriar.calligrapher.Calligrapher;

/**
 * Created by user on 08-Aug-17.
 */

public class Tab1Groups extends Fragment{

    protected DatabaseReference mFirebaseDatabaseReference;
    protected LinearLayoutManager mLinearLayoutManager;
    protected RecyclerView mMessageRecyclerView;
    protected String uid;
    protected boolean isLeft;
    protected int order;
    protected ArrayList<Group> list;
    protected ArrayList<String> groupId;
    protected RecyclerAdapter recyclerAdapter;
    protected ValueEventListener vel;
    protected boolean resumedOnce;
    protected MainActivity m;
    protected boolean hasNoGroup;
    protected int x;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab1groups, container, false);
        resumedOnce = false;
        Calligrapher cl = new Calligrapher(getActivity());
        cl.setFont(getActivity(), "pnsb.ttf", true);
        return rootView;
    }

    public void onStart() {
        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser() == null)
            startActivity(new Intent(getActivity(), welcome.class));
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        // New child entries; for real time update with database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("groups");
        Query q = mFirebaseDatabaseReference.orderByChild("members/" + uid+"/userLocation").equalTo(uid);

        mMessageRecyclerView = (RecyclerView) getActivity().findViewById(R.id.groupRecyclerView);
        mMessageRecyclerView.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setStackFromEnd(false);
        mLinearLayoutManager.setReverseLayout(false);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        groupId = new ArrayList<String>();
        list = new ArrayList<Group>();
        LottieAnimationView animationView = (LottieAnimationView) getView().findViewById(R.id.animation_view);
        animationView.setAnimation("star.json");
        animationView.loop(true);
        animationView.playAnimation();

        m = (MainActivity) getActivity();
        if(!m.doneIntialLoadf) {
            ((ProgressBar) getActivity().findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
            vel = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(!dataSnapshot.exists()) {
                        LinearLayout LL = (LinearLayout) getActivity().findViewById(R.id.text);
                        LL.setVisibility(View.VISIBLE);
                        TextView emoji = (TextView) getActivity().findViewById(R.id.emoji);
                        emoji.setText((CharSequence) ("" + EmojiGetter.getEmoji(0x1F605)));
                        mMessageRecyclerView.setVisibility(View.GONE);
                        ((ProgressBar) getActivity().findViewById(R.id.progressBar)).setVisibility(View.INVISIBLE);
                        m.doneIntialLoadf = true;
                        hasNoGroup = true;
                        return;
                    }
                    hasNoGroup = false;
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        groupId.add(ds.getKey());
                        Group g = ds.getValue(Group.class);
                        list.add(g);
                    }
                    m.initialLoadNumGroup = list.size();
                    recyclerAdapter = new RecyclerAdapter(groupId, list, getActivity());
                    RecyclerView.LayoutManager recyce = new GridLayoutManager(getActivity(), 2);
                    /// RecyclerView.LayoutManager recyce = new LinearLayoutManager(MainActivity.this);
                    mMessageRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 30, false));
                    //Toast.makeText(getActivity(), "summoned " + mMessageRecyclerView.getChildCount(), Toast.LENGTH_SHORT).show();
                    mMessageRecyclerView.setLayoutManager(recyce);
                    mMessageRecyclerView.setItemAnimator(new DefaultItemAnimator());
                    mMessageRecyclerView.setAdapter(recyclerAdapter);
                    ((ProgressBar) getActivity().findViewById(R.id.progressBar)).setVisibility(View.INVISIBLE);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            q.addListenerForSingleValueEvent(vel);
            m.doneIntialLoadf = true;
        }
        else {
            if(!hasNoGroup) {
                RecyclerView.LayoutManager recyce = new GridLayoutManager(getActivity(), 2);
                if(!resumedOnce) {
                    mMessageRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 30, false));
                    resumedOnce = true;
                }
                mMessageRecyclerView.setLayoutManager(recyce);
                mMessageRecyclerView.setItemAnimator(new DefaultItemAnimator());
                mMessageRecyclerView.setAdapter(recyclerAdapter);
                mMessageRecyclerView.setLayoutManager(recyce);
            }
            else {
                mMessageRecyclerView.setVisibility(View.GONE);
                LinearLayout LL = (LinearLayout) getActivity().findViewById(R.id.text);
                LL.setVisibility(View.VISIBLE);
                TextView emoji = (TextView) getActivity().findViewById(R.id.emoji);
                emoji.setText((CharSequence) ("" + EmojiGetter.getEmoji(0x1F605)));
            }
        }
    }

    public void create(View view) {
        startActivity(new Intent(getActivity(), createGroup.class));
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i("Tab1", "is destroyed");
    }
}
