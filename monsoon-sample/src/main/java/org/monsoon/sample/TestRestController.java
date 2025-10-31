package org.monsoon.sample;

import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.Controller;
import org.monsoon.framework.db.DataSource;
import org.monsoon.framework.web.annotations.*;

@Controller
public class TestRestController {
    public TestRestController(){
        try {
            DataSource dataSource = Monsoon.getContext().getBean("dataSource", DataSource.class);
            System.out.println("=======================");
            System.out.println(dataSource);
            System.out.println("=======================");
        } catch (Exception e) {

        }
    }

    @RequestMapping(path = "/test")
    public String test() {
        return "template-1";
    }

    @RequestMapping(path = "/test-path/{path}")
    public String testPath(@PathVariable("path") String path) {
        return "hello from test-path method -> " + path;
    }

    @RequestMapping(path = "/test-query")
    public String testQuery(@QueryParam("query") String query) {
        return "hello from test-query method -> " + query;
    }

    @RequestMapping(path = "/user")
    public UserDto createUser(@RequestBody UserDto userDto){
        return userDto;
    }
}
