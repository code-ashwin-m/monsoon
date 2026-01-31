package org.monsoon.sample;

import org.monsoon.framework.core.Monsoon;
import org.monsoon.framework.core.annotations.Autowired;
import org.monsoon.framework.core.annotations.Controller;
import org.monsoon.framework.db.DataSource;
import org.monsoon.framework.web.ModelMap;
import org.monsoon.framework.web.annotations.*;

@Controller
public class TestRestController {
    @Autowired
    private UserService userService;

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
    public String testPath(@PathVariable("path") String path, ModelMap model) {
        model.addAttribute("name", "ashwin nambiar");
        model.addAttribute("path", path);
        return "template-2";
    }

    @RequestMapping(path = "/test-query")
    public String testQuery(@QueryParam("query") String query) {
        return "template-3";
    }

    @RequestMapping(path = "/user", method = "POST")
    @ResponseBody
    public UserDto createUser(@RequestBody UserDto userDto){
        System.out.println(userDto);
        userService.create(userDto);
        return userDto;
    }
}
