package org.monsoon.framework.db.datapersister;

import org.monsoon.framework.db.interfaces.DataPersister;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeDataPersister implements DataPersister<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public Object javaToSql(Object value) {
        return value == null ? null : ((LocalDateTime) value).format(FORMATTER);
    }

    @Override
    public LocalDateTime sqlToJava(Object value) {
        return value == null ? null : LocalDateTime.parse(value.toString(), FORMATTER);
    }
}
