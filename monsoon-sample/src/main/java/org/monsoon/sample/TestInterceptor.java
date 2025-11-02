package org.monsoon.sample;

import org.monsoon.framework.core.annotations.Component;
import org.monsoon.framework.web.interfaces.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TestInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        System.out.println("Pre Handle " + handler);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        System.out.println("Post Handle");
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        System.out.println("After Completion");
    }
}
