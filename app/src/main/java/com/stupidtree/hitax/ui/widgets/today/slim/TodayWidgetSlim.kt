package com.stupidtree.hitax.ui.widgets.today.slim

import android.app.Application
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.stupidtree.hitax.data.repository.TimetableRepository
import com.stupidtree.hitax.ui.widgets.WidgetUtils.EVENT_REFRESH
import com.stupidtree.hitax.ui.widgets.today.TodayUtils
import com.stupidtree.hitax.ui.widgets.today.TodayUtils.goAsync
import kotlinx.coroutines.DelicateCoroutinesApi
import java.sql.Timestamp
import java.util.Calendar

/**
 * Implementation of App Widget functionality.
 */
@OptIn(DelicateCoroutinesApi::class)
class TodayWidgetSlim : AppWidgetProvider() {
    companion object {
        const val EVENT_CLICK2 = "com.stupidtree.hita.WIDGET_EVENT_CLICK2"
        const val EVENT_EXTRA2 = "com.stupidtree.hita.EXTRA_ITEM2"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val timetableRepo =
            TimetableRepository.getInstance(context.applicationContext as Application)
        goAsync {
            val todayStatus = this.todayStatus(timetableRepo)
            for (appWidgetId in appWidgetIds) {
                //Log.e("WI2", "UPDATE:$appWidgetId")
                TodayUtils.setUpOneWidget(context, todayStatus, appWidgetManager, appWidgetId, true)
            }
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        when (intent!!.action) {
            EVENT_REFRESH -> {
                val cn = ComponentName(context, TodayWidgetSlim::class.java)
                val mgr = AppWidgetManager.getInstance(context)
                val timetableRepo =
                    TimetableRepository.getInstance(context.applicationContext as Application)
                goAsync {
                    val todayStatus = this.todayStatus(timetableRepo)
                    for (appWidgetId in mgr.getAppWidgetIds(cn)) {
                       // Log.e("WI2", "refressh$appWidgetId")
                        TodayUtils.setUpOneWidget(context, todayStatus, mgr, appWidgetId, true)
                    }
                }
            }
        }

    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        //Log.e("WI2", "onDisabled")
        super.onDisabled(context)
    }

    // 0 显示今日课：今日有课且未结束
    // 1 今日空闲： 今日无课且明日无课
    // 2 今日课已上完： 今日有课且已结束且明日无课
    // 3 明日课预告：今日无课或已结束且明日有课
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

