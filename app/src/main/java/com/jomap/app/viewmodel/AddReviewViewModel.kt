package com.jomap.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddReviewViewModel : ViewModel() {

    // حالة النجوم (0 إلى 5)
    private val _rating = MutableStateFlow(0)
    val rating = _rating.asStateFlow()

    // نص المراجعة
    private val _reviewText = MutableStateFlow("")
    val reviewText = _reviewText.asStateFlow()

    // حالة التحميل (أثناء الإرسال)
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // حالة النجاح (لإغلاق الشاشة أو إظهار رسالة)
    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    fun setRating(value: Int) {
        _rating.value = value
    }

    fun onReviewTextChanged(text: String) {
        _reviewText.value = text
    }

    fun submitReview(locationId: String) {
        if (_rating.value == 0) return // منع الإرسال بدون تقييم

        viewModelScope.launch {
            _isLoading.value = true
            // محاكاة الاتصال بالسيرفر (تأخير 2 ثانية)
            delay(2000)

            // هنا تضع كود إرسال البيانات للـ Backend لاحقاً

            _isLoading.value = false
            _isSuccess.value = true
        }
    }
}