package org.monsoon.framework.db.interfaces;


import java.util.List;

public interface BaseRepository<T> {
    boolean createTableIfNotExists();
    boolean create(T entity);
    boolean createMany(List<T> entities);
    boolean update(T entity);
    boolean updateMany(List<T> entities);
    boolean deleteOne(T entity);
    boolean deleteMany(List<T> entities);
    List<T> findAll();
    T findById(Object id);
}