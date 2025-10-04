package org.ashwin.monsoon.config.test1;

import org.ashwin.monsoon.config.annotations.ConfigurationProperties;

@ConfigurationProperties(prefix = "server")
public class ServerConfig {
    private String host;
    private int port;
    private SslConfig ssl;   // nested object

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public SslConfig getSsl() { return ssl; }
    public void setSsl(SslConfig ssl) { this.ssl = ssl; }

    public static class SslConfig {
        private boolean enabled;
        private String keyStore;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getKeyStore() { return keyStore; }
        public void setKeyStore(String keyStore) { this.keyStore = keyStore; }
    }
}
