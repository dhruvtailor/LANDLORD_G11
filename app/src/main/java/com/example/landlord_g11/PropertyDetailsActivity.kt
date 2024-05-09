package com.example.landlord_g11

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.landlord_g11.databinding.ActivityPropertyDetailsBinding
import com.example.landlord_g11.models.Property
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.util.Locale

class PropertyDetailsActivity : AppCompatActivity() {

    private val TAG: String = "LANDLORD_APP"

    lateinit var binding: ActivityPropertyDetailsBinding
    lateinit var geocoder: Geocoder

    var db = Firebase.firestore
    var documentIdFromViewListings: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPropertyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        geocoder = Geocoder(applicationContext, Locale.getDefault())

        if (intent != null) {
            var idFromIntent = intent.getStringExtra("documentId")
            if (idFromIntent == null) {
                documentIdFromViewListings = ""
            } else {
                documentIdFromViewListings = idFromIntent
            }
        }

        Log.d(TAG, "Document Id: ${documentIdFromViewListings}")

        if (documentIdFromViewListings != null) {
            getDocument(documentIdFromViewListings)
        }

        binding.btnSaveChanges.setOnClickListener {
            updateProperty()
        }

    }

    fun getDocument(documentIdToFind: String) {
        db.collection("properties").document(documentIdToFind).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val property = document.toObject(Property::class.java)

                    if (property != null) {
                        binding.etDocumentId.setText(property.id)
                        binding.etAddress.setText(property.address)
                        binding.etImageUrl.setText(property.imageUrl)
                        binding.etNumBedrooms.setText(property.numberOfBedrooms.toString())
                        binding.etRentalPrice.setText(property.rentalPrice.toString())

                        if(property.rentalType == "HOUSE") {
                            binding.spRentalType.setSelection(0)
                        } else if (property.rentalType == "CONDO") {
                            binding.spRentalType.setSelection(1)
                        } else {
                            binding.spRentalType.setSelection(2)
                        }

                        binding.swIsAvailable.isChecked = property.isAvailable
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Exception getting document", exception)
            }
    }

    fun updateProperty() {
        if (this.documentIdFromViewListings == "") {
            Log.d(TAG, "ERROR, document id is null, cannot update")
            return
        }

        val addressFromUI = binding.etAddress.text.toString()
        val imageUrlFromUI = binding.etImageUrl.text.toString()
        val rentalPriceFromUI = binding.etRentalPrice.text.toString()
        val rentalTypeFromUI = binding.spRentalType.selectedItem.toString()
        val numBedroomsFromUI = binding.etNumBedrooms.text.toString()
        val isAvailableFromUI = binding.swIsAvailable.isChecked

        if (addressFromUI.isEmpty() || imageUrlFromUI.isEmpty() || rentalPriceFromUI.isEmpty() || rentalTypeFromUI.isEmpty() || numBedroomsFromUI.isEmpty()) {
            binding.tvError.visibility = View.VISIBLE
            binding.tvError.text = "ERROR : ALL FIELDS MUST BE FILLED IN"
            return
        }

        val rentalPrice = rentalPriceFromUI.toDouble()
        val numBedrooms = numBedroomsFromUI.toDouble()

        val (latitude, longitude) = getAddressCoordinates(addressFromUI)

        val data: MutableMap<String, Any> = HashMap()
        data["address"] = addressFromUI
        data["imageUrl"] = imageUrlFromUI
        data["rentalPrice"] = rentalPrice
        data["rentalType"] = rentalTypeFromUI
        data["numberOfBedrooms"] = numBedrooms
        data["latitude"] = latitude
        data["longitude"] = longitude
        data["isAvailable"] = isAvailableFromUI

        db.collection("properties").document(this.documentIdFromViewListings)
            .set(data, SetOptions.merge()).addOnSuccessListener { docRef ->
                Log.d(TAG, "Document successfully updated")
                finish()
            }.addOnFailureListener { ex ->
                Log.e(TAG, "Exception occurred while adding a document : $ex")
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
}