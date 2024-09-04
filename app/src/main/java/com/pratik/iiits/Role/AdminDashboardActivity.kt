package com.pratik.iiits.Role

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.R

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: RoleRequestAdapter
    private lateinit var groupSpinner: Spinner
    private lateinit var subgroupSpinner: Spinner
    private lateinit var subsubgroupNameEditText: EditText
    private lateinit var createSubsubgroupButton: Button
    private var groupList: List<String> = listOf()
    private var subgroupList: List<String> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_dashboard)

        db = FirebaseFirestore.getInstance()

        val roleRequestsList = findViewById<RecyclerView>(R.id.role_requests_list)
        roleRequestsList.layoutManager = LinearLayoutManager(this)

        adapter = RoleRequestAdapter { requestId, userId, roleName, action ->
            handleRoleRequest(requestId, userId, roleName, action)
        }
        roleRequestsList.adapter = adapter

        groupSpinner = findViewById(R.id.group_spinner)
        subgroupSpinner = findViewById(R.id.subgroup_spinner)
        subsubgroupNameEditText = findViewById(R.id.subsubgroup_name_edit_text)
        createSubsubgroupButton = findViewById(R.id.create_subsubgroup_button)

        fetchGroups() // Fetching all groups

        groupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < groupList.size) {
                    fetchSubgroups(groupList[position])
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        createSubsubgroupButton.setOnClickListener {
            val groupName = groupSpinner.selectedItem?.toString()
            val subgroupName = subgroupSpinner.selectedItem?.toString()
            val subsubgroupName = subsubgroupNameEditText.text.toString().trim()

            if (groupName.isNullOrEmpty() || subgroupName.isNullOrEmpty()) {
                Toast.makeText(this, "Please select both group and subgroup", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (subsubgroupName.isNotEmpty()) {
                createSubsubgroup(groupName, subgroupName, subsubgroupName)
            } else {
                Toast.makeText(this, "Subsubgroup name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch role requests
        fetchRoleRequests()
    }

    // Fetch and populate categories in the first spinner
    private fun fetchGroups() {
        db.collection("groups").get().addOnSuccessListener { result ->
            val uniqueGroups = mutableSetOf<String>()
            for (document in result.documents) {
                val category = document.getString("category")
                if (category != null) {
                    uniqueGroups.add(category)
                }
            }
            groupList = uniqueGroups.toList()
            val groupAdapter = ArrayAdapter(this, R.layout.spinner_item, groupList)
            groupAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            groupSpinner.adapter = groupAdapter
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch groups", Toast.LENGTH_SHORT).show()
        }
    }


    private fun fetchSubgroups(groupName: String) {
        db.collection("groups")
            .whereEqualTo("category", groupName)
            .get()
            .addOnSuccessListener { result ->
                val uniqueSubgroups = mutableSetOf<String>()
                for (document in result.documents) {
                    val subgroupName = document.getString("name")
                    if (subgroupName != null) {
                        uniqueSubgroups.add(subgroupName)
                    }
                }
                subgroupList = uniqueSubgroups.toList()
                val subgroupAdapter = ArrayAdapter(this, R.layout.spinner_item, subgroupList)
                subgroupAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                subgroupSpinner.adapter = subgroupAdapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch subgroups", Toast.LENGTH_SHORT).show()
            }
    }





    private fun createSubsubgroup(groupName: String, subgroupName: String, subsubgroupName: String) {
        val subsubgroup = hashMapOf("name" to subsubgroupName, "level" to "subsubgroup", "parent" to subgroupName)
        db.collection("roles").document(groupName).collection("subgroups").document(subgroupName).collection("subsubgroups").document(subsubgroupName).set(subsubgroup).addOnSuccessListener {
            Toast.makeText(this, "Subsubgroup created", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to create subsubgroup", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchRoleRequests() {
        db.collection("roleRequests").whereEqualTo("status", "pending").get().addOnSuccessListener { result ->
            val requests = result.map { document ->
                document.toObject(RoleRequest::class.java).apply {
                    id = document.id
                }
            }
            adapter.submitList(requests)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to fetch role requests", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleRoleRequest(requestId: String, userId: String, roleName: String, action: String) {
        val status = if (action == "approve") "approved" else "rejected"
        db.collection("roleRequests").document(requestId).update("status", status).addOnSuccessListener {
            if (action == "approve") {
                db.collection("users").document(userId).update("roles", com.google.firebase.firestore.FieldValue.arrayUnion(roleName)).addOnSuccessListener {
                    Toast.makeText(this, "Role request approved", Toast.LENGTH_SHORT).show()
                    adapter.removeRequest(requestId)
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed to update user roles", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Role request rejected", Toast.LENGTH_SHORT).show()
                adapter.removeRequest(requestId)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to update role request", Toast.LENGTH_SHORT).show()
        }
    }
}
