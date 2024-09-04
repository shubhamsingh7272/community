package com.pratik.iiits

import PollAdapter
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.Models.Poll
import com.pratik.iiits.Models.UserModel
import com.pratik.iiits.utils.PollUtils

class PollsFragment : Fragment() {
    private lateinit var pollsRecyclerView: RecyclerView
    private lateinit var pollAdapter: PollAdapter
    private lateinit var firestoreDb: FirebaseFirestore
    private var currentUser: UserModel? = null
    private val pollIds = mutableListOf<String>()
    private var previousPolls: List<Poll> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_polls, container, false)

        // Initialize Firestore and RecyclerView
        firestoreDb = FirebaseFirestore.getInstance()
        pollsRecyclerView = view.findViewById(R.id.rvPolls)
        pollsRecyclerView.layoutManager = LinearLayoutManager(context)

        // Load previous polls from SharedPreferences
        previousPolls = PollUtils.loadPolls(requireContext())
        Log.d("PollsFragment", "Loaded previous polls: $previousPolls")

        // Fetch current user information
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            firestoreDb.collection("users")
                .document(currentUserUid)
                .get()
                .addOnSuccessListener { userSnapshot ->
                    currentUser = userSnapshot.toObject(UserModel::class.java)
                    Log.d("PollsFragment", "Current user: $currentUser")
                    // Initialize adapter and set it to RecyclerView
                    pollAdapter = PollAdapter(requireContext(), pollsRecyclerView, listOf(), currentUser!!)
                    pollsRecyclerView.adapter = pollAdapter
                    // Fetch polls from Firestore
                    fetchPollsFromFirestore()
                }
                .addOnFailureListener { exception ->
                    Log.e("PollsFragment", "Error fetching current user", exception)
                }
        }

        return view
    }

    private fun fetchPollsFromFirestore() {
        firestoreDb.collection("polls")
            .addSnapshotListener { result, error ->
                if (error != null) {
                    Log.e("PollsFragment", "Error fetching polls", error)
                    return@addSnapshotListener
                }

                if (result != null) {
                    val polls = result.map { document ->
                        pollIds.add(document.id)  // Store document ID
                        document.toObject(Poll::class.java).also {
                            Log.d("PollsFragment", "Fetched poll: ${it.question}, options: ${it.options}")
                        }
                    }
                    Log.d("PollsFragment", "Current polls: $polls")
                    Log.d("PollsFragment", "Previous polls: $previousPolls")

                    pollAdapter.updatePolls(polls, pollIds)
                    if (polls.size != previousPolls.size) {

                        sendNotification("New poll update available!")
                        previousPolls = polls
                        // Save updated polls to SharedPreferences
                        PollUtils.savePolls(requireContext(), previousPolls)
                        Log.d("PollsFragment", "Saved new polls: $previousPolls")
                    }
                }
            }
    }

    private fun sendNotification(messageBody: String) {
        val intent = Intent(context, EventsActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "poll_updates_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.logo_splash_chatapp)
            .setContentTitle("Poll Update")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since Android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Poll Updates", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}
