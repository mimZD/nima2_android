package org.eshragh.nima2.util

import ir.huri.jcal.JalaliCalendar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.TimeZone

data class JalaliDate(
    val year: Int,
    val month: Int, // 1 to 12
    val day: Int    // 1 to 31
) {
    val monthName: String
        get() = PERSIAN_MONTH_NAMES.getOrElse(month - 1) { "" }

    fun toPersianDisplay(): String {
        return "$day $monthName $year"
    }

    fun toShortPersianDisplay(): String {
        return "$day $monthName"
    }
}

val PERSIAN_MONTH_NAMES = listOf(
    "فروردین", "اردیبهشت", "خرداد",
    "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر",
    "دی", "بهمن", "اسفند"
)

object JalaliCalendarHelper {

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gCal = GregorianCalendar(gy, gm - 1, gd)
        val jCal = JalaliCalendar(gCal)
        return JalaliDate(jCal.year, jCal.month, jCal.day)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Calendar {
        val jCal = JalaliCalendar(jy, jm, jd)
        val gCal = jCal.toGregorian()
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(gCal.get(Calendar.YEAR), gCal.get(Calendar.MONTH), gCal.get(Calendar.DAY_OF_MONTH), 12, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }

    fun currentDateInJalali(): JalaliDate {
        val jCal = JalaliCalendar()
        return JalaliDate(jCal.year, jCal.month, jCal.day)
    }

    fun jalaliToIso8601(jalaliDate: JalaliDate): String {
        val cal = jalaliToGregorian(jalaliDate.year, jalaliDate.month, jalaliDate.day)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(cal.time)
    }

    fun iso8601ToJalali(isoString: String?): JalaliDate? {
        if (isoString.isNullOrBlank()) return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(isoString) ?: return null
            val gCal = GregorianCalendar(TimeZone.getTimeZone("UTC"))
            gCal.time = date
            val jCal = JalaliCalendar(gCal)
            JalaliDate(jCal.year, jCal.month, jCal.day)
        } catch (_: Exception) {
            try {
                val sdf2 = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date2 = sdf2.parse(isoString.take(10)) ?: return null
                val gCal = GregorianCalendar()
                gCal.time = date2
                val jCal = JalaliCalendar(gCal)
                JalaliDate(jCal.year, jCal.month, jCal.day)
            } catch (_: Exception) {
                null
            }
        }
    }
}
