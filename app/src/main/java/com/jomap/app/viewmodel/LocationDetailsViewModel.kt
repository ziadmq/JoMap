package com.jomap.app.viewmodel

import com.jomap.app.R
import androidx.lifecycle.ViewModel
import com.jomap.app.data.model.NearbyLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class LocationDetailsViewModel : ViewModel() {

    private val _allLocations = listOf(
        NearbyLocation(id = "1", name = "مطعم السلطان", rating = 4.5, lat = 31.9568, lng = 35.9153, imageRes = R.drawable.ic_launcher_background, category = "مطاعم", visitCount = 1500, distanceKm = 1.2),
        NearbyLocation(id = "2", name = "كافيه عمان", rating = 4.2, lat = 31.9523, lng = 35.9108, imageRes = R.drawable.ic_launcher_background, category = "كافيهات", visitCount = 850, distanceKm = 0.5),
        NearbyLocation(id = "3", name = "حديقة الحسين", rating = 4.8, lat = 31.9590, lng = 35.9086, imageRes = R.drawable.ic_launcher_background, category = "منتزهات", visitCount = 5000, distanceKm = 3.5),
        NearbyLocation(id = "4", name = "فندق الرويال", rating = 4.6, lat = 31.9600, lng = 35.9100, imageRes = R.drawable.ic_launcher_background, category = "فنادق", visitCount = 2000, distanceKm = 2.0)
    )

    private val _selectedLocation = MutableStateFlow<NearbyLocation?>(null)
    val selectedLocation = _selectedLocation.asStateFlow()

    val reviews = listOf(
        Review("أحمد محمد", 5.0, "مكان رائع جداً والخدمة ممتازة!", "يومين مضت"),
        Review("سارة علي", 4.0, "الأكل لذيذ لكن المكان مزدحم قليلاً.", "أسبوع مضى"),
        Review("خالد يوسف", 4.5, "أنصح بتجربة المنسف، شيء خرافي.", "شهر مضى")
    )

    val ownerPost = OwnerPost(
        ownerName = "الإدارة",
        content = "أهلاً بكم في مطعمنا! يسرنا إعلامكم بتوفر قائمة جديدة خاصة بفصل الشتاء، تشمل مشروبات ساخنة وحلويات شرقية. زورونا الآن!",
        time = "قبل 3 ساعات"
    )

    fun loadLocation(id: String) {
        val found = _allLocations.find { it.id == id } ?: _allLocations.firstOrNull()
        _selectedLocation.value = found
    }
}
