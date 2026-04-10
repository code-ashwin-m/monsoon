package org.monsoon.framework.db.datapersister;

import org.monsoon.framework.db.interfaces.DataPersister;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeDataPersister implements DataPersister<LocalTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public Object javaToSql(Object value) {
        return value == null ? null : ((LocalTime) value).format(FORMATTER);
    }

    @Override
    public LocalTime sqlToJava(Object value) {
        return value == null ? null : LocalTime.parse(String.valueOf(value), FORMATTER);
    }
}