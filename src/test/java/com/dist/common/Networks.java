package com.dist.common;

import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

class NetworkInterfaceNotFound extends Exception {
    public NetworkInterfaceNotFound(String message) {
        super(message);
    }
}

public class Networks {
    private final String interfaceName;
    private final NetworkInterfaceProvider networkProvider;

    public Networks(String interfaceName, NetworkInterfaceProvider networkProvider) {
        this.interfaceName = interfaceName;
        this.networkProvider = networkProvider;
    }

    public Networks(String interfaceName) {
        this(interfaceName, new NetworkInterfaceProvider());
    }

    public Networks() {
        this("");
    }

    public String hostname()  {
        try {
            return ipv4Address().getHostAddress();

        } catch (Exception e) {
          throw new RuntimeException(e);
        }
    }

    public InetAddress ipv4Address() throws UnknownHostException, NetworkInterfaceNotFound {
        return mappings().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .filter(entry -> isIpv4(entry.getValue()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(InetAddress.getLocalHost());
    }

    private boolean isIpv4(InetAddress addr) {
        return !addr.isLoopbackAddress() && !(addr instanceof Inet6Address);
    }

    private List<Map.Entry<Integer, InetAddress>> mappings() throws NetworkInterfaceNotFound {
        return interfaces().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(inetAddress -> new AbstractMap.SimpleEntry<>(entry.getKey(), inetAddress)))
                .collect(Collectors.toList());
    }

    private List<Map.Entry<Integer, List<InetAddress>>> interfaces() throws NetworkInterfaceNotFound {
        if (interfaceName.isEmpty()) {
            return networkProvider.allInterfaces();
        } else {
            return networkProvider.getInterface(interfaceName);
        }
    }
}

class NetworkInterfaceProvider {
    public List<Map.Entry<Integer, List<InetAddress>>> allInterfaces() {
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .map(iface -> new AbstractMap.SimpleEntry<Integer, List<InetAddress>>(
                            iface.getIndex(),
                            Collections.list(iface.getInetAddresses())
                    ))
                    .collect(Collectors.toList());
        } catch (SocketException e) {
            throw new RuntimeException("Failed to get network interfaces", e);
        }
    }

    public List<Map.Entry<Integer, List<InetAddress>>> getInterface(String interfaceName) throws NetworkInterfaceNotFound {
        try {
            NetworkInterface nic = NetworkInterface.getByName(interfaceName);
            if (nic == null) {
                throw new NetworkInterfaceNotFound("Network interface=" + interfaceName + " not found.");
            }
            return Collections.singletonList(
                    new AbstractMap.SimpleEntry<>(
                            nic.getIndex(),
                            Collections.list(nic.getInetAddresses())
                    )
            );
        } catch (SocketException e) {
            throw new NetworkInterfaceNotFound("Failed to get network interface: " + interfaceName);
        }
    }
}