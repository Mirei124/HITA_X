package com.stupidtree.hitax.ui.widgets.today.normal

import android.app.Application
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.stupidtree.hitax.data.repository.TimetableRepository
import com.stupidtree.hitax.ui.widgets.WidgetUtils.EVENT_REFRESH
import com.stupidtree.hitax.ui.widgets.today.TodayUtils.goAsync
import com.stupidtree.hitax.ui.widgets.today.TodayUtils.setUpOneWidget
import kotlinx.coroutines.DelicateCoroutinesApi
import java.sql.Timestamp
import java.util.Calendar

/**
 * Implementation of App Widget functionality.
 */
class TodayWidget : AppWidgetProvider() {
    companion object {
        const val EVENT_CLICK = "com.stupidtree.hita.WIDGET_EVENT_CLICK"
        const val EVENT_EXTRA = "com.stupidtree.hita.EXTRA_ITEM"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        val timetableRepo =
            TimetableRepository.getInstance(context.applicationContext as Application)
        goAsync{
            val todayStauts = this.todayStatus(timetableRepo)
            for (appWidgetId in appWidgetIds) {
                Log.e("WI", "UPDATE:$appWidgetId")
                setUpOneWidget(context, todayStauts, appWidgetManager, appWidgetId,false)
            }
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        //val appWidgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID)
        when (intent!!.action) {
            EVENT_CLICK -> {
//                Log.e("WI", "click")
//                val bd = intent.extras
//                val position = intent.getStringExtra(EVENT_EXTRA)
//                Toast.makeText(context, "打开...$position", Toast.LENGTH_SHORT).show()
            }
            EVENT_REFRESH -> {
                val cn = ComponentName(context, TodayWidget::class.java)
                val mgr = AppWidgetManager.getInstance(context)
                val timetableRepo =
                    TimetableRepository.getInstance(context.applicationContext as Application)
                goAsync {
                    val todayStatus = this.todayStatus(timetableRepo)
                    for (appWidgetId in mgr.getAppWidgetIds(cn)) {
                        Log.e("WI", "refressh$appWidgetId")
                        setUpOneWidget(context, todayStatus, mgr, appWidgetId,false)
                    }
                }

            }
        }

    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        Log.e("WI", "onDisabled")
        super.onDisabled(context)
    }

    fun todayStatus(timetableRepository: TimetableRepository): Int {
        val events = timetableRepository.getTodayEventsSync()
        if (!events.isEmpty()) {
            var maxTimestamp = events[0].to
            for (event in events) {
                maxTimestamp = if (maxTimestamp > event.to) maxTimestamp else event.to
            }
            val now = Timestamp(Calendar.getInstance().timeInMillis)
            if (maxTimestamp > now) {
                return 0
            }
        }
        val tomorrow = timetableRepository.getTomorrowEventsSync()
        if (tomorrow.isEmpty()) {
            if (events.isEmpty()) {
                return 1
            } else {
                return 2
            }
        } else {
            return 3
        }
    }
}

