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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nbt.hudat.identity_classes.Chat;
import com.nbt.hudat.tools.ChatRecyclerAdapter;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Outgoing.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Outgoing#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Outgoing extends Fragment {
    private boolean isVisible;
    private RecyclerView ChatRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private ChatRecyclerAdapter ChatAdapter;
    private DatabaseReference mFirebaseDatabaseReference;
    private ArrayList<Chat> chats;
    private ArrayList<String> chatId;

    // newInstance constructor for creating fragment with arguments
    public static Outgoing newInstance(int page, String title) {
        Outgoing outgoing = new Outgoing();
        Bundle args = new Bundle();
        args.putInt("1", page);
        args.putString("Outgoing", title);
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
        View view = inflater.inflate(R.layout.fragment_outgoing, container, false);
        Log.i("onCreateView", "is called");
        return view;
    }

    public void onStart() {
        super.onStart();
        Log.i("onStart", "is called");
        // Initialize ProgressBar and RecyclerView

        ChatRecyclerView = (RecyclerView) getActivity().findViewById(R.id.chats);
        mProgressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLinearLayoutManager.setStackFromEnd(false);
        ChatRecyclerView.setLayoutManager(mLinearLayoutManager);
        if(((Tab2Chats) getParentFragment()).ChatAdapter != null)
            ChatRecyclerView.setAdapter(((Tab2Chats) getParentFragment()).ChatAdapter);
        chats = new ArrayList<Chat>();
        chatId = new ArrayList<String>();


        // New child entries; for real time update with database
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("chats");
        Query q = mFirebaseDatabaseReference.orderByChild("outgoingUserId").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Chat c = ds.getValue(Chat.class);
                    chats.add(c);
                    chatId.add(ds.getKey());
                }
                ChatAdapter = new ChatRecyclerAdapter(chatId, chats, getContext());
                if(((Tab2Chats) getParentFragment()).ChatAdapter == null) {
                    ((Tab2Chats) getParentFragment()).ChatAdapter = ChatAdapter;
                    ChatRecyclerView.setAdapter(ChatAdapter);
                }
                else if(!ChatAdapter.equals(((Tab2Chats) getParentFragment()).ChatAdapter)) {
                    ChatRecyclerView.setAdapter(ChatAdapter);
                }
                mProgressBar.setVisibility(View.GONE);
                //real time update codes end
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onResume() {
        super.onResume();
        Log.i("onCreateView", "is called");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.i("visible Hint is called", "");
        if(ChatAdapter != null) {
            ChatRecyclerView.setLayoutManager(mLinearLayoutManager);
            ChatRecyclerView.setAdapter(ChatAdapter);
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
