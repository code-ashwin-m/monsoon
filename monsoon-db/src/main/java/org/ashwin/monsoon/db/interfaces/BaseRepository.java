package org.ashwin.monsoon.db.interfaces;

import java.util.List;

public interface BaseRepository<T> {
    boolean createTableIfNotExists();
    Object create(T entity);
    boolean update(T entity);
    boolean delete(T entity);
    boolean deleteById(Object id);
    List<T> getAll();
    T getById(Object id);
}
