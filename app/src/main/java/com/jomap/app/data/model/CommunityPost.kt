package com.jomap.app.data.model

import com.google.android.gms.maps.model.LatLng
import java.util.UUID

data class CommunityPost(
    val id: String = UUID.randomUUID().toString(),
    val governorateId: String, // "0" for Amman, "11" for Aqaba, etc.
    val placeName: String,
    val description: String,
    val type: PostType, // Offer or Event
    val location: LatLng,
    val imageRes: Int,
    val date: String
)

enum class PostType {
    OFFER, EVENT, NEWS
}