package org.eshragh.nima2.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import org.eshragh.nima2.R
import org.eshragh.nima2.data.local.AppDatabase
import org.eshragh.nima2.data.local.ServerKartablCardEntity
import org.eshragh.nima2.util.JalaliCalendarHelper
import org.eshragh.nima2.util.toPersianDigits
import java.text.SimpleDateFormat
import java.util.*

class OverdueWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return OverdueRemoteViewsFactory(this.applicationContext)
    }
}

class OverdueRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var items: List<ServerKartablCardEntity> = emptyList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val db = AppDatabase.getDatabase(context)
        val allCards = kotlinx.coroutines.runBlocking {
            db.serverKartablDao().getAllCachedCardsSync()
        }
        
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        items = allCards.filter { 
            !it.dueDate.isNullOrBlank() && it.dueDate!! < nowIso 
        }.sortedBy { it.dueDate }
    }

    override fun onDestroy() {}

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        val item = items[position]
        val views = RemoteViews(context.packageName, R.layout.widget_overdue_item)
        
        views.setTextViewText(R.id.card_name, item.name)
        views.setTextViewText(R.id.project_name, item.projectName)
        
        val jalali = JalaliCalendarHelper.iso8601ToJalali(item.dueDate)
        val dateText = jalali?.toShortPersianDisplay()?.toPersianDigits() ?: ""
        views.setTextViewText(R.id.due_date, dateText)

        val fillInIntent = Intent()
        views.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)
        
        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}
