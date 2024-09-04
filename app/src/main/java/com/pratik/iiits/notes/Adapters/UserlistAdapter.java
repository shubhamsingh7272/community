package com.pratik.iiits.notes.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pratik.iiits.Models.UserModel;
import com.pratik.iiits.R;
import com.pratik.iiits.chatapp.ChatAppHome;
import com.pratik.iiits.chatapp.ChatScreen;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

// Ensure that the Filterable interface is imported
import android.widget.Filter;
import android.widget.Filterable;

public class UserlistAdapter extends RecyclerView.Adapter<UserlistAdapter.ViewHolder> implements Filterable {

    private Context homeActivity;
    private ArrayList<UserModel> userModelArrayList;
    private ArrayList<UserModel> filteredUserList;

    public UserlistAdapter(@NotNull ChatAppHome chatAppHome, @NotNull ArrayList<UserModel> usersArrayList) {
        this.homeActivity = chatAppHome;
        this.userModelArrayList = usersArrayList;
        this.filteredUserList = new ArrayList<>(usersArrayList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(homeActivity).inflate(R.layout.item_user_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel userModel = filteredUserList.get(position);

        holder.user_name.setText(userModel.getName());
        holder.user_status.setText(userModel.getStatus());
        Picasso.get().load(userModel.getImageUri()).into(holder.user_profile);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(homeActivity, ChatScreen.class);
                intent.putExtra("name", userModel.getName());
                intent.putExtra("ReciverImage", userModel.getImageUri());
                intent.putExtra("uid", userModel.getUid());
                homeActivity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredUserList.size();
    }

    @Override
    public Filter getFilter() {
        return userFilter;
    }

    private Filter userFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<UserModel> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(userModelArrayList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (UserModel user : userModelArrayList) {
                    if (user.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(user);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredUserList.clear();
            filteredUserList.addAll((ArrayList<UserModel>) results.values);
            notifyDataSetChanged();
        }
    };

    public void updateList(ArrayList<UserModel> newList) {
        userModelArrayList = new ArrayList<>(newList);
        filteredUserList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView user_profile;
        TextView user_name, user_status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            user_profile = itemView.findViewById(R.id.users_image);
            user_name = itemView.findViewById(R.id.users_name);
            user_status = itemView.findViewById(R.id.users_status);
        }
    }
}
