package com.pratik.iiits.Role

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.R

class UserRoleManagementActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_role_management)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val groupSpinner: AutoCompleteTextView = findViewById(R.id.group_spinner)
        val subgroupSpinner: AutoCompleteTextView = findViewById(R.id.subgroup_spinner)
        val subsubgroupSpinner: AutoCompleteTextView = findViewById(R.id.subsubgroup_spinner)

        // Set empty adapters initially
        groupSpinner.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, listOf()))
        subgroupSpinner.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, listOf()))
        subsubgroupSpinner.setAdapter(ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, listOf()))

        setSpinnerOnItemClickListener(groupSpinner, findViewById(R.id.group_text_input_layout))
        setSpinnerOnItemClickListener(subgroupSpinner, findViewById(R.id.subgroup_text_input_layout))
        setSpinnerOnItemClickListener(subsubgroupSpinner, findViewById(R.id.subsubgroup_text_input_layout))
        val requestButton = findViewById<Button>(R.id.request_button)

        // Populate group spinner from Firestore
        db.collection("roles").get().addOnSuccessListener { result ->
            val groups = result.map { it.id }
            val groupAdapter = ArrayAdapter(this, R.layout.dropdown_item, groups)
            groupSpinner.setAdapter(groupAdapter)
            // Log the data for debugging
            println("Groups loaded: $groups")
        }

        // Populate subgroup spinner based on selected group
        groupSpinner.setOnItemClickListener { parent, view, position, id ->
            val selectedGroup = groupSpinner.text.toString()
            db.collection("roles").document(selectedGroup).collection("subgroups").get().addOnSuccessListener { result ->
                val subgroups = result.map { it.id }
                val subgroupAdapter = ArrayAdapter(this@UserRoleManagementActivity, R.layout.dropdown_item, subgroups)
                subgroupSpinner.setAdapter(subgroupAdapter)
                // Log the data for debugging
                println("Subgroups loaded: $subgroups for group: $selectedGroup")
            }
        }

        // Populate subsubgroup spinner based on selected subgroup
        subgroupSpinner.setOnItemClickListener { parent, view, position, id ->
            val selectedGroup = groupSpinner.text.toString()
            val selectedSubgroup = subgroupSpinner.text.toString()
            db.collection("roles").document(selectedGroup).collection("subgroups").document(selectedSubgroup).collection("subsubgroups").get().addOnSuccessListener { result ->
                val subsubgroups = result.map { it.id }
                val subsubgroupAdapter = ArrayAdapter(this@UserRoleManagementActivity, R.layout.dropdown_item, subsubgroups)
                subsubgroupSpinner.setAdapter(subsubgroupAdapter)
                // Log the data for debugging
                println("Subsubgroups loaded: $subsubgroups for subgroup: $selectedSubgroup")
            }
        }

        requestButton.setOnClickListener {
            val selectedGroup = groupSpinner.text.toString()
            val selectedSubgroup = subgroupSpinner.text.toString()
            val selectedSubsubgroup = subsubgroupSpinner.text.toString()
            val selectedRole = "$selectedGroup > $selectedSubgroup > $selectedSubsubgroup"
            sendRoleRequest(selectedRole)
        }
    }

    private fun setSpinnerOnItemClickListener(spinner: AutoCompleteTextView, textInputLayout: TextInputLayout) {
        spinner.setOnItemClickListener { _, _, _, _ ->
            textInputLayout.hint = textInputLayout.hint.toString() // Reset hint to trigger it to move up
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
    }
}
