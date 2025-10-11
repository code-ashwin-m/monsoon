# Monsoon Web

Monsoon Web is a web framework for Java applications. It is built on top of Monsoon Core and provides a simple way to create web applications.

# Annotations
1. @RestController
2. @RequestMapping
3. @PathVariable
4. @RequestBody
5. @RequestParam

# How to use?

1. Add the following dependency to your project:
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.20.0</version>
    <scope>compile</scope>
</dependency>
<dependency>
    <groupId>org.monsoon.framework.web</groupId>
    <artifactId>monsoon-web</artifactId>
    <version>1.0</version>
</dependency>
```

2. Create a web application:
```java
@MonsoonApplication
public class MyWebApplication {
    public static void main(String[] args) {
        ApplicationContext context = Monsoon.run(MyWebApplication.class);
        context.refresh();
    }
}
```
3. Define REST Controller
```java
@RestController
public class HelloController {

    @RequestMapping(path = "/hello/{name}", method = "GET")
    public String hello(@PathVariable("name") String name) {
        return "Hello, " + name;
    }

    @RequestMapping(path = "/user", method = "POST")
    public UserDto createUser(@RequestBody UserDto user) {
        user.setName(user.getName().toUpperCase());
        return user;
    }
}
```