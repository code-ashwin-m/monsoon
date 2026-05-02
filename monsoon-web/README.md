# Monsoon Framework - Web Module (`monsoon-web`)

Welcome to the **Monsoon Framework Web Module**. The `monsoon-web` package is a lightweight, Servlet-based web MVC and REST framework. It provides robust capabilities for HTTP request routing, static resource serving, request interception, and automatic serialization/deserialization for REST APIs.

This highly detailed guide covers everything you need to know to build web applications and RESTful services using the Monsoon framework.

---

## Table of Contents

1. [Web Context Initialization](#1-web-context-initialization)
2. [Controllers and Routing](#2-controllers-and-routing)
3. [Request Parameters & Payload Binding](#3-request-parameters--payload-binding)
4. [Response Handling & Views](#4-response-handling--views)
5. [Static Resources Serving](#5-static-resources-serving)
6. [Interceptors & Filters](#6-interceptors--filters)
7. [Pluggable Converters & Renderers](#7-pluggable-converters--renderers)

---

## 1. Web Context Initialization

When you start your application using `Monsoon.run()`, the framework automatically detects the presence of the `monsoon-web` module and initializes an `ApplicationContextFromWebClass`. 

This context registers the core `Dispatcher` and the `ServletWebAdapter`, which act as the bridge between the standard Java Servlet container (like Tomcat or Jetty) and the Monsoon framework.

*Note: You do not need to configure web.xml manually. The framework uses the Servlet 3.0 API to automatically register the necessary Servlets on startup.*

---

## 2. Controllers and Routing

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

---

## 3. Request Parameters & Payload Binding

The `Dispatcher` dynamically binds HTTP request data to your controller method parameters using specific annotations.

### `@PathVariable`
Extracts values directly from the URI path. The path in `@RequestMapping` must contain a variable enclosed in `{}`.

```java
@RequestMapping(path = "/api/users/{id}", method = "GET")
public User getUserById(@PathVariable("id") Long id) {
    return userService.findById(id);
}
```
*The framework automatically converts the string from the URI into the parameter's type (e.g., String to Long, Integer, Double).*

### `@QueryParam`
Extracts values from the query string (e.g., `/api/users?type=admin`).

```java
@RequestMapping(path = "/api/users", method = "GET")
public List<User> getUsersByType(@QueryParam("type") String userType) {
    return userService.findByType(userType);
}
```

### `@RequestBody`
Reads the HTTP request body and deserializes it into a Java object using the configured `HttpMessageConverter` (by default, Jackson).

```java
@RequestMapping(path = "/api/users", method = "POST")
public APIResponse createUser(@RequestBody UserDto userDto) {
    userService.create(userDto);
    return new APIResponse("success");
}
```

---

## 4. Response Handling & Views

The return type of your controller method dictates the response body.

### JSON Responses (`@ResponseBody`)
If a method is annotated with `@ResponseBody` (or if the class is annotated with `@RestController`), the `Dispatcher` will intercept the returned object, convert it to a JSON string using the `HttpMessageConverter`, and set the `Content-Type` header to `application/json`.

### View Rendering (HTML)
If you are returning HTML views (e.g., using FreeMarker or Thymeleaf), omit the `@ResponseBody` annotation. Instead, accept a `ModelMap` in your method parameters, add data to it, and return the name of the view as a `String`.

```java
import org.monsoon.framework.web.ModelMap;

@Controller
public class HomeController {

    @RequestMapping(path = "/home", method = "GET")
    public String renderHomePage(ModelMap model) {
        model.addAttribute("title", "Welcome to Monsoon");
        return "index"; // The ViewRenderer will resolve this to a template
    }
}
```

---

## 5. Static Resources Serving

The `Dispatcher` includes built-in support for serving static files (HTML, CSS, JS, Images, etc.) directly from the classpath without requiring specific controller mappings.

**How it works:**
If an incoming request does not match any `@RequestMapping` route, the Dispatcher attempts to find a matching file in the following classpath directories (in order):

1. `/static`
2. `/public`
3. `/META-INF/static`
4. `/META-INF/public`

**Examples:**
- A request to `/css/style.css` will be served if a file exists at `src/main/resources/static/css/style.css`.
- A request to the root path `/` will automatically attempt to serve `/index.html` from these static directories before falling back to controller mappings.

MIME types are automatically detected and applied based on the file extension (e.g., `text/css`, `image/png`, `application/javascript`, `application/font-woff2`).

---

## 6. Interceptors & Filters

Monsoon allows you to intercept HTTP requests and add pre- or post-processing logic.

### `HandlerInterceptor`
Implement the `HandlerInterceptor` interface to wrap controller execution. This is extremely useful for authentication, logging, and authorization checks.

```java
import org.monsoon.framework.web.interfaces.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.monsoon.framework.web.RequestHandler;

public class AuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, RequestHandler handler) throws Exception {
        if (req.getHeader("Authorization") == null) {
            res.setStatus(401);
            return false; // Halts execution
        }
        return true; // Continues to the controller
    }
}
```

### `WebConfigurer`
To register your interceptors and Servlet filters, create a configuration class that implements `WebConfigurer` and override the registry methods.

```java
import org.monsoon.framework.core.annotations.Configuration;
import org.monsoon.framework.web.interfaces.WebConfigurer;

@Configuration
public class MyWebConfig implements WebConfigurer {
    
    @Override
    public void addInterceptors(List<HandlerInterceptor> registry) {
        registry.add(new AuthInterceptor());
    }
}
```

---

## 7. Pluggable Converters & Renderers

The framework is designed to be extensible. By default, it uses auto-configured Jackson and FreeMarker components, but you can override them easily.

### `HttpMessageConverter`
Responsible for JSON serialization/deserialization. If you wish to use GSON instead of Jackson, simply define a Bean that implements `HttpMessageConverter`. The `@ConditionalOnMissingBean` logic in the auto-configuration will step aside and use your implementation.

### `ViewRenderer`
Responsible for rendering HTML templates. You can provide your own implementation of the `ViewRenderer` interface by exposing it as a Bean.

```java
@Bean
public ViewRenderer myCustomViewRenderer() {
    return new ThymeleafViewRenderer();
}
```

---

**End of Documentation**
*This documentation covers the `monsoon-web` package. For core framework mechanics or database capabilities, refer to the `monsoon-core` and `monsoon-db` module documentation respectively.*
