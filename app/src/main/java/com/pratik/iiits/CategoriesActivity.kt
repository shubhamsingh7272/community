package com.pratik.iiits

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pratik.iiits.Adapters.CategoryAdapter

class CategoriesActivity : AppCompatActivity() {

    private lateinit var categoriesRecyclerView: RecyclerView
    private val categoryViewModel: CategoryViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        categoriesRecyclerView = findViewById(R.id.categories_recycler_view)
        categoriesRecyclerView.layoutManager = LinearLayoutManager(this)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        checkIfAdmin()

        val colors = listOf(
            getColor(R.color.colorItem1),
            getColor(R.color.colorItem2),
            getColor(R.color.colorItem3),
            getColor(R.color.colorItem4)
        )

        categoryViewModel.categories.observe(this, Observer { categories ->
            categoriesRecyclerView.adapter = CategoryAdapter(categories, colors, ::openCategory)
        })

        findViewById<MaterialButton>(R.id.group_requests_button).setOnClickListener {
            val intent = Intent(this, GroupRequestsActivity::class.java)
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.add_category_button).setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun checkIfAdmin() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val postInIIIT = document.getString("postinIIIT")
                        Log.e(ContentValues.TAG, postInIIIT.toString())
                        if (postInIIIT == "Admin" ) {
                            findViewById<MaterialButton>(R.id.group_requests_button).visibility = MaterialButton.VISIBLE
                            findViewById<MaterialButton>(R.id.add_category_button).visibility = MaterialButton.VISIBLE
                        }
                        else if(postInIIIT == "Council"){
                            findViewById<MaterialButton>(R.id.group_requests_button).visibility = MaterialButton.VISIBLE
                            findViewById<MaterialButton>(R.id.add_category_button).visibility = MaterialButton.GONE
                        }else {
                            findViewById<MaterialButton>(R.id.group_requests_button).visibility = MaterialButton.GONE
                            findViewById<MaterialButton>(R.id.add_category_button).visibility = MaterialButton.GONE
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(ContentValues.TAG, "Error fetching user details: $e")
                    findViewById<MaterialButton>(R.id.group_requests_button).visibility = MaterialButton.GONE
                    findViewById<MaterialButton>(R.id.add_category_button).visibility = MaterialButton.GONE
                }
        }
    }

    private fun openCategory(category: String) {
        val intent = Intent(this, GroupsListActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }

    private fun showAddCategoryDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Category")

        val inputLayout = TextInputLayout(this).apply {
            setPadding(16, 0, 16, 0)
        }

        val input = TextInputEditText(this).apply {
            hint = "Category Name"
        }

        inputLayout.addView(input)
        builder.setView(inputLayout)

        builder.setPositiveButton("Add") { dialog, _ ->
            val categoryName = input.text.toString()
            if (categoryName.isNotEmpty()) {
                categoryViewModel.addCategory(categoryName)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
}
