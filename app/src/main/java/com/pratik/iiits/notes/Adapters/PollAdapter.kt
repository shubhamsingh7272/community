import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.Models.Poll
import com.pratik.iiits.Models.UserModel
import com.pratik.iiits.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PollAdapter(
    private val context: Context,
    private val pollsRecyclerView: RecyclerView,
    private var polls: List<Poll>,
    private val currentUser: UserModel
) : RecyclerView.Adapter<PollAdapter.PollViewHolder>() {

    private val pollIds = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_poll, parent, false)
        return PollViewHolder(view)
    }

    override fun onBindViewHolder(holder: PollViewHolder, position: Int) {
        val poll = polls[position]
        holder.pollQuestionText.text = poll.question
        holder.optionsRadioGroup.removeAllViews()

        holder.itemView.setOnClickListener {
            showPollStatsDialog(poll)
        }

        Log.d("PollAdapter", "Binding poll question: ${poll.question}")
        Log.d("PollAdapter", "Options size: ${poll.options.size}")

        poll.options.forEachIndexed { index, option ->
            Log.d("PollAdapter", "Adding option: $option")
            val radioButton = RadioButton(context).apply {
                text = option
                id = View.generateViewId()
                layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                )
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                buttonTintList = ContextCompat.getColorStateList(context, android.R.color.black)
                isEnabled = !poll.hasUserVoted(currentUser.uid)
            }
            holder.optionsRadioGroup.addView(radioButton)
        }

        poll.createdBy?.let { user ->
            holder.nameTextView.text = user.name
            Picasso.get()
                .load(user.imageUri)
                .placeholder(R.drawable.profile) // Placeholder image
                .error(R.drawable.profile) // Error image
                .into(holder.userImageView)
        }

        // Check if the current user has already voted for this poll
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && poll.voters.containsKey(userId)) {
            val selectedOptionIndex = poll.voters[userId] ?: -1
            if (selectedOptionIndex != -1) {
                val radioButton = holder.optionsRadioGroup.getChildAt(selectedOptionIndex) as RadioButton
                // Temporarily remove listener before updating the checked state
                holder.optionsRadioGroup.setOnCheckedChangeListener(null)
                radioButton.isChecked = true
                // Re-enable listener after updating the checked state
                holder.optionsRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                    val selectedOptionIndex = group.indexOfChild(group.findViewById(checkedId))
                    if (selectedOptionIndex != -1) {
                        Log.d("PollAdapter", "User selected option index: $selectedOptionIndex")
                        registerVote(pollIds[position], selectedOptionIndex)
                    } else {
                        Log.e("PollAdapter", "Invalid option index selected: $selectedOptionIndex")
                    }
                }
            }
            // Disable radio buttons if user has already voted
            disableRadioButtons(holder.optionsRadioGroup)
        } else {
            // Set the listener for the radio buttons
            holder.optionsRadioGroup.setOnCheckedChangeListener { group, checkedId ->
                val selectedOptionIndex = group.indexOfChild(group.findViewById(checkedId))
                if (selectedOptionIndex != -1) {
                    Log.d("PollAdapter", "User selected option index: $selectedOptionIndex")
                    registerVote(pollIds[position], selectedOptionIndex)
                } else {
                    Log.e("PollAdapter", "Invalid option index selected: $selectedOptionIndex")
                }
            }
        }
    }

    private fun registerVote(pollId: String, selectedOptionIndex: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val pollRef = FirebaseFirestore.getInstance().collection("polls").document(pollId)

        pollRef.get().addOnSuccessListener { documentSnapshot ->
            val poll = documentSnapshot.toObject(Poll::class.java)
            if (poll != null) {
                if (!poll.voters.containsKey(userId)) {
                    Log.d("PollAdapter", "User has not voted yet. Registering vote.")
                    poll.voteCounts[selectedOptionIndex] += 1
                    poll.voters[userId] = selectedOptionIndex
                    pollRef.set(poll).addOnSuccessListener {
                        Log.d("PollAdapter", "Vote registered successfully for poll ID: $pollId")
                        Toast.makeText(context, "Vote registered successfully", Toast.LENGTH_SHORT).show()
                        // Disable radio buttons after voting
                        val pollPosition = pollIds.indexOf(pollId)
                        if (pollPosition != -1) {
                            val viewHolder = pollsRecyclerView.findViewHolderForAdapterPosition(pollPosition) as? PollViewHolder
                            viewHolder?.let { disableRadioButtons(it.optionsRadioGroup) }
                        }
                    }.addOnFailureListener { e ->
                        Log.e("PollAdapter", "Error registering vote for poll ID: $pollId", e)
                        Toast.makeText(context, "Failed to register vote", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.w("PollAdapter", "User has already voted for poll ID: $pollId")
                    Toast.makeText(context, "You have already voted", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e("PollAdapter", "Poll not found for poll ID: $pollId")
            }
        }.addOnFailureListener { e ->
            Log.e("PollAdapter", "Error fetching poll for poll ID: $pollId", e)
        }
    }

    private fun disableRadioButtons(radioGroup: RadioGroup) {
        for (i in 0 until radioGroup.childCount) {
            radioGroup.getChildAt(i).isEnabled = false
        }
    }

    override fun getItemCount(): Int = polls.size

    fun updatePolls(newPolls: List<Poll>, newPollIds: List<String>) {
        this.polls = newPolls
        this.pollIds.clear()
        this.pollIds.addAll(newPollIds)
        Log.d("PollAdapter", "Updated polls list, new size: ${newPolls.size}")
        notifyDataSetChanged()
    }

    class PollViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pollQuestionText: TextView = itemView.findViewById(R.id.pollQuestionText)
        val optionsRadioGroup: RadioGroup = itemView.findViewById(R.id.optionsRadioGroup)
        val userImageView: CircleImageView = itemView.findViewById(R.id.userimage)
        val nameTextView: TextView = itemView.findViewById(R.id.name)
    }

    private fun showPollStatsDialog(poll: Poll) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_poll_stats, null)
        val pollQuestionTextView = dialogView.findViewById<TextView>(R.id.pollQuestionTextView)
        val statsRecyclerView = dialogView.findViewById<RecyclerView>(R.id.statsRecyclerView)
        val closeButton = dialogView.findViewById<Button>(R.id.closeButton)

        pollQuestionTextView.text = poll.question

        val pollOptions = poll.options.zip(poll.voteCounts)
        val statsAdapter = StatsAdapter(pollOptions)
        statsRecyclerView.layoutManager = LinearLayoutManager(context)
        statsRecyclerView.adapter = statsAdapter

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }
}
