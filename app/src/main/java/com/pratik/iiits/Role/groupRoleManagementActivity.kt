package com.pratik.iiits.Role

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.R

class groupRoleManagementActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_role_management)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val groupSpinner: AutoCompleteTextView = findViewById(R.id.group_spinner)
        val subgroupSpinner: AutoCompleteTextView = findViewById(R.id.subgroup_spinner)
        val subsubgroupSpinner: AutoCompleteTextView = findViewById(R.id.subsubgroup_spinner)

        // Retrieve group and subgroup from intent
        val selectedGroup = intent.getStringExtra("selectedGroup")
        val selectedSubgroup = intent.getStringExtra("selectedSubgroup")

        // Set group and subgroup spinners to retrieved values and disable them
        groupSpinner.setText(selectedGroup, false)
        subgroupSpinner.setText(selectedSubgroup, false)
        groupSpinner.isEnabled = false
        subgroupSpinner.isEnabled = false

        // Populate subsubgroup spinner based on selected group and subgroup
        if (selectedGroup != null && selectedSubgroup != null) {
            db.collection("roles").document(selectedGroup).collection("subgroups").document(selectedSubgroup).collection("subsubgroups").get().addOnSuccessListener { result ->
                val subsubgroups = result.map { it.getString("name") ?: "" }
                val subsubgroupAdapter = ArrayAdapter(this@groupRoleManagementActivity, android.R.layout.simple_dropdown_item_1line, subsubgroups)
                subsubgroupSpinner.setAdapter(subsubgroupAdapter)
                // Log the data for debugging
                println("Subsubgroups loaded: $subsubgroups for subgroup: $selectedSubgroup")
            }
        }

        val requestButton = findViewById<Button>(R.id.request_button)

        requestButton.setOnClickListener {
            val selectedSubsubgroup = subsubgroupSpinner.text.toString()
            val selectedRole = "$selectedGroup > $selectedSubgroup > $selectedSubsubgroup"
            sendRoleRequest(selectedRole)
        }
    }

    private fun sendRoleRequest(roleName: String) {
        val userId = auth.currentUser?.uid ?: return
        val request = hashMapOf(
            "userId" to userId,
            "roleName" to roleName,
            "status" to "pending"
        )
        db.collection("roleRequests").add(request).addOnSuccessListener {
            Toast.makeText(this, "Role request sent", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to send role request", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}