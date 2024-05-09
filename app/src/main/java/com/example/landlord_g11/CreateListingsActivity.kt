package com.example.landlord_g11

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.landlord_g11.databinding.ActivityCreateListingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.util.Locale

class CreateListingsActivity : AppCompatActivity() {

    private val TAG: String = "LANDLORD_APP"

    lateinit var binding: ActivityCreateListingsBinding
    lateinit var geocoder: Geocoder

    val db = Firebase.firestore
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateListingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        geocoder = Geocoder(applicationContext, Locale.getDefault())
        setSupportActionBar(binding.myToolbar)

        binding.btnSubmit.setOnClickListener {
            addPropertyListing()
        }
    }

    fun addPropertyListing() {
        val addressFromUI = binding.etAddress.text.toString()
        val imageUrlFromUI = binding.etImageUrl.text.toString()
        val rentalPriceFromUI = binding.etRentalPrice.text.toString()
        val rentalTypeFromUI = binding.spRentalType.selectedItem.toString()
        val numBedroomsFromUI = binding.etNumBedrooms.text.toString()

        if (addressFromUI.isEmpty() || imageUrlFromUI.isEmpty() || rentalPriceFromUI.isEmpty() || rentalTypeFromUI.isEmpty() || numBedroomsFromUI.isEmpty()) {
            binding.tvError.visibility = View.VISIBLE
            binding.tvError.text = "ERROR : ALL FIELDS MUST BE FILLED IN"
            return
        }

        val rentalPrice = rentalPriceFromUI.toDouble()
        val numBedrooms = numBedroomsFromUI.toDouble()

        val (latitude, longitude) = getAddressCoordinates(addressFromUI)

        val uid = auth.currentUser?.uid.toString()

        val data: MutableMap<String, Any> = HashMap()
        data["landlordID"] = uid
        data["address"] = addressFromUI
        data["imageUrl"] = imageUrlFromUI
        data["rentalPrice"] = rentalPrice
        data["rentalType"] = rentalTypeFromUI
        data["numberOfBedrooms"] = numBedrooms
        data["latitude"] = latitude
        data["longitude"] = longitude
        data["isAvailable"] = true

        db.collection("properties")
            .add(data)
            .addOnSuccessListener {
                Log.d(TAG, "Document successfully added")
                Toast.makeText(this,"Property Successfully Added", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@CreateListingsActivity, ViewListingsActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding document", e)
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getAddressCoordinates(address: String): Pair<Double, Double> {
        var latitudeFromAddress = 0.0
        var longitudeFromAddress = 0.0

        try {
            val searchResults: MutableList<Address>? = geocoder.getFromLocationName(address, 1)
            if (searchResults == null) {
                Log.d(TAG, "ERROR:Result is null")
                binding.tvError.setText("ERROR: Results is null")
                return Pair(latitudeFromAddress, longitudeFromAddress)
            }
            if (searchResults.isEmpty() == true) {
                Log.d(TAG, "ERROR:No coordinates found")
                binding.tvError.setText("No matching coordinates found.")
                return Pair(latitudeFromAddress, longitudeFromAddress)
            }
            val matchingItem: Address = searchResults.get(0)
            Log.d(TAG, "Coordinate found: (${matchingItem.latitude}, ${matchingItem.longitude})")
            latitudeFromAddress = matchingItem.latitude
            longitudeFromAddress = matchingItem.longitude
        } catch (e: IOException) {
            Log.e(TAG, "Error getting coordinates from address", e)
        }
        return Pair(latitudeFromAddress, longitudeFromAddress)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.mi_View_Listings -> {
                val intent = Intent(this@CreateListingsActivity, ViewListingsActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}

