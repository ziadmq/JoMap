package com.jomap.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jomap.app.R
import com.jomap.app.data.model.NearbyLocation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class SortOption {
    DISTANCE, RATING, VISITS
}
data class OwnerPost(val ownerName: String, val content: String, val time: String)
data class Review(val name: String, val rating: Double, val comment: String, val date: String)
class LocationListViewModel : ViewModel() {

    // --- بيانات المواقع ---
    private val _allLocations = listOf(
        NearbyLocation(UUID.randomUUID().toString(), "مطعم السلطان", 4.5, 31.9568, 35.9153, R.drawable.ic_launcher_background, "مطاعم", 1500, 1.2),
        NearbyLocation(UUID.randomUUID().toString(), "كافيه عمان", 4.2, 31.9523, 35.9108, R.drawable.ic_launcher_background, "كافيهات", 850, 0.5),
        NearbyLocation(UUID.randomUUID().toString(), "حديقة الحسين", 4.8, 31.9590, 35.9086, R.drawable.ic_launcher_background, "منتزهات", 5000, 3.5),
        NearbyLocation(UUID.randomUUID().toString(), "فندق الرويال", 4.6, 31.9600, 35.9100, R.drawable.ic_launcher_background, "فنادق", 2000, 2.0)
    )

    private val _uiLocations = MutableStateFlow(_allLocations)
    val uiLocations = _uiLocations.asStateFlow()

    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedSort = MutableStateFlow(SortOption.DISTANCE)
    val selectedSort = _selectedSort.asStateFlow()

    // --- منطق إضافة المراجعة (الجديد) ---
    // الموقع الذي يتم تقييمه حالياً (إذا كان null، فالنافذة مغلقة)
    private val _activeReviewLocation = MutableStateFlow<NearbyLocation?>(null)
    val activeReviewLocation = _activeReviewLocation.asStateFlow()

    private val _rating = MutableStateFlow(0)
    val rating = _rating.asStateFlow()

    private val _reviewText = MutableStateFlow("")
    val reviewText = _reviewText.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    // --- Actions ---

    fun openReviewDialog(location: NearbyLocation) {
        _rating.value = 0
        _reviewText.value = ""
        _activeReviewLocation.value = location
    }

    fun closeReviewDialog() {
        _activeReviewLocation.value = null
    }

    fun setRating(value: Int) { _rating.value = value }
    fun onReviewTextChanged(text: String) { _reviewText.value = text }

    fun submitReview() {
        if (_rating.value == 0) return
        viewModelScope.launch {
            _isSubmitting.value = true
            delay(1500) // محاكاة الاتصال
            _isSubmitting.value = false
            closeReviewDialog()
            // هنا يمكنك إضافة منطق لإظهار رسالة نجاح في الشاشة
        }
    }

    fun filterByCategory(category: String) {
        _selectedCategory.value = category
        applyFilters()
    }

    fun sortList(option: SortOption) {
        _selectedSort.value = option
        applyFilters()
    }

    private fun applyFilters() {
        var list = _allLocations
        if (_selectedCategory.value != "الكل") {
            list = list.filter { it.category == _selectedCategory.value }
        }
        list = when (_selectedSort.value) {
            SortOption.DISTANCE -> list.sortedBy { it.distanceKm }
            SortOption.RATING -> list.sortedByDescending { it.rating }
            SortOption.VISITS -> list.sortedByDescending { it.visitCount }
        }
        _uiLocations.value = list
    }
}

