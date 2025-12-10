package com.jomap.app.data.repository

import com.jomap.app.data.model.Location
import com.jomap.app.data.model.Review

class LocationRepository {

    // مبدئياً: داتا تجريبية (Fake Data)، لاحقاً تربطها مع API أو Firebase
    suspend fun getNearbyLocations(): List<Location> {
        // TODO: Replace with real implementation
        return emptyList()
    }

    suspend fun getLocationDetails(id: String): Location? {
        // TODO
        return null
    }

    suspend fun getLocationReviews(locationId: String): List<Review> {
        // TODO
        return emptyList()
    }

    suspend fun addReview(review: Review) {
        // TODO
    }
}
