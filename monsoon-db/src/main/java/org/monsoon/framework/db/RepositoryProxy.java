package org.monsoon.framework.db;

import org.monsoon.framework.db.helper.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.List;

public class RepositoryProxy implements InvocationHandler {

    private final Class<?> clazz;
    private final EntityMeta meta;
    private final DatabaseConfiguration dbConfig;

    public RepositoryProxy(Class<?> clazz, EntityMeta meta, DatabaseConfiguration dbConfig) {
        this.clazz = clazz;
        this.meta = meta;
        this.dbConfig = dbConfig;
    }

    public static Object create(Class<?> clazz, EntityMeta meta, DatabaseConfiguration dbConfig) {
        Object object = Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[] { clazz },
                new RepositoryProxy(clazz, meta, dbConfig)
        );
        return object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Connection conn = ConnectionFactory.getConnection(dbConfig);

        String methodName = method.getName();
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
        throw new UnsupportedOperationException("Method not supported: " + methodName);
    }
}
