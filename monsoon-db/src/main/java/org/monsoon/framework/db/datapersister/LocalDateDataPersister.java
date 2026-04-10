package org.monsoon.framework.db.datapersister;

import org.monsoon.framework.db.interfaces.DataPersister;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateDataPersister implements DataPersister<LocalDate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public Object javaToSql(Object value) {
        return value == null ? null : ((LocalDate) value).format(FORMATTER);
    }

    @Override
    public LocalDate sqlToJava(Object value) {
        return value == null ? null : LocalDate.parse(String.valueOf(value), FORMATTER);
    }
}