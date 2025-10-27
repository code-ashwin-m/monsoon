package org.monsoon.framework.web.autoconfigure;

import org.monsoon.framework.web.ServletWebAdapter;
import org.monsoon.framework.web.interfaces.EmbeddedServer;

public class DefaultEmbeddedServer implements EmbeddedServer {

    @Override
    public void start(String host, Integer port, ServletWebAdapter servlet) throws Exception {

    }
}
