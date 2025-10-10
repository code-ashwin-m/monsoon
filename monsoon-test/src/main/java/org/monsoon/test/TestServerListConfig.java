package org.monsoon.test;

import org.monsoon.framework.core.annotations.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties( prefix = "servers" )
public class TestServerListConfig {
    private List<ServerConfig> servers;

    public List<ServerConfig> getServers() {
        return servers;
    }

    public void setServers(List<ServerConfig> servers) {
        this.servers = servers;
    }

    public static class ServerConfig {
        private String host;
        private int port;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
    }
}
