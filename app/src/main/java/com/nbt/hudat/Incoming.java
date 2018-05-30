package com.nbt.hudat;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nbt.hudat.identity_classes.Chat;
import com.nbt.hudat.tools.IncomingChatRecylcerAdapter;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Incoming.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Incoming#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Incoming extends Fragment {
    private boolean isVisible;
    private RecyclerView ChatRecyclerView;
    private LinearLayoutManager incomingLinearLayoutManager;
    private ProgressBar mProgressBar;
    private IncomingChatRecylcerAdapter incomingChatAdapter;
    private DatabaseReference mFirebaseDatabaseReference;
    private ArrayList<Chat> chats;
    private ArrayList<String> chatId;

    // newInstance constructor for creating fragment with arguments
    public static Incoming newInstance(int page, String title) {
        Incoming outgoing = new Incoming();
        Bundle args = new Bundle();
        args.putInt("1", page);
        args.putString("Incoming", title);
        outgoing.setArguments(args);
        return outgoing;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incoming, container, false);
        return view;
    }

    public void onStart() {
        super.onStart();

        // Initialize ProgressBar and RecyclerView.
        ChatRecyclerView = (RecyclerView) getActivity().findViewById(R.id.chats_incoming);
        ChatRecyclerView.setHasFixedSize(true);
        mProgressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        incomingLinearLayoutManager = new LinearLayoutManager(getActivity());
        incomingLinearLayoutManager.setStackFromEnd(false);
        ChatRecyclerView.setLayoutManager(incomingLinearLayoutManager);
        chats = new ArrayList<Chat>();
        chatId = new ArrayList<String>();


        // New child entries; for real time update with database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("chats");
        Query q = mFirebaseDatabaseReference.orderByChild("incomingUserId").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        q.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mProgressBar.setVisibility(View.VISIBLE);
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    chats.add(ds.getValue(Chat.class));
                    chatId.add(ds.getKey());
                }
                incomingChatAdapter = new IncomingChatRecylcerAdapter(chatId, chats, getContext());
                ChatRecyclerView.setLayoutManager(incomingLinearLayoutManager);
                ChatRecyclerView.setAdapter(incomingChatAdapter);
                mProgressBar.setVisibility(View.GONE);
                //real time update codes end
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            isVisible = true;
            ChatRecyclerView.setLayoutManager(incomingLinearLayoutManager);
            ChatRecyclerView.setAdapter(incomingChatAdapter);
            mProgressBar.setVisibility(View.GONE);
        }
        else {
            isVisible = false;
        }
    }
}
