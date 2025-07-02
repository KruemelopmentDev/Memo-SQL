package de.kruemelopment.org.memo_sql

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class WidgeteinzelnesMemo : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        private var aBoolean = false
        fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val sp7 = context.getSharedPreferences("Widget", 0)
            val was = sp7.getString("single$appWidgetId", "")
            val views = RemoteViews(context.packageName, R.layout.widgeteinzelnes_memo)
            val myDB = DataBaseHelper(context)
            val res = myDB.allData
            if (res.count > 0) {
                while (res.moveToNext()) {
                    if (res.getString(0) == was) {
                        views.setTextViewText(R.id.textView201, res.getString(1))
                        views.setTextViewText(R.id.textView301, res.getString(3))
                        views.setTextViewText(R.id.textView401, res.getString(2))
                        views.setTextViewText(R.id.textView501, res.getString(4))
                        val clickIntent = Intent(context, MainActivity::class.java)
                        clickIntent.putExtra("Hallo", res.getString(0))
                        val clickPI = PendingIntent.getActivity(
                            context, 0,
                            clickIntent, PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widgetlayout, clickPI)
                        val edit = Intent(context, WidgetMemoBearbeiten::class.java)
                        edit.putExtra("id", res.getString(0))
                        edit.putExtra("titel", res.getString(1))
                        edit.putExtra("thema", res.getString(2))
                        edit.putExtra("inhalt", res.getString(3))
                        edit.putExtra("passwort", res.getString(6))
                        val editclick = PendingIntent.getActivity(
                            context, 0,
                            edit, PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.imageView10, editclick)
                        views.setImageViewResource(R.id.imageView10, R.drawable.pencil_outline)
                        views.setImageViewResource(R.id.imageView9, R.drawable.settings)
                        aBoolean = true
                    }
                }
            }
            if (!aBoolean) {
                views.setTextViewText(R.id.textView501, context.getString(R.string.deletedmemo))
                val clickIntent = Intent(context, MainActivity::class.java)
                val clickPI = PendingIntent.getActivity(
                    context, 0,
                    clickIntent, PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widgetlayout, clickPI)
            }
            aBoolean = false
            val configIntent = Intent(context, WidgeteinzelnesMemoConfigureActivity::class.java)
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            val configPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                configIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.imageView9, configPendingIntent)
            appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views)
        }
    }
}