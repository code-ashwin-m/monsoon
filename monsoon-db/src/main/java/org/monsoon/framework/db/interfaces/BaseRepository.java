package org.monsoon.framework.db.interfaces;


import java.util.List;

public interface BaseRepository<T> {
    boolean createTableIfNotExists();
    boolean create(T entity);
    boolean createMany(List<T> entities);
    boolean update(T entity);
    boolean updateMany(List<T> entities);
    boolean delete(T entity);
    boolean deleteById(Object id);
    List<T> getAll();
    T getById(Object id);
}