import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pratik.iiits.R
import com.pratik.iiits.Role.GroupItem

class GroupAdapter(private var groupList: List<GroupItem>) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_role, parent, false)
        return GroupViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val groupItem = groupList[position]
        holder.bind(groupItem)
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    fun updateData(newGroupList: List<GroupItem>) {
        groupList = newGroupList
        notifyDataSetChanged()
    }

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupTextView: TextView = itemView.findViewById(R.id.group_text_view)
        private val subgroupTextView: TextView = itemView.findViewById(R.id.subgroup_text_view)
        private val subsubgroupTextView: TextView = itemView.findViewById(R.id.subsubgroup_text_view)

        fun bind(groupItem: GroupItem) {
            groupTextView.text = groupItem.groupName
            subgroupTextView.text = groupItem.subGroupName
            subsubgroupTextView.text = groupItem.subSubGroupName
        }
    }
}
