package com.alaythiaproductions.instagramclone.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alaythiaproductions.instagramclone.R;
import com.alaythiaproductions.instagramclone.models.GroupList;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.HolderGroupList> {

    private Context context;
    private ArrayList<GroupList> groupLists;

    public GroupListAdapter(Context context, ArrayList<GroupList> groupLists) {
        this.context = context;
        this.groupLists = groupLists;
    }

    @NonNull
    @Override
    public HolderGroupList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate Layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_groups, parent, false);

        return new HolderGroupList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupList holder, int position) {

        // Get Data
        GroupList model = groupLists.get(position);
        String groupId = model.getGroupId();
        String icon = model.getGroupIcon();
        String name = model.getGroupName();

        holder.group_name_textview.setText(name);

         try {
             Picasso.get().load(icon).placeholder(R.drawable.ic_group_default).into(holder.group_icon_imageview);
         } catch (Exception e) {
             holder.group_icon_imageview.setImageResource(R.drawable.ic_group_default);
         }


    }

    @Override
    public int getItemCount() {
        return groupLists.size();
    }

    class HolderGroupList extends RecyclerView.ViewHolder {

        ImageView group_icon_imageview;
        TextView group_name_textview, group_sender_textview, group_message_textview, group_timestamp_textview;

        public HolderGroupList(@NonNull View itemView) {
            super(itemView);

            group_icon_imageview = itemView.findViewById(R.id.group_icon_imageview);
            group_name_textview = itemView.findViewById(R.id.group_name_textview);
            group_sender_textview = itemView.findViewById(R.id.group_sender_textview);
            group_message_textview = itemView.findViewById(R.id.group_message_textview);
            group_timestamp_textview = itemView.findViewById(R.id.group_timestamp_textview);

        }
    }
}
