package com.dist.simplekafka;

import java.util.Objects;

final class Broker {
    private final int id;
    private final String host; //key=value;
    private final int port;

    Broker(int id, String host, int port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }

    private Broker() { //for jackson
        this(-1, "", -1);
    }

    public int id() {
        return id;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Broker) obj;
        return this.id == that.id &&
                Objects.equals(this.host, that.host) &&
                this.port == that.port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, host, port);
    }

    @Override
    public String toString() {
        return "Broker[" +
                "id=" + id + ", " +
                "host=" + host + ", " +
                "port=" + port + ']';
    }
}
