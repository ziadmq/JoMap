package com.jomap.app.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.jomap.app.R
import com.jomap.app.data.model.Governorate
import com.jomap.app.data.model.NearbyLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _selectedGovernorate = MutableStateFlow<Governorate?>(null)
    val selectedGovernorate = _selectedGovernorate.asStateFlow()

    private val _governorates = MutableStateFlow<List<Governorate>>(emptyList())
    val governorates = _governorates.asStateFlow()

    // ألوان المحافظات
    private val govColors = listOf(
        Color(0xFFE57373), Color(0xFFBA68C8), Color(0xFF64B5F6), Color(0xFF4DB6AC),
        Color(0xFFFFF176), Color(0xFFFFB74D), Color(0xFFA1887F), Color(0xFF90A4AE),
        Color(0xFFF06292), Color(0xFF7986CB), Color(0xFF4DD0E1), Color(0xFF81C784)
    )

    init {
        loadGovernoratesData()
    }

    private fun loadGovernoratesData() {
        // ملاحظة: في التطبيق الحقيقي يفضل قراءة هذه النصوص من ملف Strings.xml أو API
        // لكن سأضعها هنا مباشرة للسرعة كما طلبت

        val ammanLocs = listOf(
            NearbyLocation(UUID.randomUUID().toString(), "المدرج الروماني", 4.7, 31.951, 35.939, R.drawable.ic_launcher_background, "تاريخي", 5000, 0.0),
            NearbyLocation(UUID.randomUUID().toString(), "جبل القلعة", 4.8, 31.954, 35.935, R.drawable.ic_launcher_background, "تاريخي", 6000, 0.0),
            NearbyLocation(UUID.randomUUID().toString(), "بوليفارد العبدلي", 4.6, 31.968, 35.900, R.drawable.ic_launcher_background, "تسوق", 4000, 0.0)
        )

        val jerashLocs = listOf(
            NearbyLocation(UUID.randomUUID().toString(), "آثار جرش", 4.9, 32.272, 35.891, R.drawable.ic_launcher_background, "تاريخي", 8000, 0.0),
            NearbyLocation(UUID.randomUUID().toString(), "ساحة الأعمدة", 4.8, 32.275, 35.890, R.drawable.ic_launcher_background, "تاريخي", 7500, 0.0)
        )

        val aqabaLocs = listOf(
            NearbyLocation(UUID.randomUUID().toString(), "شاطئ الغندور", 4.5, 29.532, 35.000, R.drawable.ic_launcher_background, "سياحي", 9000, 0.0),
            NearbyLocation(UUID.randomUUID().toString(), "قلعة العقبة", 4.3, 29.520, 35.005, R.drawable.ic_launcher_background, "تاريخي", 2000, 0.0)
        )

        // سنقوم ببناء القائمة الكاملة (يمكنك إكمال الباقي بنفس النمط)
        // سأعتمد على قراءة الـ GeoJSON للنقاط، ودمجها مع البيانات الوصفية هنا

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val resourceId = context.resources.getIdentifier("jordan_governorates", "raw", context.packageName)

            if (resourceId != 0) {
                val inputStream = context.resources.openRawResource(resourceId)
                val jsonString = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                val parsedPoints = parseGeoJsonPoints(jsonString) // دالة مساعدة تجلب النقاط حسب الاسم أو الترتيب

                // دمج البيانات
                val fullGovernorates = listOf(
                    createGov(0, "العاصمة (عمان)", R.drawable.ic_launcher_background,
                        "عمان هي عاصمة المملكة الأردنية الهاشمية ومركزها التجاري والإداري.",
                        "يعود تاريخ عمان إلى آلاف السنين، عرفت باسم 'ربة عمون' ثم 'فيلادلفيا' في العهد الروماني.",
                        ammanLocs, listOf("مهرجان صيف عمان", "ليالي القلعة"), parsedPoints),

                    createGov(1, "إربد", R.drawable.ic_launcher_background,
                        "عروس الشمال وثاني أكبر مدن الأردن من حيث السكان.",
                        "تشتهر بسهولها الخصبة وتاريخها العريق الذي يضم مدناً مثل أم قيس (جدارا).",
                        listOf(NearbyLocation(UUID.randomUUID().toString(), "أم قيس", 4.7, 32.65, 35.68, R.drawable.ic_launcher_background, "تاريخي", 3000, 0.0)),
                        listOf("مهرجان القمح والزيتون"), parsedPoints),

                    createGov(2, "الزرقاء", R.drawable.ic_launcher_background, "مدينة صناعية وتجارية هامة.", "تاريخها مرتبط بسكة حديد الحجاز والهجرات المتنوعة.", emptyList(), emptyList(), parsedPoints),
                    createGov(3, "المفرق", R.drawable.ic_launcher_background, "بوابة الأردن الشرقية.", "تضم آثاراً نبطية ورومانية وكنائس بيزنطية.", emptyList(), emptyList(), parsedPoints),

                    createGov(4, "عجلون", R.drawable.ic_launcher_background,
                        "تتميز بطبيعتها الخضراء وغاباتها الكثيفة.",
                        "تشتهر بقلعة عجلون التي بناها عز الدين أسامة أحد قادة صلاح الدين الأيوبي.",
                        listOf(NearbyLocation(UUID.randomUUID().toString(), "قلعة عجلون", 4.8, 32.32, 35.72, R.drawable.ic_launcher_background, "تاريخي", 5000, 0.0)),
                        listOf("مهرجان عجلون السياحي"), parsedPoints),

                    createGov(5, "جرش", R.drawable.ic_launcher_background,
                        "مدينة الألف عمود، واحدة من أكبر المدن الرومانية المحفوظة في العالم.",
                        "تأسست في عهد الإسكندر الأكبر وازدهرت في العصر الروماني.",
                        jerashLocs, listOf("مهرجان جرش للثقافة والفنون"), parsedPoints),

                    createGov(6, "مادبا", R.drawable.ic_launcher_background, "مدينة الفسيفساء.", "تشتهر بخارطة مادبا الفسيفسائية وجبل نيبو.", emptyList(), emptyList(), parsedPoints),
                    createGov(7, "البلقاء", R.drawable.ic_launcher_background, "مركزها السلط، مدينة التراث العالمي.", "تاريخ عريق يجمع بين التراث المعماري الأصفر والطبيعة الخلابة.", emptyList(), emptyList(), parsedPoints),
                    createGov(8, "الكرك", R.drawable.ic_launcher_background, "مدينة القلعة الحصينة.", "شهدت حضارات المؤابيين والأنباط والصليبيين والأيوبيين.", emptyList(), emptyList(), parsedPoints),
                    createGov(9, "الطفيلة", R.drawable.ic_launcher_background, "الهاشمية، وتضم محمية ضانا.", "تاريخها يعود للأدوميين وتتميز بتضاريسها المتنوعة.", emptyList(), emptyList(), parsedPoints),

                    createGov(10, "معان", R.drawable.ic_launcher_background,
                        "أكبر محافظات الأردن مساحة، وتضم البتراء ووادي رم.",
                        "كانت ممراً للقوافل التجارية ومركزاً للحضارة النبطية.",
                        listOf(
                            NearbyLocation(UUID.randomUUID().toString(), "البتراء", 5.0, 30.32, 35.44, R.drawable.ic_launcher_background, "عجائب الدنيا", 10000, 0.0),
                            NearbyLocation(UUID.randomUUID().toString(), "وادي رم", 4.9, 29.57, 35.42, R.drawable.ic_launcher_background, "طبيعة", 8000, 0.0)
                        ),
                        listOf("مهرجان البتراء السياحي"), parsedPoints),

                    createGov(11, "العقبة", R.drawable.ic_launcher_background,
                        "ثغر الأردن الباسم ومنفذه البحري الوحيد.",
                        "تاريخها يعود لآلاف السنين كمنطقة تجارية هامة بين القارات.",
                        aqabaLocs, listOf("مهرجان العقبة للفنون التقليدية"), parsedPoints)
                )

                withContext(Dispatchers.Main) {
                    _governorates.value = fullGovernorates
                }
            }
        }
    }

    private fun createGov(index: Int, name: String, img: Int, desc: String, hist: String, locs: List<NearbyLocation>, events: List<String>, allPoints: List<List<LatLng>>): Governorate {
        val points = if (index < allPoints.size) allPoints[index] else emptyList()
        val center = if (points.isNotEmpty()) calculateCentroid(points) else LatLng(31.0, 36.0)

        return Governorate(
            id = index.toString(),
            name = name,
            imageRes = img,
            description = desc,
            history = hist,
            bestLocations = locs,
            events = events,
            center = center,
            defaultZoom = 10f,
            color = govColors[index % govColors.size],
            points = points
        )
    }

    // دالة مساعدة بسيطة لفك JSON وإرجاع قائمة النقاط (مبسطة)
    private fun parseGeoJsonPoints(json: String): List<List<LatLng>> {
        val allPolygons = mutableListOf<List<LatLng>>()
        try {
            val root = JSONObject(json)
            val features = root.getJSONArray("features")
            for (i in 0 until features.length()) {
                val geometry = features.getJSONObject(i).getJSONObject("geometry")
                val type = geometry.getString("type")
                val points = mutableListOf<LatLng>()

                if (type == "Polygon") {
                    val coords = geometry.getJSONArray("coordinates").getJSONArray(0)
                    for (j in 0 until coords.length()) {
                        points.add(LatLng(coords.getJSONArray(j).getDouble(1), coords.getJSONArray(j).getDouble(0)))
                    }
                } else if (type == "MultiPolygon") {
                    val coords = geometry.getJSONArray("coordinates").getJSONArray(0).getJSONArray(0)
                    for (j in 0 until coords.length()) {
                        points.add(LatLng(coords.getJSONArray(j).getDouble(1), coords.getJSONArray(j).getDouble(0)))
                    }
                }
                allPolygons.add(points)
            }
        } catch (e: Exception) { e.printStackTrace() }
        return allPolygons
    }

    private fun calculateCentroid(points: List<LatLng>): LatLng {
        var lat = 0.0; var lng = 0.0
        points.forEach { lat += it.latitude; lng += it.longitude }
        return LatLng(lat / points.size, lng / points.size)
    }

    fun onGovernorateSelected(gov: Governorate) { _selectedGovernorate.value = gov }
    fun clearSelectedGovernorate() { _selectedGovernorate.value = null }
}