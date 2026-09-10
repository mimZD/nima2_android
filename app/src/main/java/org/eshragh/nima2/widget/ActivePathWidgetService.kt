package org.eshragh.nima2.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.eshragh.nima2.R
import org.eshragh.nima2.data.local.AppDatabase
import org.eshragh.nima2.data.local.CachedFullListCardEntity
import org.eshragh.nima2.data.pref.UserPreferencesRepository

class ActivePathWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ActivePathWidgetFactory(this.applicationContext)
    }
}

class ActivePathWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var cards: List<CachedFullListCardEntity> = listOf()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val db = AppDatabase.getDatabase(context)
        val prefs = UserPreferencesRepository(context)
        
        val listId = runBlocking { 
            prefs.viewListSelectedTarget.first().listId 
        }

        if (listId != null) {
            cards = runBlocking { 
                db.metadataDao().getFullListCardsSync(listId) 
            }
        } else {
            cards = listOf()
        }
    }

    override fun onDestroy() {
        cards = listOf()
    }

    override fun getCount(): Int = cards.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= cards.size) return RemoteViews(context.packageName, R.layout.widget_active_path_item)

        val card = cards[position]
        val views = RemoteViews(context.packageName, R.layout.widget_active_path_item)

        views.setTextViewText(R.id.card_name, card.name)

        val fillInIntent = Intent()
        views.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}
