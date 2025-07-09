# Participant Assignments: Kafka Group Membership Fundamentals

## **Course Overview**

This course focuses on the **fundamental concepts** of Kafka group membership using ZooKeeper for distributed
coordination. Participants will learn core distributed systems patterns through hands-on implementation of broker
discovery, registration, and change notification systems.

## **Assignment 1: Disk Performance Analysis** ✅ *Already Implemented*

### **Objective**

Understand storage I/O impact on distributed systems and why durability guarantees affect performance.

### **Background**

Distributed systems like Kafka must balance performance with durability. Understanding disk I/O characteristics is
crucial for designing high-performance distributed systems that can guarantee data persistence.

### **Tasks**

1. **Run Performance Tests**:
   ```bash
   ./gradlew run --args="com.dist.perf.DiskWritePerformanceTest"
   ```

2. **Analyze Different Scenarios**:
    - Run tests with and without `fsync()` calls
    - Compare buffered vs unbuffered writes
    - Measure throughput degradation with durability guarantees

3. **Performance Analysis**:
    - Calculate writes per second and MB/s throughput
    - Analyze the relationship between durability and performance
    - Document the trade-offs between speed and data safety

### **Learning Outcomes**

- Understand I/O bottlenecks in distributed systems
- Learn trade-offs between performance and durability
- Analyze system performance characteristics under load
- Apply performance measurement techniques

---

## **Assignment 2: Queuing Theory Analysis** ✅ *Already Implemented*

### **Objective**

Apply queuing theory and mathematical models to understand system performance characteristics and predict behavior under
load.

### **Background**

Distributed systems behavior can be modeled using queuing theory. Understanding Little's Law, M/M/1 queues, and the
Universal Scalability Law helps predict system performance and identify bottlenecks.

### **Tasks**

1. **Queuing Theory Analysis**:
   ```bash
   cd src/main/python
   python queuing_theory.py
   ```

2. **Performance Visualization**:
   ```bash
   python queuing_theory_visualization.py
   ```

3. **Universal Scalability Law**:
   ```bash
   python universal_scalability_law.py
   ```

4. **Custom Analysis**:
    - Modify service rates and analyze different scenarios
    - Compare ideal vs realistic system behavior
    - Identify system saturation points

5. **Real-World Application**:
    - Apply these models to Kafka broker scenarios
    - Predict performance under different load patterns
    - Design capacity planning recommendations

### **Learning Outcomes**

- Understand Little's Law and queue behavior
- Learn to predict system performance under varying loads
- Analyze scalability limits using mathematical models
- Apply theoretical concepts to practical system design

## **Assignment 3: Implement Broker Registration** ✅ *Partially Implemented*

### **Objective**

Complete and enhance the ZooKeeper-based broker registration system with robust error handling, retry logic, and
comprehensive monitoring.

### **Background**

Service registration is a fundamental pattern in distributed systems. Brokers must register themselves in ZooKeeper
using ephemeral nodes that automatically clean up when the broker fails.

### **Current Implementation Status**

Nede to complete the following tasks:

- Complete the existing `registerBroker()` method by creating an ephemeral node.

#### **3.1 Optional - Enhanced Error Handling**

Improve the existing `registerBroker()` method:

```java
public void registerBroker(Broker broker) {
    int maxRetries = 3;
    int retryDelayMs = 1000;
    
    for (int attempt = 0; attempt < maxRetries; attempt++) {
        try {
            String brokerValue = JsonSerDes.toJson(broker);
            String brokerKey = getBrokerPath(broker.id());
            
            // TODO: Handle ZkNodeExistsException appropriately
            // TODO: Add exponential backoff for retries
            // TODO: Log detailed error information
            
            createEphemeralPath(zkClient, brokerKey, brokerValue);
            
            // TODO: Update local cache atomically
            // TODO: Notify registered listeners
            // TODO: Log successful registration
            
            return; // Success
            
        } catch (ZkNodeExistsException e) {
            // TODO: Handle broker ID conflicts
        } catch (Exception e) {
            // TODO: Implement retry logic with backoff
            if (attempt == maxRetries - 1) {
                throw new RuntimeException("Broker registration failed after " + maxRetries + " attempts", e);
            }
        }
    }
}
```

### **Learning Outcomes**

- Understand ephemeral nodes and ZooKeeper session management
- Learn distributed system failure patterns and recovery strategies
- Implement robust error handling and retry mechanisms
- Design monitoring and observability for distributed services

---

## **Assignment 4: Advanced Broker Change Notifications** ✅ *Partially Implemented*

### **Objective**

Implement an event-driven system for detecting and handling broker membership changes with efficient change detection
and proper cleanup.

### **Background**

In distributed systems, components must react to membership changes efficiently. This requires implementing the "
list-and-watch" pattern to avoid race conditions and ensure eventual consistency.

### **Tasks**

#### **4.1 Enhanced Change Listener**

Implement a sophisticated broker change listener:

```java
public class BrokerChangeListener implements IZkChildListener {
    private static final Logger logger = Logger.getLogger(BrokerChangeListener.class);
    
    private final Map<String, Broker> activeBrokers = new ConcurrentHashMap<>();
    private final List<BrokerMembershipObserver> observers = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    @Override
    public void handleChildChange(String parentPath, List<String> currentChildren) {
        lock.writeLock().lock();
        try {
            Set<String> currentBrokerIds = new HashSet<>(currentChildren != null ? currentChildren : Collections.emptyList());
            Set<String> previousBrokerIds = new HashSet<>(activeBrokers.keySet());
            
            // TODO: Detect broker additions
            Set<String> addedBrokers = new HashSet<>(currentBrokerIds);
            addedBrokers.removeAll(previousBrokerIds);
            
            // TODO: Detect broker removals
            Set<String> removedBrokers = new HashSet<>(previousBrokerIds);
            removedBrokers.removeAll(currentBrokerIds);
            
            // TODO: Handle additions
            for (String brokerId : addedBrokers) {
                handleBrokerAdded(brokerId);
            }
            
            // TODO: Handle removals
            for (String brokerId : removedBrokers) {
                handleBrokerRemoved(brokerId);
            }
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private void handleBrokerAdded(String brokerId) {
        // TODO: Fetch broker information
        // TODO: Update local cache
        // TODO: Notify observers
        // TODO: Log the addition
    }
    
    private void handleBrokerRemoved(String brokerId) {
        // TODO: Remove from local cache
        // TODO: Notify observers
        // TODO: Clean up any resources
        // TODO: Log the removal
    }
}
```

#### **4.2 Observer Pattern Implementation**

Create an observer system for broker changes:

```java
public interface BrokerMembershipObserver {
    void onBrokerAdded(Broker broker);
    void onBrokerRemoved(Broker broker);
    void onMembershipChanged(Set<Broker> currentBrokers);
}

public class BrokerMembershipManager {
    // TODO: Implement observer registration and notification
    // TODO: Handle observer failures gracefully
    // TODO: Provide current membership snapshot
}
```

#### **4.3 Race Condition Prevention**

Implement the "list-and-watch" pattern:

- Get initial broker list before setting up watches
- Handle the race condition between listing and watching
- Ensure consistency between local cache and ZooKeeper state

#### **4.4 Comprehensive Testing**

Create extensive tests:

- Test broker addition/removal scenarios
- Test rapid membership changes
- Test network partitions and reconnections
- Test observer notification reliability

### **Learning Outcomes**

- Learn to handle dynamic membership changes efficiently
- Implement observer patterns in distributed systems
- Handle race conditions and consistency issues
- Design resilient distributed system components

---

## **Course Timeline and Assessment**

### **Recommended Learning Path**

**Week 1: Foundation & Performance Analysis**

- **Assignment 1**: Disk Performance Analysis
- **Assignment 2**: Queuing Theory Analysis
- Focus: Understanding system performance characteristics

**Week 2: Distributed Coordination**

- **Assignment 3**: Enhanced Broker Registration
- **Assignment 4**: Advanced Broker Change Notifications
- Focus: ZooKeeper-based coordination and event-driven architecture

## **Learning Outcomes**

By completing these four assignments, participants will demonstrate:

### **Core Distributed Systems Concepts**

- **Service Discovery**: How distributed components find each other
- **Membership Management**: Handling dynamic cluster membership
- **Failure Detection**: Recognizing and responding to component failures
- **Event-Driven Architecture**: Reactive programming patterns

### **ZooKeeper Coordination Patterns**

- **Ephemeral Nodes**: Automatic cleanup and failure detection
- **Watches**: Event notification mechanisms
- **Session Management**: Connection lifecycle and recovery

### **Performance Engineering**

- **I/O Analysis**: Understanding storage bottlenecks
- **Queuing Theory**: Modeling system behavior under load
- **Scalability Analysis**: Predicting system limits
- **Performance Measurement**: Systematic benchmarking approaches

---

### **Debugging Tips**

- **ZooKeeper Issues**: Check embedded ZooKeeper startup in test logs
- **Connection Problems**: Verify port availability and firewall settings
- **Performance Variations**: Run tests multiple times for consistent results
- **Build Issues**: Clear Gradle cache with `./gradlew clean build`

---

### **Essential Reading**

- **"Designing Data-Intensive Applications"** by Martin Kleppmann (Chapters 5, 8, 9)
- **"Patterns Of Distributed Systems"** by Unmesh Joshi and Martin Fowler (Chapter 1 & 2)
- **Apache Kafka Documentation**: https://kafka.apache.org/documentation/
- **Apache ZooKeeper Guide**: https://zookeeper.apache.org/doc/current/recipes.html