package org.ashwin.testInjection;

import org.ashwin.monsoon.core.annotations.Component;
import org.ashwin.monsoon.core.annotations.Inject;

@Component
public class GreetingService {
    @Inject
    private UserService userService;

    public String getGreeting() {
        return "Hello " + userService.getUser();
    }
}
