package com.observer;

/**
 * Created by fangqing on 11/24/17.
 */
public class Confs {
    private String ip;
    private int port;

    @Override
    public String toString() {
        return ip+port;
    }

    public Confs() {
        this.ip = "locahost";
        this.port = 1234;
    }

    public Confs(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
