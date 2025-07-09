# Broker Demo with ZooKeeper - Step by Step Guide

This guide demonstrates how to run the broker registration demo using ZooKeeper and shows how to inspect the ZooKeeper
data structure.

## Prerequisites

- Docker installed and running
- Java 8 or higher
- Gradle (included in the project)

## Step 1: Start ZooKeeper in Docker

First, start a ZooKeeper container:

```bash
docker run --rm --name zookeeper-demo -p 2181:2181 zookeeper:3.8.1
```

This command:

- `--rm`: Automatically remove the container when it stops
- `--name zookeeper-demo`: Name the container for easy reference
- `-p 2181:2181`: Map port 2181 from container to host
- `zookeeper:3.8.1`: Use ZooKeeper version 3.8.1

**Note**: Keep this terminal window open. The ZooKeeper server will continue running.

## Step 2: Start Broker Applications

Open **new terminal windows** for each broker. You can run multiple brokers simultaneously to see the watch mechanism in
action.

### Terminal 2: Start Broker 1

```bash
./gradlew run --args="localhost:2181 1"
```

### Terminal 3: Start Broker 2

```bash
./gradlew run --args="localhost:2181 2"
```

### Terminal 4: Start Broker 3 (optional)

```bash
./gradlew run --args="localhost:2181 3"
```

## Step 3: Inspect ZooKeeper Data Structure

Open another terminal window to inspect what's happening in ZooKeeper:

### Connect to ZooKeeper Container

```bash
docker exec -it zookeeper-demo /bin/bash
```

### Navigate to ZooKeeper CLI

```bash
cd bin
./zkCli.sh
```

### Inspect Broker Data

Once in the ZooKeeper CLI, you can inspect the broker registrations:

```bash
# List all brokers in the cluster
ls /brokers/ids

# Get detailed information about broker 1
get /brokers/ids/1

# Get detailed information about broker 2
get /brokers/ids/2

# Get detailed information about broker 3 (if running)
get /brokers/ids/3

# List all children recursively
ls -R /brokers/ids
```

## Expected Output

### When you run `ls /brokers/ids`:

```
[1, 2, 3]
```

### When you run `get /brokers/ids/1`:

```
{"id":1,"host":"192.168.1.100","port":9093}
```

### When you run `ls -R /brokers/ids`:

```
/brokers/ids
/brokers/ids/1
/brokers/ids/2
/brokers/ids/3
```

## Understanding the Data Structure

- **Path**: `/brokers/ids/{brokerId}`
- **Node Type**: Ephemeral (automatically deleted when broker disconnects)
- **Data Format**: JSON containing broker information
  ```json
  {
    "id": 1,
    "host": "192.168.1.100",
    "port": 9093
  }
  ```

## Watch Mechanism Demonstration

1. **Start Broker 1** - You'll see it register in ZooKeeper
2. **Start Broker 2** - Broker 1 will receive a notification about the new broker
3. **Stop Broker 2** (Ctrl+C) - Broker 1 will receive a notification about the broker leaving
4. **Check ZooKeeper** - The ephemeral node for Broker 2 will be automatically removed

## Broker Application Output

When you start brokers, you'll see output like:

```
=== Broker Demo Application ===
ZooKeeper Address: localhost:2181
Broker ID: 1
================================
Registering broker 1 with ZooKeeper...
✓ Broker 1 registered successfully!

=== CURRENT CLUSTER STATE ===
Total brokers in cluster: 1
  Broker[id=1, host=192.168.1.100, port=9093]
=============================

Demo application is running...
This broker will stay registered and watch for membership changes.
Press Ctrl+C to exit.
You can start other brokers in separate terminals to see watch events.
Example: java BrokerApp localhost:2181 2
✓ Broker 1 is still alive and watching...
```

When another broker joins, you'll see:

```
=== BROKER MEMBERSHIP CHANGE DETECTED ===
Parent Path: /brokers/ids
Current Brokers: [1, 2]

Detailed Broker Information:
  Broker 1: Broker[id=1, host=192.168.1.100, port=9093]
  Broker 2: Broker[id=2, host=192.168.1.100, port=9094]
========================================
```

## Cleanup

To stop the demo:

1. **Stop all broker applications**: Press `Ctrl+C` in each broker terminal
2. **Stop ZooKeeper**: Press `Ctrl+C` in the ZooKeeper terminal
3. **Remove container**: The `--rm` flag will automatically remove the container

## Troubleshooting

### Port Already in Use

If you get "port already in use" error:

```bash
# Check what's using port 2181
lsof -i :2181

# Kill the process if needed
kill -9 <PID>
```

### Docker Container Issues

If the ZooKeeper container fails to start:

```bash
# Check Docker logs
docker logs zookeeper-demo

# Remove existing container
docker rm -f zookeeper-demo

# Start fresh
docker run --rm --name zookeeper-demo -p 2181:2181 zookeeper:3.8.1
```

### Connection Refused

If brokers can't connect to ZooKeeper:

1. Make sure ZooKeeper is running: `docker ps | grep zookeeper-demo`
2. Check if port 2181 is accessible: `telnet localhost 2181`
3. Verify ZooKeeper is ready: `echo ruok | nc localhost 2181` (should return "imok")

## Key Concepts Demonstrated

1. **Ephemeral Nodes**: Broker registrations are ephemeral - they disappear when the broker disconnects
2. **Watch Events**: Brokers receive notifications when cluster membership changes
3. **Distributed Coordination**: Multiple brokers can discover each other through ZooKeeper
4. **Failure Detection**: When a broker crashes or disconnects, other brokers are notified
5. **Session Management**: ZooKeeper handles session expiration and reconnection automatically

## Next Steps

After running this demo, you can explore:

1. **ZooKeeper CLI Commands**: Try other commands like `stat`, `set`, `delete`
2. **Multiple ZooKeeper Instances**: Set up a ZooKeeper ensemble for high availability
3. **Custom Broker Logic**: Modify the broker application to add more functionality
4. **Integration with Kafka**: Use this pattern as a foundation for a Kafka-like system 