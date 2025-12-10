package com.jomap.app.data

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// 1. نموذج البيانات لاستقبال الرد من API
data class NominatimResponse(
    @SerializedName("geojson") val geojson: GeoJsonData
)

data class GeoJsonData(
    @SerializedName("type") val type: String,
    @SerializedName("coordinates") val coordinates: List<Any> // نستخدم Any لأن الهيكلة معقدة
)

// 2. واجهة الاتصال (API Interface)
interface NominatimApiService {
    @GET("search")
    suspend fun getCountryBoundary(
        @Query("country") country: String,
        @Query("format") format: String = "json",
        @Query("polygon_geojson") polygonGeojson: Int = 1,
        @Query("polygon_threshold") threshold: Double = 0.005 // لتبسيط الحدود وتقليل حجم البيانات
    ): List<NominatimResponse>
}

// 3. كائن للاتصال (Singleton)
object BoundaryApiClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    val service: NominatimApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApiService::class.java)
    }
}

// 4. دالة مساعدة لتحويل GeoJSON إلى قائمة نقاط LatLng
fun parseGeoJsonToLatLng(geojson: GeoJsonData): List<List<LatLng>> {
    val polygons = mutableListOf<List<LatLng>>()

    try {
        if (geojson.type == "Polygon") {
            // Polygon بسيط
            val coords = geojson.coordinates[0] as List<List<Double>>
            polygons.add(coords.map { LatLng(it[1], it[0]) }) // LatLng تعكس الترتيب (lat, lng)
        } else if (geojson.type == "MultiPolygon") {
            // MultiPolygon (دولة فيها جزر أو أجزاء منفصلة)
            val multiCoords = geojson.coordinates as List<List<List<List<Double>>>>
            for (polygonCoords in multiCoords) {
                val coords = polygonCoords[0]
                polygons.add(coords.map { LatLng(it[1], it[0]) })
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return polygons
}