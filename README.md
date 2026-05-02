# Monsoon Framework

A lightweight Java framework designed to help developers build modern web applications with minimal setup. It comes bundled with built-in server support, integrated database configuration, and a streamlined development model—allowing you to focus on business logic instead of boilerplate.

The framework emphasizes simplicity, modularity, and scalability. It provides a clean structure for developing RESTful services, UI-driven applications, and backend systems, while remaining flexible enough to adapt to custom requirements.

Ideal for rapid prototyping as well as production-grade applications, it reduces complexity without sacrificing control.

This comprehensive guide covers all three main modules of the framework: `monsoon-core`, `monsoon-db`, and `monsoon-web`.

---

# 1. Monsoon Framework - Core Module (`monsoon-core`)

Welcome to the **Monsoon Framework Core Module**. Monsoon is a lightweight, Java-based Inversion of Control (IoC) and Dependency Injection (DI) framework. The core module provides the foundational infrastructure for application bootstrapping, bean management, property injection, and robust auto-configuration capabilities.

## 1.1 Application Bootstrapping

To start a Monsoon application, you need a main entry point class. The framework uses the `Monsoon.run()` method to initialize the `ApplicationContext`, scan for components, and instantiate singletons.

### The Entry Point
Create a main class and annotate it with `@MonsoonApplication` or `@ComponentScan`. 

```java
package com.example.myapp;

import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.ComponentScan;

@ComponentScan // Scans the current package and sub-packages
public class Application {
    public static void main(String[] args) throws Exception {
        Monsoon.run(Application.class, args);
    }
}
```

**What happens during `Monsoon.run()`?**
1. The framework identifies if the environment is a CLI app or a Web app (by checking for `ApplicationContextFromWebClass`).
2. It initializes the `ApplicationContext`.
3. It performs classpath scanning to find managed components.
4. It executes the Auto-Configuration engine.
5. It instantiates beans and resolves their dependencies.

## 1.2 Stereotype Annotations & Component Scanning

Monsoon manages the lifecycle of your objects (Beans). To tell Monsoon to manage a class, use one of the stereotype annotations.

### Core Stereotypes
- `@Component`: The generic annotation for any framework-managed class.
- `@Service`: A specialization of `@Component` used to indicate that a class holds business logic.
- `@Controller` / `@RestController`: Specialized components used primarily by the `monsoon-web` module to handle HTTP requests.

### Component Scanning
The `@ComponentScan` annotation tells the framework where to look for the above stereotypes. By default, placing it on your main class will scan the package of the main class and all its sub-packages.

## 1.3 Dependency Injection

Monsoon uses the `@Autowired` annotation to perform Dependency Injection (DI). The framework will automatically resolve and inject the required beans at runtime.

### Field Injection
Simply annotate a field with `@Autowired`. The framework will reflectively set the field after the bean is instantiated.

```java
import org.monsoon.framework.core.annotations.Autowired;
import org.monsoon.framework.core.annotations.Service;

@Service
public class OrderService {
    
    @Autowired
    private UserService userService;

    public void processOrder() {
        userService.createUser();
    }
}
```

## 1.4 Java-based Configuration

Instead of relying solely on component scanning, you can explicitly define beans using Java configuration classes.

### `@Configuration` and `@Bean`
Annotate a class with `@Configuration` to mark it as a source of bean definitions. Inside this class, annotate methods with `@Bean`. The framework will call these methods and register the returned objects as beans in the `ApplicationContext`.

```java
import org.monsoon.framework.core.annotations.Configuration;
import org.monsoon.framework.core.annotations.Bean;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## 1.5 Properties & Environment

Monsoon allows you to externalize your configuration using properties files.

### `@Property`
You can inject values from your application's properties directly into bean fields using the `@Property` annotation.

```java
import org.monsoon.framework.core.annotations.Component;
import org.monsoon.framework.core.annotations.Property;

@Component
public class DatabaseConfig {

    @Property("db.url")
    private String databaseUrl;

    @Property("db.timeout")
    private int timeout;
}
```

## 1.6 Auto-Configuration System

One of Monsoon's most powerful features is its Auto-Configuration engine, which allows the framework (and external libraries) to automatically configure beans based on the classpath environment.

### Enabling Auto-Configuration
To use auto-configuration, use the `@EnableAutoConfiguration` annotation.

### The `imports` File
Auto-configuration classes are not discovered via standard component scanning. Instead, they must be explicitly registered.
Create a file at `META-INF/org.monsoon.framework.core.AutoConfiguration.imports` in your resources folder. Add the fully qualified names of your auto-configuration classes, one per line.

## 1.7 Conditional Bean Creation

To make auto-configuration smart, Monsoon provides a suite of `@Conditional` annotations. These allow beans or entire configuration classes to be registered *only* if specific conditions are met.

### Classpath Conditions
- `@ConditionalOnClass(Target.class)`
- `@ConditionalOnMissingClass(Target.class)`

### Bean Conditions
- `@ConditionalOnMissingBean(Target.class)`: Registers the bean only if no other bean of type `Target.class` has been registered yet.

### Property Conditions
- `@ConditionalOnProperty(name = "feature.enabled", havingValue = "true")`

## 1.8 The Application Context

The `ApplicationContext` is the heart of the framework. It holds all bean definitions and singletons.

### Accessing the Context
You can retrieve the context at any time using the static `getInstance()` or `getContext()` methods on the `Monsoon` class.

---

# 2. Monsoon Framework - Database Module (`monsoon-db`)

Welcome to the **Monsoon Framework Database Module**. The `monsoon-db` module provides a lightweight, proxy-based Object-Relational Mapping (ORM) and data access framework. It simplifies database interactions by mapping Java objects to database tables, providing a robust repository layer, and handling transactions automatically.

## 2.1 Configuration

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

## 2.2 Entity Mapping

To map a Java class to a database table, you use a set of specialized annotations from the `org.monsoon.framework.db.annotations` package.

### Annotations Overview
- `@Entity(tableName = "users")`: Marks the class as a database entity and specifies the table name.
- `@Id`: Marks a field as the Primary Key.
- `@GeneratedId`: Used in conjunction with `@Id` to indicate that the database auto-generates this value (e.g., AUTO_INCREMENT or SERIAL).
- `@Column(name = "db_column_name")`: Explicitly maps a class field to a specific database column.

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

    // Getters and Setters...
}
```

## 2.3 The BaseRepository

The framework provides a generic interface `BaseRepository<T>` that includes all standard CRUD operations out of the box.

### Built-in Methods
- `boolean createTableIfNotExists()`
- `Object create(T entity)`
- `boolean createMany(List<T> entities)`
- `boolean update(T entity)`
- `boolean updateMany(List<T> entities)`
- `boolean deleteOne(T entity)`
- `boolean deleteMany(List<T> entities)`
- `List<T> findAll()`
- `T findById(Object id)`

## 2.4 Custom Repositories & Dynamic Queries

To interact with an entity, create an interface that extends `BaseRepository<T>` and annotate it with `@Repository`.

### Dynamic Query Methods
Monsoon DB supports dynamic query generation based on method names. 

```java
@Repository
public interface UserRepository extends BaseRepository<User> {
    
    // Executes: SELECT * FROM users WHERE username = ?
    User findByUsername(String username);
}
```

## 2.5 Custom Query Annotations

For more complex queries, use explicit SQL annotations: `@Query`, `@Update`, and `@Delete`. The framework provides a special placeholder `{table}` which is automatically replaced with the table name.

```java
@Repository
public interface UserRepository extends BaseRepository<User> {

    @Query("SELECT * FROM {table} WHERE status = 'ACTIVE' AND age > ?")
    List<User> findActiveUsersOlderThan(int age);
}
```

## 2.6 Transaction Management

Monsoon DB handles transaction boundaries using the `@Transactional` annotation. This is typically applied at the Service layer to ensure that multiple repository calls succeed or fail atomically.

---

# 3. Monsoon Framework - Web Module (`monsoon-web`)

Welcome to the **Monsoon Framework Web Module**. The `monsoon-web` package is a lightweight, Servlet-based web MVC and REST framework. It provides robust capabilities for HTTP request routing, static resource serving, request interception, and automatic serialization/deserialization for REST APIs.

## 3.1 Web Context Initialization

When you start your application using `Monsoon.run()`, the framework automatically detects the presence of the `monsoon-web` module and initializes an `ApplicationContextFromWebClass`. 

This context registers the core `Dispatcher` and the `ServletWebAdapter`, which act as the bridge between the standard Java Servlet container (like Tomcat or Jetty) and the Monsoon framework.

## 3.2 Controllers and Routing

Monsoon maps incoming HTTP requests to specific methods using stereotype annotations.

### `@Controller` vs `@RestController`
- `@Controller`: Used primarily for returning views (HTML templates).
- `@RestController`: A convenience annotation that combines `@Controller` and `@ResponseBody`. Every method in a `@RestController` automatically serializes its return value to JSON.

### `@RequestMapping`
Use the `@RequestMapping` annotation on your class methods to specify the HTTP method and the URI path.

```java
import org.monsoon.framework.web.annotations.RestController;
import org.monsoon.framework.web.annotations.RequestMapping;

@RestController
public class UserController {

    @RequestMapping(path = "/api/users", method = "GET")
    public List<User> getAllUsers() {
        return userService.findAll();
    }
}
```

## 3.3 Request Parameters & Payload Binding

The `Dispatcher` dynamically binds HTTP request data to your controller method parameters.

### `@PathVariable`
Extracts values directly from the URI path. 

```java
@RequestMapping(path = "/api/users/{id}", method = "GET")
public User getUserById(@PathVariable("id") Long id) {
    return userService.findById(id);
}
```

### `@QueryParam`
Extracts values from the query string (e.g., `/api/users?type=admin`).

### `@RequestBody`
Reads the HTTP request body and deserializes it into a Java object using the configured `HttpMessageConverter`.

## 3.4 Response Handling & Views

The return type of your controller method dictates the response body.

### JSON Responses (`@ResponseBody`)
If a method is annotated with `@ResponseBody` (or if the class is annotated with `@RestController`), the `Dispatcher` will convert it to a JSON string.

### View Rendering (HTML)
If you are returning HTML views, accept a `ModelMap` in your method parameters, add data to it, and return the name of the view as a `String`.

```java
@Controller
public class HomeController {

    @RequestMapping(path = "/home", method = "GET")
    public String renderHomePage(ModelMap model) {
        model.addAttribute("title", "Welcome to Monsoon");
        return "index"; // The ViewRenderer will resolve this to a template
    }
}
```

## 3.5 Static Resources Serving

The `Dispatcher` includes built-in support for serving static files directly from the classpath.
If an incoming request does not match any `@RequestMapping` route, the Dispatcher attempts to find a matching file in the following classpath directories:

1. `/static`
2. `/public`
3. `/META-INF/static`
4. `/META-INF/public`

MIME types are automatically detected and applied based on the file extension.

## 3.6 Interceptors & Filters

Monsoon allows you to intercept HTTP requests and add pre- or post-processing logic.

### `HandlerInterceptor`
Implement the `HandlerInterceptor` interface to wrap controller execution. Useful for authentication checks.

### `WebConfigurer`
To register your interceptors and Servlet filters, create a configuration class that implements `WebConfigurer` and override the registry methods.

## 3.7 Pluggable Converters & Renderers

The framework is designed to be extensible. By default, it uses auto-configured Jackson and FreeMarker components.

### `HttpMessageConverter`
Responsible for JSON serialization/deserialization. You can override it by defining a custom Bean.

### `ViewRenderer`
Responsible for rendering HTML templates. You can provide your own implementation of the `ViewRenderer` interface by exposing it as a Bean.
