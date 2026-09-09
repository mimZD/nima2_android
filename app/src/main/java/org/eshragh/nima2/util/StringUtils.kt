package org.eshragh.nima2.util

fun String.toPersianDigits(): String {
    var result = this
    val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
    for (i in 0..9) {
        result = result.replace(i.toString(), persianDigits[i])
    }
    return result
}

fun Int.toPersianDigits(): String = this.toString().toPersianDigits()
