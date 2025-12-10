package com.jomap.app.viewmodel

import androidx.lifecycle.ViewModel
import com.jomap.app.R
import com.jomap.app.data.model.UserActivityReview
import com.jomap.app.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {

    // 1. بيانات المستخدم الوهمية
    private val _user = MutableStateFlow(
        UserProfile(
            name = "زياد المطور",
            email = "ziad@jomap.com",
            phone = "+962 79 123 4567",
            imageRes = R.drawable.ic_launcher_background // استبدلها بصورة حقيقية
        )
    )
    val user = _user.asStateFlow()

    // 2. سجل مراجعات المستخدم
    private val _myReviews = MutableStateFlow(
        listOf(
            UserActivityReview("مطعم السلطان", 5.0, "الأكل كان رائعاً جداً!", "منذ يومين"),
            UserActivityReview("كافيه عمان", 3.5, "الخدمة بطيئة قليلاً.", "منذ أسبوع"),
            UserActivityReview("حديقة الحسين", 4.0, "مكان جميل للعائلات.", "منذ شهر")
        )
    )
    val myReviews = _myReviews.asStateFlow()

    // --- Actions ---

    fun onLogout() {
        // هنا تضع منطق تسجيل الخروج (مسح التوكن، الانتقال لشاشة الدخول...)
    }

    fun updateProfile(name: String, phone: String) {
        // تحديث البيانات
        val currentUser = _user.value
        _user.value = currentUser.copy(name = name, phone = phone)
    }
}