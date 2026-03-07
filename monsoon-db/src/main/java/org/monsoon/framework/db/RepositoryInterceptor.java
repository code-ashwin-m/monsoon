package org.monsoon.framework.db;

import org.monsoon.framework.db.actions.CreateRecord;
import org.monsoon.framework.db.actions.CreateTable;
import org.monsoon.framework.db.actions.DeleteRecord;
import org.monsoon.framework.db.actions.ReadRecord;
import org.monsoon.framework.db.actions.UpdateRecord;
import org.monsoon.framework.db.annotations.Delete;
import org.monsoon.framework.db.annotations.Query;
import org.monsoon.framework.db.annotations.Transactional;
import org.monsoon.framework.db.annotations.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;


public class RepositoryInterceptor implements InvocationHandler {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryInterceptor.class);
    private final Class<?> clazz;
    private final EntityMeta meta;
    private final DataSourceProperty dataSource;

    public RepositoryInterceptor(Class<?> clazz, EntityMeta meta, DataSourceProperty dataSource) {
        this.clazz = clazz;
        this.meta = meta;
        this.dataSource = dataSource;
    }

    public static Object create(Class<?> clazz, EntityMeta meta, DataSourceProperty dataSource) {
        Object object = Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[] { clazz },
                new RepositoryProxy(clazz, meta, dataSource)
        );
        return object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Connection conn = ConnectionFactory.getConnection(dataSource);
        return executeRepositoryMethod(conn, method, args);
    }

    private Object executeRepositoryMethod(Connection conn, Method method, Object[] args) throws Exception {
        String methodName = method.getName();

        if (method.isAnnotationPresent(Query.class)){
            String sql = method.getAnnotation(Query.class).value();
            sql = sql.replaceAll("\\{table}", meta.getTableName());
            return ReadRecord.execute(conn, meta, sql, args, method.getReturnType());
        } else if (method.isAnnotationPresent(Update.class)) {
            String sql = method.getAnnotation(Update.class).value();
            sql = sql.replaceAll("\\{table}", meta.getTableName());
            return UpdateRecord.execute(conn, sql, args);
        } else if (method.isAnnotationPresent(Delete.class)) {
            String sql = method.getAnnotation(Delete.class).value();
            sql = sql.replaceAll("\\{table}", meta.getTableName());
            return DeleteRecord.execute(conn, sql, args);
        }


        if (methodName.equals("createTableIfNotExists")){
            return CreateTable.createTableIfNotExists(conn, meta);
        } else if (methodName.equals("create")) {
            return CreateRecord.createOne(conn, meta, args[0]);
        } else if (methodName.equals("createMany")) {
            return CreateRecord.createMany(conn, meta, (List) args[0]);
        } else if (methodName.equals("update")) {
            return UpdateRecord.updateOne(conn, meta, args[0]);
        } else if (methodName.equals("updateMany")) {
            return UpdateRecord.updateMany(conn, meta, (List) args[0]);
        } else if (methodName.equals("deleteOne")) {
            return DeleteRecord.deleteOne(conn, meta, args[0]);
        } else if (methodName.equals("deleteMany")) {
            return DeleteRecord.deleteMany(conn, meta, (List) args[0]);
        } else if (methodName.equals("findAll")) {
            return ReadRecord.findAll(conn, meta);
        } else if (methodName.equals("findById")) {
            return ReadRecord.findById(conn, meta, args[0]);
        } else if (methodName.startsWith("findBy")) {
            String column = methodName.substring("findBy".length());
            column = Character.toLowerCase(column.charAt(0)) + column.substring(1);
            return ReadRecord.findByColumn(conn, meta, column, args[0]);
        }
        logger.error("Method not supported: {}", methodName);
        throw new UnsupportedOperationException("Method not supported: " + methodName);
    }
}
