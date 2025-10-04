package org.ashwin.testInjection;

import org.ashwin.monsoon.core.annotations.Component;
import org.ashwin.monsoon.core.annotations.Service;

@Service
public class UserService {
    public String getUser() {
        return "User1";
    }
}
