package org.eshragh.nima2.ui.composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gmail.hamedvakhide.compose_jalali_datepicker.JalaliDatePickerDialog
import ir.huri.jcal.JalaliCalendar
import org.eshragh.nima2.ui.theme.AnjomanMax
import org.eshragh.nima2.util.JalaliCalendarHelper
import java.util.Calendar
import java.util.GregorianCalendar

@Composable
fun PersianDatePickerDialog(
    initialIsoDate: String?,
    onDateSelected: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    // Exact same method as Kartext: JalaliCalendar(GregorianCalendar()) for TODAY's date
    val initialCalendar = remember(initialIsoDate) {
        if (!initialIsoDate.isNullOrBlank()) {
            val j = JalaliCalendarHelper.iso8601ToJalali(initialIsoDate)
            if (j != null) {
                val gCal = JalaliCalendarHelper.jalaliToGregorian(j.year, j.month, j.day)
                val cal = GregorianCalendar()
                cal.time = gCal.time
                JalaliCalendar(cal)
            } else {
                JalaliCalendar(GregorianCalendar())
            }
        } else {
            JalaliCalendar(GregorianCalendar()) // TODAY's date (exact same as Kartext)
        }
    }

    val openDialogState = remember { mutableStateOf(true) }

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF2B2B2B) else Color.White
    val contentColor = if (isDark) Color.White else Color.Black

    if (openDialogState.value) {
        key(initialCalendar) {
            Dialog(
                onDismissRequest = {
                    openDialogState.value = false
                    onDismiss()
                },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    JalaliDatePickerDialog(
                        openDialog = openDialogState,
                        fontFamily = AnjomanMax,
                        backgroundColor = backgroundColor,
                        textColor = contentColor,
                        initialDate = initialCalendar,
                        onSelectDay = { _ -> },
                        onConfirm = { jalaliCal: JalaliCalendar ->
                            val gCal = jalaliCal.toGregorian()
                            gCal.set(Calendar.HOUR_OF_DAY, 12)
                            gCal.set(Calendar.MINUTE, 0)
                            gCal.set(Calendar.SECOND, 0)
                            gCal.set(Calendar.MILLISECOND, 0)

                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                            val iso = sdf.format(gCal.time)

                            onDateSelected(iso)
                            openDialogState.value = false
                            onDismiss()
                        }
                    )
                }
            }
        }
    } else {
        onDismiss()
    }
}
