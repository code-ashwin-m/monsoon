# Monsoon Framework - Database Module (`monsoon-db`)

Welcome to the **Monsoon Framework Database Module**. The `monsoon-db` module provides a lightweight, proxy-based Object-Relational Mapping (ORM) and data access framework. It simplifies database interactions by mapping Java objects to database tables, providing a robust repository layer, and handling transactions automatically.

This highly detailed guide covers everything you need to know to configure and use the database capabilities in your Monsoon applications.

---

## Table of Contents

1. [Configuration](#1-configuration)
2. [Entity Mapping](#2-entity-mapping)
3. [The BaseRepository](#3-the-baserepository)
4. [Custom Repositories & Dynamic Queries](#4-custom-repositories--dynamic-queries)
5. [Custom Query Annotations](#5-custom-query-annotations)
6. [Transaction Management](#6-transaction-management)

---

## 1. Configuration

The database module relies on properties defined in your application's configuration file (e.g., `application.properties`). These properties configure the underlying `DataSource`.

### Properties Structure
Define the following properties under the `monsoon.datasource` prefix:

```properties
monsoon.datasource.enabled=true
monsoon.datasource.driver=org.postgresql.Driver
monsoon.datasource.url=jdbc:postgresql://localhost:5432/mydb
monsoon.datasource.username=dbuser
monsoon.datasource.password=dbpass
monsoon.datasource.enforceForeignKeys=true
```

When `enabled=true`, the framework will automatically instantiate a `DataSource` and wire it into the IoC container.

---

## 2. Entity Mapping

To map a Java class to a database table, you use a set of specialized annotations from the `org.monsoon.framework.db.annotations` package.

### Annotations Overview
- `@Entity(tableName = "users")`: Marks the class as a database entity and specifies the table name.
- `@Id`: Marks a field as the Primary Key.
- `@GeneratedId`: Used in conjunction with `@Id` to indicate that the database auto-generates this value (e.g., AUTO_INCREMENT or SERIAL).
- `@Column(name = "db_column_name")`: Explicitly maps a class field to a specific database column. If omitted, the framework typically falls back to the field name.

### Example Entity

```java
import org.monsoon.framework.db.annotations.Entity;
import org.monsoon.framework.db.annotations.Id;
import org.monsoon.framework.db.annotations.GeneratedId;
import org.monsoon.framework.db.annotations.Column;

@Entity(tableName = "users")
public class User {

    @Id
    @GeneratedId
    @Column(name = "id")
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "email_address")
    private String email;

    // Getters and Setters...
}
```

---

## 3. The BaseRepository

The framework provides a generic interface `BaseRepository<T>` that includes all standard CRUD (Create, Read, Update, Delete) operations out of the box.

### Built-in Methods
When you extend `BaseRepository`, your interface automatically inherits the following proxy-implemented methods:

- `boolean createTableIfNotExists()`: Generates and executes a `CREATE TABLE` SQL statement based on the entity annotations.
- `Object create(T entity)`: Inserts a single entity into the database. Returns the generated ID (if applicable).
- `boolean createMany(List<T> entities)`: Batch inserts multiple entities.
- `boolean update(T entity)`: Updates an existing entity (matches by `@Id`).
- `boolean updateMany(List<T> entities)`: Batch updates multiple entities.
- `boolean deleteOne(T entity)`: Deletes the specified entity.
- `boolean deleteMany(List<T> entities)`: Batch deletes multiple entities.
- `List<T> findAll()`: Retrieves all records from the table.
- `T findById(Object id)`: Retrieves a single record by its Primary Key.

---

## 4. Custom Repositories & Dynamic Queries

To interact with an entity, create an interface that extends `BaseRepository<T>` and annotate it with `@Repository`.

### Creating a Repository

```java
import org.monsoon.framework.db.annotations.Repository;
import org.monsoon.framework.db.interfaces.BaseRepository;

@Repository
public interface UserRepository extends BaseRepository<User> {
    // Custom methods go here
}
```

### Dynamic Query Methods
Monsoon DB supports dynamic query generation based on method names. If you define a method starting with `findBy`, the framework will parse the rest of the method name, convert it to a column name, and execute a `SELECT` query.

```java
@Repository
public interface UserRepository extends BaseRepository<User> {
    
    // Executes: SELECT * FROM users WHERE username = ?
    User findByUsername(String username);
    
    // Executes: SELECT * FROM users WHERE email = ?
    User findByEmail(String email);
}
```
*Note: The string following `findBy` is converted so the first character is lowercased to match your entity's field mapping.*

---

## 5. Custom Query Annotations

For more complex queries that cannot be resolved by method name generation, you can use explicit SQL annotations.

### `@Query`, `@Update`, and `@Delete`
You can write raw SQL directly on repository methods. The framework provides a special placeholder `{table}` which is automatically replaced with the table name defined in your `@Entity` annotation.

- `@Query`: For `SELECT` statements. Maps the result set back to the entity type or a List of entities.
- `@Update`: For `UPDATE` statements.
- `@Delete`: For `DELETE` statements.

### Example Custom Queries

```java
import org.monsoon.framework.db.annotations.Query;
import org.monsoon.framework.db.annotations.Update;
import org.monsoon.framework.db.annotations.Delete;

@Repository
public interface UserRepository extends BaseRepository<User> {

    // Custom Select
    @Query("SELECT * FROM {table} WHERE status = 'ACTIVE' AND age > ?")
    List<User> findActiveUsersOlderThan(int age);

    // Custom Update
    @Update("UPDATE {table} SET status = 'INACTIVE' WHERE last_login < ?")
    int deactivateInactiveUsers(long timestamp);

    // Custom Delete
    @Delete("DELETE FROM {table} WHERE status = 'BANNED'")
    boolean deleteBannedUsers();
}
```

---

## 6. Transaction Management

Monsoon DB handles transaction boundaries using the `@Transactional` annotation. This is typically applied at the Service layer to ensure that multiple repository calls succeed or fail atomically.

### Using `@Transactional`
When a method is annotated with `@Transactional`, the `TransactionBeanPostProcessor` wraps the execution in a transaction.
- If the method completes successfully, the transaction is **committed**.
- If the method throws a `RuntimeException` or `Exception`, the transaction is **rolled back**.

```java
import org.monsoon.framework.core.annotations.Service;
import org.monsoon.framework.core.annotations.Autowired;
import org.monsoon.framework.db.annotations.Transactional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private InventoryRepository inventoryRepo;

    @Transactional
    public void processOrder(Order order) {
        // Step 1: Save the order
        orderRepo.create(order);

        // Step 2: Update inventory (if this fails, the order creation is rolled back)
        inventoryRepo.deductStock(order.getProductId(), order.getQuantity());
    }
}
```

---

**End of Documentation**
*This documentation covers the `monsoon-db` package. For core framework mechanics or web capabilities, refer to the `monsoon-core` and `monsoon-web` module documentation respectively.*
