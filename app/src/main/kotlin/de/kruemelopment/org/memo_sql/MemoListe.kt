package de.kruemelopment.org.memo_sql

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

class MemoListe : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val rv = RemoteViews(context.packageName, R.layout.widgetview)
            val intent = Intent(context, WidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)))
            rv.setRemoteAdapter(R.id.listewidget, intent)
            val clickIntent = Intent(context, MainActivity::class.java)
            val clickPI = PendingIntent.getActivity(
                context, 0,
                clickIntent, PendingIntent.FLAG_IMMUTABLE
            )
            rv.setOnClickPendingIntent(R.id.textView20, clickPI)
            rv.setOnClickPendingIntent(R.id.textView30, clickPI)
            rv.setOnClickPendingIntent(R.id.textView40, clickPI)
            rv.setOnClickPendingIntent(R.id.textView50, clickPI)
            rv.setOnClickPendingIntent(R.id.tallayout0, clickPI)
            rv.setPendingIntentTemplate(R.id.listewidget, clickPI)
            appWidgetManager.updateAppWidget(appWidgetId, rv)
        }
    }
}
