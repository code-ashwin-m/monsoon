package org.monsoon.framework.web.interfaces;

import org.monsoon.framework.web.ServletWebAdapter;

import javax.servlet.http.HttpServlet;

public interface EmbeddedServer {
    void start(String host, Integer port, ServletWebAdapter servlet) throws Exception;
}
