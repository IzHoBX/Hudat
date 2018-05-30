package com.nbt.hudat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nbt.hudat.identity_classes.Group;
import com.nbt.hudat.identity_classes.userLocation;
import com.nbt.hudat.tools.RecyclerAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Discover extends AppCompatActivity {
    android.widget.SearchView search;
    protected RecyclerAdapter recyclerAdapter;
    protected LinearLayoutManager mLinearLayoutManager;
    protected RecyclerView ResultRecyclerView;
    protected ArrayList<String> groupIds;
    protected ArrayList<Group> results;
    protected DatabaseReference dr;
    protected Query q;
    protected ProgressBar pb;

    protected PopupWindow pw;
    protected LayoutInflater li;
    protected ConstraintLayout cl;
    protected de.hdodenhof.circleimageview.CircleImageView cv;
    protected TextView name;
    protected TextView description;
    private String groupId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discover);


        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("pnsb.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        pb = (ProgressBar) findViewById(R.id.progressBar);
        search = (android.widget.SearchView) findViewById(R.id.search);
        ResultRecyclerView = (RecyclerView) findViewById(R.id.results);
        ResultRecyclerView.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(false);
        mLinearLayoutManager.setReverseLayout(false);
        ResultRecyclerView.setLayoutManager(mLinearLayoutManager);
        dr = FirebaseDatabase.getInstance().getReference().child("groups");



        search.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
           @Override
           public boolean onQueryTextSubmit(String query) {
               return false;
           }

           @Override
           public boolean onQueryTextChange(final String newText) {
               if(newText.equals(""))
                   return false;
               pb.setVisibility(View.VISIBLE);
               q = dr.orderByChild("name").startAt(newText).endAt(newText + "\uf8ff");
               q.addListenerForSingleValueEvent(new ValueEventListener() {
                   @Override
                   public void onDataChange(DataSnapshot dataSnapshot) {
                       groupIds = new ArrayList<String>();
                       results = new ArrayList<Group>();
                       for(DataSnapshot ds: dataSnapshot.getChildren()) {
                           groupIds.add(ds.getKey());
                           Group g = ds.getValue(Group.class);
                           if(g.containsMember(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                               g.members = new HashMap<String, userLocation>();
                           results.add(g);
                       }
                       recyclerAdapter = new RecyclerAdapter(groupIds, results, Discover.this);
                       RecyclerView.LayoutManager recyce = new GridLayoutManager(Discover.this, 2);
                       /// RecyclerView.LayoutManager recyce = new LinearLayoutManager(MainActivity.this);
                       //ResultRecyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 30, false));
                       //Toast.makeText(getActivity(), "summoned " + mMessageRecyclerView.getChildCount(), Toast.LENGTH_SHORT).show();
                       ResultRecyclerView.setLayoutManager(recyce);
                       ResultRecyclerView.setItemAnimator(new DefaultItemAnimator());
                       ResultRecyclerView.setAdapter(recyclerAdapter);
                       pb.setVisibility(View.GONE);
                   }

                   @Override
                   public void onCancelled(DatabaseError databaseError) {

                   }
               });
              return true;
           }
       });
    }

    protected void selectGroup(View view) {
        TextView numMemberL = (TextView) view.findViewById(R.id.numMemberL);
        if(numMemberL.getText().toString().equals("You're in!")) {
            Intent i = new Intent(this, GroupDetails.class);
            i.putExtra("groupId", (String) view.getTag());
            startActivity(i);
            return;
        }

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        cl = (ConstraintLayout) findViewById(R.id.cl);
        li = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View container = li.inflate(R.layout.group_pop, null);

        Button join = (Button) container.findViewById(R.id.join);
        cv = (de.hdodenhof.circleimageview.CircleImageView) container.findViewById(R.id.image);
        name = (TextView) container.findViewById(R.id.instruction);
        description = (TextView) container.findViewById(R.id.description);

        groupId = (String) view.getTag();

        DatabaseReference dr1 = FirebaseDatabase.getInstance().getReference().child("groups").child(groupId);
        dr1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group g = dataSnapshot.getValue(Group.class);
                Log.i("url", g.getGroupImageUrl());
                Glide.with(cv.getContext())
                        .load(g.getGroupImageUrl())
                        .into(cv);
                name.setText(g.getName());
                description.setText(g.getDescription());
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
               Intent i = new Intent(getApplicationContext(), Join.class);
               i.putExtra("groupId", groupId);
               startActivityForResult(i, 1);
           }
       });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
               finish();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult
}
