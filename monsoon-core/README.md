# Monsoon Framework - Core Module (`monsoon-core`)

Welcome to the **Monsoon Framework Core Module**. Monsoon is a lightweight, Java-based Inversion of Control (IoC) and Dependency Injection (DI) framework. The core module provides the foundational infrastructure for application bootstrapping, bean management, property injection, and robust auto-configuration capabilities.

This guide provides highly detailed, point-by-point documentation on how to use the framework, covering its features from basic bootstrapping to advanced auto-configuration.

---

## Table of Contents

1. [Application Bootstrapping](#1-application-bootstrapping)
2. [Stereotype Annotations & Component Scanning](#2-stereotype-annotations--component-scanning)
3. [Dependency Injection](#3-dependency-injection)
4. [Java-based Configuration](#4-java-based-configuration)
5. [Properties & Environment](#5-properties--environment)
6. [Auto-Configuration System](#6-auto-configuration-system)
7. [Conditional Bean Creation](#7-conditional-bean-creation)
8. [The Application Context](#8-the-application-context)

---

## 1. Application Bootstrapping

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

---

## 2. Stereotype Annotations & Component Scanning

Monsoon manages the lifecycle of your objects (Beans). To tell Monsoon to manage a class, use one of the stereotype annotations.

### Core Stereotypes
- `@Component`: The generic annotation for any framework-managed class.
- `@Service`: A specialization of `@Component` used to indicate that a class holds business logic.
- `@Controller` / `@RestController`: Specialized components used primarily by the `monsoon-web` module to handle HTTP requests.

**Example:**
```java
import org.monsoon.framework.core.annotations.Service;

@Service
public class UserService {
    public void createUser() {
        System.out.println("User created!");
    }
}
```

### Component Scanning
The `@ComponentScan` annotation tells the framework where to look for the above stereotypes. By default, placing it on your main class will scan the package of the main class and all its sub-packages.

---

## 3. Dependency Injection

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

---

## 4. Java-based Configuration

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
*Note: Beans created this way are singletons by default and will be available for `@Autowired` injection in other components.*

---

## 5. Properties & Environment

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
*The framework automatically converts the string value from the properties file into the appropriate field type (e.g., parsing strings into integers).*

---

## 6. Auto-Configuration System

One of Monsoon's most powerful features is its Auto-Configuration engine, which allows the framework (and external libraries) to automatically configure beans based on the classpath environment.

### Enabling Auto-Configuration
To use auto-configuration, use the `@EnableAutoConfiguration` annotation.

### The `imports` File
Auto-configuration classes are not discovered via standard component scanning. Instead, they must be explicitly registered.
Create a file at `META-INF/org.monsoon.framework.core.AutoConfiguration.imports` in your resources folder. Add the fully qualified names of your auto-configuration classes, one per line.

```text
# META-INF/org.monsoon.framework.core.AutoConfiguration.imports
org.monsoon.framework.web.autoconfigure.DefaultHttpConverterAutoConfiguration
org.monsoon.framework.web.autoconfigure.DefaultViewRendererAutoConfiguration
```

### Auto-Configuration Ordering
When multiple auto-configurations are present, order often matters. You can control the topological sorting of these classes using:
- `@AutoConfigureBefore(TargetClass.class)`
- `@AutoConfigureAfter(TargetClass.class)`

```java
@Configuration
@AutoConfigureAfter(DatabaseAutoConfiguration.class)
public class CacheAutoConfiguration {
    // Beans defined here will be processed AFTER DatabaseAutoConfiguration
}
```

---

## 7. Conditional Bean Creation

To make auto-configuration smart, Monsoon provides a suite of `@Conditional` annotations. These allow beans or entire configuration classes to be registered *only* if specific conditions are met.

### Classpath Conditions
- `@ConditionalOnClass(Target.class)`: Registers the bean/config only if `Target.class` is present on the classpath.
- `@ConditionalOnMissingClass(Target.class)`: Registers the bean/config only if `Target.class` is **not** present on the classpath. *(Note: This annotation also influences the topological sort order of auto-configurations).*

### Bean Conditions
- `@ConditionalOnMissingBean(Target.class)`: Registers the bean only if no other bean of type `Target.class` has been registered yet. This is heavily used to provide default implementations that users can override.

```java
@Configuration
public class DefaultWebConfig {

    @Bean
    @ConditionalOnMissingBean(ViewRenderer.class)
    public ViewRenderer defaultViewRenderer() {
        return new BasicViewRenderer(); // Only created if the user hasn't defined their own ViewRenderer
    }
}
```

### Property Conditions
- `@ConditionalOnProperty(name = "feature.enabled", havingValue = "true")`: Registers the bean only if the specified property exists and matches the provided value.

---

## 8. The Application Context

The `ApplicationContext` is the heart of the framework. It holds all bean definitions and singletons.

### Accessing the Context
You can retrieve the context at any time using the static `getInstance()` or `getContext()` methods on the `Monsoon` class.

```java
import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.interfaces.ApplicationContext;

ApplicationContext context = Monsoon.getContext();
```

### Fetching Beans Manually
While `@Autowired` is the preferred way to get dependencies, you can fetch them programmatically:

- `context.getBean("beanName")`: Returns the bean, throws an exception if not found.
- `context.getBean("beanName", MyClass.class)`: Returns the typed bean, throws an exception if not found.
- `context.getBeanOrNull("beanName")`: Returns the bean, or `null` if it doesn't exist.
- `context.getBeanOrNull("beanName", MyClass.class)`: Returns the typed bean, or `null`.

### Advanced Context Methods
- `context.refresh()`: Re-initializes the context.
- `context.registerBeanPostProcessor(BeanPostProcessor processor)`: Allows you to hook into the bean lifecycle to modify beans before or after their initialization.

---

**End of Documentation** 
*This documentation covers the `monsoon-core` package. For web capabilities, please refer to the `monsoon-web` module documentation.*
