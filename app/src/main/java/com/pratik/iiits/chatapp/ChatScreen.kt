package com.pratik.iiits.chatapp

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.pratik.iiits.Models.MessageModel
import com.pratik.iiits.ProfilePage
import com.pratik.iiits.R
import com.pratik.iiits.notes.Adapters.MessageAdapter
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList

class ChatScreen : AppCompatActivity() {

    lateinit var Reciverimage:String
    lateinit var Recivername:String
    lateinit var Reciveruid:String
    lateinit var SenderUid:String

    lateinit var imageViewreciever : CircleImageView
    lateinit var recievername: TextView
    lateinit var recieveronline : TextView
    lateinit var messageedittext : EditText
    lateinit var database: FirebaseDatabase
    lateinit var auth: FirebaseAuth
    companion object {
        lateinit var senderImage : String
        lateinit var reciveImage : String

    }

    lateinit var chatadapter: MessageAdapter
    lateinit var senderRoom:String
    lateinit var reciverRoom: String
    lateinit var messageModelArrayList: ArrayList<MessageModel>
    lateinit var messageRecycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_screen)
        hook()

        Reciverimage  = intent.getStringExtra("ReciverImage").toString()
        Recivername  = intent.getStringExtra("name").toString()
        Reciveruid  = intent.getStringExtra("uid").toString()
        SenderUid = auth.uid.toString()

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd= true
        messageRecycler.layoutManager = linearLayoutManager
        chatadapter = MessageAdapter(this@ChatScreen,messageModelArrayList)
        messageRecycler.adapter = chatadapter

        Picasso.get().load(Reciverimage).into(imageViewreciever)

        recievername.text = Recivername


        senderRoom = SenderUid+Reciveruid
        reciverRoom = Reciveruid+SenderUid



       val userref =  database
           .reference
           .child("users")
           .child(auth.uid.toString())

        val chatref =  database
            .reference
            .child("chats")
            .child(senderRoom)
            .child("messages")

        chatref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                messageModelArrayList.clear()
                for (datasnapshot in snapshot.children){
                    val messageModel = MessageModel(
                        datasnapshot.child("message").value.toString(),
                        datasnapshot.child("senderId").value.toString(),
                        datasnapshot.child("timestamp").value as Long
                        )
                    messageModelArrayList.add(messageModel)
                }
                chatadapter.notifyDataSetChanged()
                messageRecycler.scrollToPosition(chatadapter.itemCount-1)

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        userref.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                senderImage = snapshot.child("imageUri").getValue<String>().toString()
                reciveImage = Reciverimage

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Failed to read value.", error.toException())
            }

        })

        database.getReference("users").child(Reciveruid).child("onlineStatus").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value.toString()=="offline"){
                    recieveronline.text = "Offline"
                    recieveronline.setTextColor(resources.getColor(R.color.red))
                }
                else {
                    recieveronline.text = "Online"
                    recieveronline.setTextColor(resources.getColor(R.color.green))
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
    fun sendmessage(view: View) {
        val message: String = messageedittext.text.toString()
        if (message.isEmpty()){
            return
        }
        else{
            messageedittext.setText("")
            val date = Date()
            val messages = MessageModel(message,SenderUid,date.time)
            database.reference
                .child("chats")
                .child(senderRoom)
                .child("messages")
                .push()
                .setValue(messages).addOnCompleteListener {

                    database.reference
                        .child("chats")
                        .child(reciverRoom)
                        .child("messages")
                        .push()
                        .setValue(messages).addOnCompleteListener {


                        }
                }
        }

    }
    private fun hook() {
        imageViewreciever = findViewById(R.id.reciever_image)
        recievername = findViewById(R.id.reciever_name)
        messageedittext = findViewById(R.id.edittextmessage)
        messageRecycler= findViewById(R.id.messageadapter)
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        messageModelArrayList = ArrayList<MessageModel>()
        recieveronline = findViewById(R.id.reciever_online);
    }

    fun closeChat(view: View) {
        finish()
    }

    fun OpenEmoji(view: View) {}
    fun openprofile(view: View) {
        startActivity(Intent(this@ChatScreen, ProfilePage::class.java).putExtra("authuid",Reciveruid).putExtra("self",false))

    }

}