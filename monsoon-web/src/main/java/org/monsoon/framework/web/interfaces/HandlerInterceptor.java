package org.monsoon.framework.web.interfaces;

import org.monsoon.framework.web.RequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerInterceptor {
    default boolean preHandle(HttpServletRequest req, HttpServletResponse res, RequestHandler handler) throws Exception{
        return true;
    }

    default void postHandle(HttpServletRequest req, HttpServletResponse res, RequestHandler handler) throws Exception{
    }

    default void afterCompletion(HttpServletRequest req, HttpServletResponse res, RequestHandler handler) throws Exception{
    }
}
