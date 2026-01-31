package org.monsoon.framework.db.datapersister;

import org.monsoon.framework.db.interfaces.DataPersister;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class DataPersisterRegistry {
    private static final Map<Class<?>, DataPersister<?>> registry = new HashMap<>();
    private static final DefaultDataPersister defaultDataPersister = new DefaultDataPersister();

    static {
        register(String.class, defaultDataPersister);
        register(Integer.class, defaultDataPersister);
        register(int.class, defaultDataPersister);
        register(Long.class, defaultDataPersister);
        register(long.class, defaultDataPersister);
        register(Double.class, defaultDataPersister);
        register(double.class, defaultDataPersister);
        register(LocalDateTime.class, new LocalDateTimeDataPersister());
    }

    public static <T> void register(Class<?> clazz, DataPersister<T> convertor) {
        registry.put(clazz, convertor);
    }

    public static <T> DataPersister<T> get(Class<?> clazz){
        return (DataPersister<T>) registry.getOrDefault(clazz, defaultDataPersister);
    }
}
