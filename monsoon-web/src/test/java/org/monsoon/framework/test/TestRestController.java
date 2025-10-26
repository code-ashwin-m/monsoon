package org.monsoon.framework.test;

import org.monsoon.framework.web.annotations.PathVariable;
import org.monsoon.framework.web.annotations.QueryParam;
import org.monsoon.framework.web.annotations.RequestMapping;
import org.monsoon.framework.web.annotations.RestController;

@RestController
public class TestRestController {
    @RequestMapping(path = "/test")
    public String test() {
        return "hello from test method";
    }

    @RequestMapping(path = "/test-path/{path}")
    public String testPath(@PathVariable("path") String path) {
        return "hello from test-path method -> " + path;
    }

    @RequestMapping(path = "/test-query")
    public String testQuery(@QueryParam("query") String query) {
        return "hello from test-query method -> " + query;
    }
}
