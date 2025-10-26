package org.monsoon.framework.web.interfaces;

public interface Server {
    void start(int port) throws Exception;
    void stop();
    void registerController(Object controller);
}
