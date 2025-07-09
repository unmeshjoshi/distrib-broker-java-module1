package com.dist.simplekafka;

import com.dist.common.TestUtils;
import com.dist.common.ZookeeperTestHarness;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

//Assignment3 - Implement RegisterBroker method
//Assignment4 - Implement BrokerChangeListener.

public class ZookeeperClientTest extends ZookeeperTestHarness {
    //start testcontainer with zookeeper
    @Test
    public void registersBroker() {
        Broker broker = new Broker(1, "10.10.10.10", 8000);
        zookeeperClient.registerBroker(broker);
        Broker brokerInfo = zookeeperClient.getBrokerInfo(1);
        assertEquals(broker, brokerInfo);
    }

    @Test
    public void testSubscribeBrokerChangeListener() {
        List<String> brokerIds = new ArrayList<>();
        //TODO: implement IZkChildListener interface
//        zookeeperClient.subscribeBrokerChangeListener(new IZkChildListener() {
//            @Override
//            public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
//                brokerIds.clear();
//                brokerIds.addAll(currentChildren);
//            }
//        });

        zookeeperClient.registerBroker(new Broker(1, "10.10.10.10", 8000));

        // assignment: subscribe to broker change listener

        TestUtils.waitUntilTrue(() -> {
            return brokerIds.size() == 1;
        }, "Waiting for getting broker added notification");

        assertEquals("1", brokerIds.get(0));
    }

    @Test
    public void testGetAllBrokers() {
        zookeeperClient.registerSelf();
        Set<Broker> brokers = zookeeperClient.getAllBrokers();
        assertEquals(1, brokers.size());
        Broker broker = brokers.iterator().next();
        assertEquals(config.getBrokerId(), broker.id());
        assertEquals(config.getHostName(), broker.host());
        assertEquals(config.getPort(), broker.port());
    }

    @Test
    public void testGetBrokerInfo() {
        zookeeperClient.registerSelf();
        Broker broker = zookeeperClient.getBrokerInfo(config.getBrokerId());
        assertEquals(config.getBrokerId(), broker.id());
        assertEquals(config.getHostName(), broker.host());
        assertEquals(config.getPort(), broker.port());
    }
}