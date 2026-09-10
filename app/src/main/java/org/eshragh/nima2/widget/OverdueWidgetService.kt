package org.eshragh.nima2.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import kotlinx.coroutines.runBlocking
import org.eshragh.nima2.R
import org.eshragh.nima2.data.local.AppDatabase
import org.eshragh.nima2.data.local.ServerKartablCardEntity
import org.eshragh.nima2.util.JalaliCalendarHelper
import org.eshragh.nima2.util.toPersianDigits
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OverdueWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return OverdueWidgetFactory(this.applicationContext)
    }
}

class OverdueWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var cards: List<ServerKartablCardEntity> = listOf()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val db = AppDatabase.getDatabase(context)
        val allCards = runBlocking { db.serverKartablDao().getAllCachedCardsSync() }
        
        val nowIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
        
        cards = allCards.filter { card ->
            !card.dueDate.isNullOrBlank() && card.dueDate!! < nowIso
        }.sortedByDescending { it.dueDate }
    }

    override fun onDestroy() {
        cards = listOf()
    }

    override fun getCount(): Int = cards.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= cards.size) return RemoteViews(context.packageName, R.layout.widget_overdue_item)

        val card = cards[position]
        val views = RemoteViews(context.packageName, R.layout.widget_overdue_item)

        views.setTextViewText(R.id.card_name, card.name)
        views.setTextViewText(R.id.project_name, card.projectName)

        val jalaliDate = JalaliCalendarHelper.iso8601ToJalali(card.dueDate)
        val dateText = jalaliDate?.toPersianDisplay()?.toPersianDigits() ?: ""
        views.setTextViewText(R.id.due_date, dateText)

        // Fill in the fill-in intent for the item click
        val fillInIntent = Intent()
        // You could add extras here if you want to open a specific card
        // fillInIntent.putExtra("card_id", card.id)
        views.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}
