package com.startogamu.zikobot.core.event.alarm;

import com.startogamu.zikobot.module.zikobot.model.Alarm;

import lombok.Data;

/**
 * Created by josh on 20/06/16.
 */
@Data
public class EventAlarmSelect {
    private final Alarm model;
}
