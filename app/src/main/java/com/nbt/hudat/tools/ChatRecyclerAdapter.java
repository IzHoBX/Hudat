package com.nbt.hudat.tools;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nbt.hudat.R;
import com.nbt.hudat.identity_classes.Chat;
import com.nbt.hudat.identity_classes.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by user on 19-Aug-17.
 */

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ChatViewHolder>{

    List<Chat> list;
    Context context;
    List<String> chatId;

    public ChatRecyclerAdapter(List<String> chatId, List<Chat> list, Context context) {
        this.list = list;
        this.context = context;
        this.chatId = chatId;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_chat,parent,false);
        ChatViewHolder cvh = new ChatViewHolder(view);
        return cvh;
    }

    @Override
    public void onBindViewHolder(final ChatViewHolder holder, int position) {
        Chat c = list.get(position);
        if(c.isChatDeleted == null)
            Log.i("chat", "is cull");
        DatabaseReference dr0 = FirebaseDatabase.getInstance().getReference().child("groups").child(c.group).child("name");
        Log.i("groupId", c.group);
        dr0.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.group.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Query dr1 = FirebaseDatabase.getInstance().getReference().child("users").orderByKey().equalTo(c.incomingUserId);
        dr1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User x = dataSnapshot.getValue(User.class);
                holder.name.setText(x.name);
                Glide.with(holder.image.getContext())
                        .load(x.profileImageUrl)
                        .into(holder.image);

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
        holder.text.setText(c.lastMessage);
        holder.time.setText(new SimpleDateFormat("hh:mm aa").format(c.lastMessageDate*1000));
        holder.canvas.setTag(chatId.get(position));
    }

    @Override
    public int getItemCount() {

        int arr = 0;

        try{
            if(list.size()==0){

                arr = 0;

            }
            else{

                arr=list.size();
            }



        }catch (Exception e){



        }

        return arr;

    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView group;
        TextView text;
        CircleImageView image;
        TextView time;
        LinearLayout canvas;

        public ChatViewHolder(View v) {
            super(v);
            name = (TextView) itemView.findViewById(R.id.instruction);
            group = (TextView) itemView.findViewById(R.id.group);
            image = (CircleImageView) itemView.findViewById(R.id.image);
            text = (TextView) itemView.findViewById(R.id.text);
            time = (TextView) itemView.findViewById(R.id.time);
            canvas = (LinearLayout) itemView.findViewById(R.id.canvas);
        }
    }
}
