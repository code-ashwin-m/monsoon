# Monsoon Web

Monsoon Web is a web framework for Java applications. It is built on top of Monsoon Core and provides a simple way to create web applications.

# Annotations
1. @RestController
2. @RequestMapping
3. @PathVariable
4. @RequestBody
5. @QueryParam

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
import java.util.ArrayList;

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

    @RequestMapping(path = "/comments", method = "GET")
    public ApiResponse httpComments(@QueryParam("postId") Integer postId) {
        List<CommentDto> result = new ArrayList<>();
        return ApiResponse.success(result);
    }

    @RequestMapping(path = "/comments/{postId}", method = "GET")
    public ApiResponse httpComments1(@PathVariable("postId") Integer postId) {
        List<CommentDto> result = new ArrayList<>();
        return ApiResponse.success(result);
    }
}
```

3. HTTP Client Service

Annotations
1. @HttpService(
2. @RequestMapping
3. @QueryParam
4. @Header

```java
@HttpService( baseUrl = "https://jsonplaceholder.typicode.com")
public interface UserClient {
    @RequestMapping(path = "/todos/1", method = "GET")
    ClientDto httpSimple();

    @RequestMapping(path = "/comments", method = "GET")
    List<CommentDto> httpList(@QueryParam("postId") Integer postId);

    @RequestMapping(path = "/posts", method = "POST")
    Post createPost(Post post, @Header("X-Auth-Token") String token);
}

public class Main {
    public static void main(String[] args) {
        UserClient userClient = HttpServiceProxy.create(UserClient.class);
        ClientDto dto = userClient.httpSimple();
        List<CommentDto> comments = userClient.httpList(1);
    }
}
```