package com.example.landlord_g11

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.landlord_g11.adapters.PropertyListingAdapter
import com.example.landlord_g11.databinding.ActivityViewListingsBinding
import com.example.landlord_g11.models.Property
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ViewListingsActivity : AppCompatActivity() {

    private val TAG: String = "LANDLORD_APP"
    lateinit var binding: ActivityViewListingsBinding
    lateinit var propertyListingAdapter: PropertyListingAdapter

    val db = Firebase.firestore
    lateinit var auth: FirebaseAuth

    var propertyList: MutableList<Property> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewListingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        propertyListingAdapter = PropertyListingAdapter(propertyList, deleteRowButtonClicked, goToPropertyDetails)
        binding.rv.adapter = propertyListingAdapter
        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )
        loadData()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() executing....")
        loadData()
    }

    val deleteRowButtonClicked: (String) -> Unit = { docId: String
        ->
        deleteDocument(docId)
    }

    fun loadData() {
        val uid = auth.currentUser?.uid.toString()

        Log.d(TAG, "Loading data")

        db.collection("properties")
            .whereEqualTo("landlordID", uid)
            .get()
            .addOnSuccessListener { result: QuerySnapshot ->
                propertyList.clear()
                for (document: QueryDocumentSnapshot in result) {
                    val propertyFromDB: Property = document.toObject(Property::class.java)
                    propertyList.add(propertyFromDB)
                }
                propertyListingAdapter.notifyDataSetChanged()
                Log.d(TAG, "Number of items retrieved from Firestore: ${propertyList.size}")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error retrieving documents", exception)
            }
    }

    fun deleteDocument(docId: String) {
        db.collection("properties")
            .document(docId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Document deleted!")
                loadData()

            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error deleting document", e)
            }
    }

    val goToPropertyDetails: (Int) -> Unit = { position ->
        val property = propertyList[position]
        val intent = Intent(this@ViewListingsActivity, PropertyDetailsActivity::class.java)
        intent.putExtra("documentId", property.id)
        startActivity(intent)
    }
}