import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pratik.iiits.R

class StatsAdapter(private val pollOptions: List<Pair<String, Int>>) :
    RecyclerView.Adapter<StatsAdapter.StatsViewHolder>() {

    class StatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val optionTextView: TextView = itemView.findViewById(R.id.optionTextView)
        val votesTextView: TextView = itemView.findViewById(R.id.votesTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_poll_stat, parent, false)
        return StatsViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatsViewHolder, position: Int) {
        val (option, votes) = pollOptions[position]
        holder.optionTextView.text = option
        holder.votesTextView.text = "$votes votes"
    }

    override fun getItemCount(): Int = pollOptions.size
}
