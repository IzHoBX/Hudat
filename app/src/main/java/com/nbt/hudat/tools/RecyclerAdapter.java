package com.nbt.hudat.tools;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nbt.hudat.R;
import com.nbt.hudat.identity_classes.Group;

import java.util.List;

/**
 * Created by user on 15-Aug-17.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.GroupViewHolder>{

    List<Group> list;
    Context context;
    List<String> groupId;

    public RecyclerAdapter(List<String> groupId, List<Group> list, Context context) {
        this.list = list;
        this.context = context;
        this.groupId = groupId;
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_group,parent,false);
        GroupViewHolder gvh = new GroupViewHolder(view);
        return gvh;
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        Group g = list.get(position);
        holder.nameL.setText(g.getName());
        if(g.getMembers() == null)
            holder.numMemberL.setText("0 member");
        else if(g.getMembers().size() == 1)
            holder.numMemberL.setText("1 member");
        else if(g.getMembers().size() == 0)
            holder.numMemberL.setText("You're in!");
        else
            holder.numMemberL.setText(g.getMembers().size() + " members");
        Glide.with(holder.imageL.getContext())
                .load(g.getGroupImageUrl())
                .into(holder.imageL);
        holder.left.setTag(groupId.get(position));
        switch(position % 4) {
            case 0:
                holder.left.setBackground(context.getDrawable(R.drawable.round_group));
                break;
            case 1:
                holder.left.setBackground(context.getDrawable(R.drawable.round_group_1));
                break;
            case 2:
                holder.left.setBackground(context.getDrawable(R.drawable.round_group_2));
                break;
            case 3:
                holder.left.setBackground(context.getDrawable(R.drawable.round_group_3));
                break;
        }
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

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView nameL;
        TextView nameR;
        de.hdodenhof.circleimageview.CircleImageView imageL;
        de.hdodenhof.circleimageview.CircleImageView imageR;
        TextView numMemberL;
        TextView numMemberR;
        LinearLayout right;
        LinearLayout left;

        public GroupViewHolder(View v) {
            super(v);
            nameL = (TextView) itemView.findViewById(R.id.nameL);
            imageL = (de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.imageL);
            numMemberL = (TextView) itemView.findViewById(R.id.numMemberL);
            left = (LinearLayout) itemView.findViewById(R.id.left);
        }

        public void vanish() {
            itemView.findViewById(R.id.master).setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        }

        public void changeId(String groupId) {
            itemView.setId(Integer.parseInt(groupId));
        }

    }
}