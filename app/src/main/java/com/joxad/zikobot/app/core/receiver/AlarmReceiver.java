package com.joxad.zikobot.app.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.joxad.zikobot.app.alarm.AlarmManager;
import com.joxad.zikobot.app.core.fragmentmanager.IntentManager;
import com.joxad.zikobot.app.core.utils.EXTRA;
import com.orhanobut.logger.Logger;

/**
 * Created by josh on 28/03/16.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long alarmId = intent.getLongExtra(EXTRA.ALARM_ID, -1);
        Logger.d("AlarmReceiver" + alarmId);
        AlarmManager.getAlarmById(alarmId).subscribe((alarm) -> {
            AlarmManager.prepareAlarm(context, alarm);
            if (AlarmManager.canStart(alarm)) {
                Logger.d("AlarmReceiver" + alarm.getName());
                if (alarm.getRepeated() == 0) {
                    alarm.setActive(0);
                    alarm.save();
                }
                context.startActivity(IntentManager.goToWakeUp(alarm));
            }
        }, throwable -> {
            Logger.d(throwable.getMessage());
        });

    }
}

