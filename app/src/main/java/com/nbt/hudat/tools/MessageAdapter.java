package com.nbt.hudat.tools;

import android.content.Context;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nbt.hudat.R;
import com.nbt.hudat.identity_classes.Chat;
import com.nbt.hudat.identity_classes.Group;
import com.nbt.hudat.identity_classes.Message;
import com.nbt.hudat.identity_classes.User;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.attr.data;
import static android.R.attr.type;
import static android.R.id.list;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by user on 23-Aug-17.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public List<Message> messages;
    public Context context;
    public Chat chat;
    public String incomingPhoto;
    public boolean outgoingFromMe;

    public MessageAdapter(Chat chat, Context context, Boolean outgoingFromMe) {
        this.messages = new ArrayList<>();
        this.context = context;
        this.chat = chat;
        FirebaseDatabase.getInstance().getReference().child("users").child(chat.incomingUserId).child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                incomingPhoto = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        this.outgoingFromMe = outgoingFromMe;
    }

    //0 = welcome; 1 = normal; 2= date; 3 = reveal
    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).contentType != null && messages.get(position).contentType.equals("intro")) {
            return 0;
        }
        else if(messages.get(position).messageType.equals("date")) {
            return 2;
        }
        else if(messages.get(position).messageType.equals("reveal")) {
            return 3;
        }
        else {
            return 1;
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch(viewType) {
            case 0:
                View view = LayoutInflater.from(context).inflate(R.layout.item_welcome,parent,false);
                WelcomeViewHolder wvh = new WelcomeViewHolder(view);
                return wvh;
            case 1:
                View view1 = LayoutInflater.from(context).inflate(R.layout.item_message,parent,false);
                MessageViewHolder mvh = new MessageViewHolder(view1);
                return mvh;
            case 2:
                View view2 = LayoutInflater.from(context).inflate(R.layout.item_date,parent,false);
                DateViewHolder dvh = new DateViewHolder(view2);
                return dvh;
            case 3:
                View view3 = LayoutInflater.from(context).inflate(R.layout.item_reveal,parent,false);
                RevealViewHolder rvh = new RevealViewHolder(view3);
                return rvh;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch(holder.getItemViewType()) {
            case 0:
                final DecimalFormat df2 = new DecimalFormat(".#");
                final WelcomeViewHolder wvh = (WelcomeViewHolder) holder;
                String outgoingUserId = chat.outgoingUserId;
                FirebaseDatabase.getInstance().getReference().child("users").child(outgoingUserId).child("rating").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int total = 0;
                        for(DataSnapshot ds: dataSnapshot.getChildren()) {
                            total += ds.getValue(Integer.class);
                        }
                        double avg = ((double) total)/dataSnapshot.getChildrenCount();
                        wvh.rating.setText(chat.outgoingUsername + "'s rating: " + df2.format(avg));
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                return;
            case 1:
                final MessageViewHolder mvh = (MessageViewHolder) holder;
                if(messages.get(position).messageType.equals("outgoing")) {
                    mvh.name.setText(chat.outgoingUsername);
                    if(outgoingFromMe) {
                        if(!chat.isIdRevealed) {
                            //Toast.makeText(context, "detected outgoing from me and setting up mask", Toast.LENGTH_SHORT).show();
                            Glide.with(context)
                                    .load(chat.outgoingUserProfileImageUrl)
                                    .into(mvh.mask);
                            mvh.mask.setVisibility(View.VISIBLE);
                        }
                        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Glide.with(context)
                                        .load(dataSnapshot.getValue(String.class))
                                        .into(mvh.photo);
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    else
                    Glide.with(context)
                            .load(chat.outgoingUserProfileImageUrl)
                            .into(mvh.photo);
                }
                else
                    FirebaseDatabase.getInstance().getReference().child("users").orderByKey().equalTo(chat.incomingUserId).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            User u = dataSnapshot.getValue(User.class);
                            Glide.with(context)
                                    .load(u.profileImageUrl)
                                    .into(mvh.photo);
                            mvh.name.setText(u.name);
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
                if(messages.get(position).contentType.equals("text")) {
                    mvh.text.setText(messages.get(position).content);
                    mvh.text.setVisibility(View.VISIBLE);
                    mvh.image.setVisibility(View.GONE);
                }
                else if(messages.get(position).contentType.equals("image")) {
                    mvh.text.setVisibility(View.GONE);
                    Glide.with(context)
                            .load(messages.get(position).content)
                            .into(mvh.image);
                }
                mvh.time.setText(new SimpleDateFormat("hh:mm aa").format(chat.lastMessageDate*1000));
                return;
            case 2:
                final DateViewHolder dvh = (DateViewHolder) holder;
                dvh.date.setText(new SimpleDateFormat("dd MMM YY").format(messages.get(position).sentDay*1000));
                return;
            case 3:
                final RevealViewHolder rvh = (RevealViewHolder) holder;
                rvh.instruction.setText(chat.outgoingUsername + rvh.instruction.getText().toString());
                Glide.with(context)
                        .load(chat.outgoingUserProfileImageUrl)
                        .into(rvh.mask);
                FirebaseDatabase.getInstance().getReference().child("users").orderByKey().equalTo(chat.outgoingUserId).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        User u = dataSnapshot.getValue(User.class);
                        rvh.name.setText(rvh.name.getText() + u.name + "!");
                        Glide.with(context)
                                .load(u.profileImageUrl)
                                .into(rvh.photo);
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
                return;
        }
    }

    @Override
    public int getItemCount() {

        int arr = 0;

        try{
            if(messages.size()==0){

                arr = 0;

            }
            else{

                arr=messages.size();
            }



        }catch (Exception e){



        }

        return arr;

    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image;
        TextView text;
        CircleImageView photo;
        CircleImageView mask;
        TextView time;

        public MessageViewHolder(View v) {
            super(v);
            name = (TextView) itemView.findViewById(R.id.messengerTextView);
            image = (ImageView) itemView.findViewById(R.id.messageImageView);
            photo = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
            mask = (CircleImageView) itemView.findViewById(R.id.mask);
            text = (TextView) itemView.findViewById(R.id.messageTextView);
            time = (TextView) itemView.findViewById(R.id.time);
        }
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView date;

        public DateViewHolder(View v) {
            super(v);
            date = (TextView) itemView.findViewById(R.id.Date);
        }
    }

    public static class RevealViewHolder extends RecyclerView.ViewHolder {
        TextView instruction;
        TextView name;
        de.hdodenhof.circleimageview.CircleImageView mask;
        de.hdodenhof.circleimageview.CircleImageView photo;


        public RevealViewHolder(View v) {
            super(v);
            instruction = (TextView) itemView.findViewById(R.id.instruction);
            name = (TextView) itemView.findViewById(R.id.name);
            mask = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.mask);
            photo = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.photo);
        }
    }

    public static class WelcomeViewHolder extends RecyclerView.ViewHolder {
        TextView rating;

        public WelcomeViewHolder(View v) {
            super(v);
            rating = (TextView) itemView.findViewById(R.id.rating);
        }
    }
}
