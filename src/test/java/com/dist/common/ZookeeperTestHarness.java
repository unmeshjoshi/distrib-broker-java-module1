package com.dist.common;

import com.dist.simplekafka.ZookeeperClient;
import org.I0Itec.zkclient.ZkClient;
import org.junit.After;
import org.junit.Before;

import java.util.Collections;

public class ZookeeperTestHarness {
    protected final String zkConnectAddress =
            "127.0.0.1:" + TestUtils.choosePort();
    //choose random port to allow multiple tests to run together

    protected final int zkConnectionTimeout = 10000;
    protected final int zkSessionTimeout = 15000;

    protected EmbeddedZookeeper zookeeper;
    protected ZkClient zkClient;
    protected ZookeeperClient zookeeperClient;
    protected Config config;


    @Before
    public void setUp() throws Exception {
        zookeeper = new EmbeddedZookeeper(zkConnectAddress);
        zkClient = new ZkClient(zkConnectAddress, zkSessionTimeout, zkConnectionTimeout, new ZKStringSerializer());
        config = testConfig();
        zookeeperClient = new ZookeeperClient(config);

    }

    @After
    public void tearDown() {
        zkClient.close();
        zookeeper.shutdown();
    }

    protected Config testConfig() {
        return new Config(1, new Networks().hostname(),
                TestUtils.choosePort(), zkConnectAddress,
                Collections.singletonList(TestUtils.tempDir().getAbsolutePath()));
    }

}
