package com.dist.common;

import com.dist.net.InetAddressAndPort;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonSerDesTest {

    @Test
    public void serializesInetAddressAndPort() {
        InetAddressAndPort inetAddressAndPort = InetAddressAndPort.create("127.0.0.1", TestUtils.choosePort());
        byte[] s = JsonSerDes.serialize(inetAddressAndPort);
        assertEquals(inetAddressAndPort, JsonSerDes.deserialize(s,
                InetAddressAndPort.class));
    }

}