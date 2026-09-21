package org.eshragh.nima2.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.eshragh.nima2.R
import org.eshragh.nima2.data.local.AppDatabase
import org.eshragh.nima2.data.local.CachedFullListCardEntity
import org.eshragh.nima2.data.pref.UserPreferencesRepository

class ActivePathWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ActivePathRemoteViewsFactory(this.applicationContext)
    }
}

class ActivePathRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var items: List<CachedFullListCardEntity> = emptyList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val userPrefs = UserPreferencesRepository(context)
        val db = AppDatabase.getDatabase(context)
        
        runBlocking {
            val savedTarget = userPrefs.viewListSelectedTarget.firstOrNull()
            val listId = savedTarget?.listId
            
            if (listId != null) {
                items = db.metadataDao().getFullListCardsSync(listId)
            } else {
                items = emptyList()
            }
        }
    }

    override fun onDestroy() {}

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews {
        if (position >= items.size) return RemoteViews(context.packageName, R.layout.widget_active_path_item)
        
        val item = items[position]
        val views = RemoteViews(context.packageName, R.layout.widget_active_path_item)
        
        views.setTextViewText(R.id.card_name, item.name)

        val fillInIntent = Intent()
        views.setOnClickFillInIntent(R.id.widget_item_container, fillInIntent)
        
        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}
