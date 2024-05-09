package com.example.landlord_g11.models

import com.google.firebase.firestore.DocumentId

data class Property(

    @DocumentId var id: String = "",
    var address: String = "",
    var imageUrl: String = "",
    var rentalPrice: Double = 0.0,
    var rentalType: String = "",
    var numberOfBedrooms: Int = 0,
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var landlordID: String = "",
    @JvmField var isAvailable: Boolean = true
)
