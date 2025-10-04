package org.ashwin.monsoon.config.test1;

import org.ashwin.monsoon.config.annotations.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "servers")
public class ServerListConfig {
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
        private List<String> tag;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }

        public List<String> getTag() {
            return tag;
        }

        public void setTag(List<String> tag) {
            this.tag = tag;
        }
    }
}
