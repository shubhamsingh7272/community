package com.pratik.iiits.notes.Adapters;

import static com.pratik.iiits.chatapp.ChatScreen.reciveImage;
import static com.pratik.iiits.chatapp.ChatScreen.senderImage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.ktx.Firebase;
import com.pratik.iiits.Models.MessageModel;
import com.pratik.iiits.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter {

    Context context;
    ArrayList<MessageModel> messageModelArrayList;
    int ITEM_SEND = 1,ITEM_RECIEVE =2;


    public MessageAdapter(Context context, ArrayList<MessageModel> messageModelArrayList) {
        this.context = context;
        this.messageModelArrayList = messageModelArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==ITEM_SEND)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.senderlayoutitem,parent,false);
            return new SenderViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.recieverlayoutitem,parent,false);
            return new ReciverViewHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        MessageModel messageModel = messageModelArrayList.get(position);

        if (holder.getClass()==SenderViewHolder.class){
            SenderViewHolder viewHolder =( SenderViewHolder) holder;
            viewHolder.txtmessage.setText(messageModel.getMessage());
            Picasso.get().load(senderImage).into(viewHolder.circleImageView);
        }
        else {
            ReciverViewHolder viewHolder =( ReciverViewHolder) holder;
            viewHolder.txtmessage.setText(messageModel.getMessage());
            Picasso.get().load(reciveImage).into(viewHolder.circleImageView);

        }
    }

    @Override
    public int getItemCount() {
        return messageModelArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel messageModel  = messageModelArrayList.get(position);
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messageModel.getSenderId())){
            return ITEM_SEND;
        }
        else return ITEM_RECIEVE;
    }

    class SenderViewHolder extends RecyclerView.ViewHolder{

        CircleImageView circleImageView ;
        TextView txtmessage;
        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.sernder_image);
            txtmessage = itemView.findViewById(R.id.TextMessages);
        }
    }

    class ReciverViewHolder extends RecyclerView.ViewHolder{
        CircleImageView circleImageView ;
        TextView txtmessage;
        public ReciverViewHolder(@NonNull View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.reciever_image);
            txtmessage = itemView.findViewById(R.id.TextMessages);
        }
    }
}
