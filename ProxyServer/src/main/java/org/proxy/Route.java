package org.proxy;

public class Route {
    private String keyword;
    public Route(){}
    public Route(String keyword, String service, Integer port, String replace) {
        this.keyword = keyword;
        this.service = service;
        this.port = port;
        this.replace = replace;
    }

    private String service;
    private Integer port;
    private String replace;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getReplace() {
        return replace;
    }

    public void setReplace(String replace) {
        this.replace = replace;
    }
}
