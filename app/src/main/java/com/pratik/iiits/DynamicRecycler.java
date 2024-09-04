package com.pratik.iiits;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DynamicRecycler extends RecyclerView.Adapter<DynamicRecycler.DynamicHolder> {
    public ArrayList<DynamicRvModel>dynamicRvModels;
    Integer[] colorlist = {R.color.darktheme_front,R.color.darktheme_front,R.color.darktheme_front,R.color.darktheme_front};
    public DynamicRecycler(ArrayList<DynamicRvModel> dynamicRvModels){
        this.dynamicRvModels =dynamicRvModels;
    }
    public class DynamicHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView heading;
        public TextView subject;
        public CardView cardView;
        public DynamicHolder(@NonNull View itemView) {
            super(itemView);
            imageView= itemView.findViewById(R.id.image_view);
            heading = itemView.findViewById(R.id.headingtitle);
            subject = itemView.findViewById(R.id.discription);
            cardView = itemView.findViewById(R.id.cardviewevent);
        }
    }
    @NonNull
    @Override
    public DynamicRecycler.DynamicHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dynamic_item_layout,parent,false);

        DynamicHolder dynamicHolder = new DynamicHolder(view);
        return dynamicHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DynamicRecycler.DynamicHolder holder, int position) {
        DynamicRvModel currentitem = dynamicRvModels.get(position);
        Context context = holder.imageView.getContext();
        Glide.with(context).load(currentitem.getImage()).into(holder.imageView);
        holder.heading.setText(currentitem.getName());
        holder.subject.setText(currentitem.getSubject());
        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context,colorlist[(position/2)%4]));
    }

    @Override
    public int getItemCount() {
        return dynamicRvModels.size();
    }


}
