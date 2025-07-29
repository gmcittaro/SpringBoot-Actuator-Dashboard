package com.sb.actuator.dashboard.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;

/**
 * Service for validating authorized IP addresses.
 * Implements caching to optimize performance and supports CIDR notation.
 */
@Service
public class IpValidationService {

	@Value("${security.actuator.enable:true}")
    private boolean enableFilter;

	@Value("${security.actuator.allowed-ips:127.0.0.1}")
    private List<String> allowedIps;

    private final ConcurrentMap<String, Boolean> ipCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, SubnetInfo> subnetCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeSubnets() {
        if (allowedIps != null) {
            allowedIps.forEach(this::preprocessIpOrSubnet);
        }
    }

    /**
     * Validates if an IP is authorized with caching for optimal performance.
     */
    @Cacheable(value = "ipValidation", key = "#clientIp")
    public boolean isIpAllowed(String clientIp) {
        if (!StringUtils.hasText(clientIp)) {
            return false;
        }
        
        if (!enableFilter) {
            return true;
        }
        

        // Cache check
        Boolean cached = ipCache.get(clientIp);
        if (cached != null) {
            return cached;
        }

        boolean allowed = checkIpPermission(clientIp);
        ipCache.put(clientIp, allowed);
        return allowed;
    }

    /**
     * Checks IP permissions supporting both single IPs and CIDR notation.
     */
    private boolean checkIpPermission(String clientIp) {
        if (allowedIps == null || allowedIps.isEmpty()) {
            return false;
        }

        return allowedIps.stream().anyMatch(allowedIp -> 
            isIpInRange(clientIp, allowedIp)
        );
    }

    /**
     * Verifies if an IP is in the specified range (supports CIDR).
     */
    private boolean isIpInRange(String clientIp, String allowedRange) {
        try {
            if (allowedRange.contains("/")) {
                return isIpInSubnet(clientIp, allowedRange);
            } else {
                return isExactIpMatch(clientIp, allowedRange);
            }
        } catch (Exception e) {
            // Log error without exposing security details
            return false;
        }
    }

    /**
     * Verifies exact IP match.
     */
    private boolean isExactIpMatch(String clientIp, String allowedIp) {
        return clientIp.equals(allowedIp) || 
               ("localhost".equals(allowedIp) && isLocalhost(clientIp));
    }

    /**
     * Verifies if the IP is in the specified subnet (CIDR notation).
     */
    private boolean isIpInSubnet(String clientIp, String cidr) throws UnknownHostException {
        SubnetInfo subnet = subnetCache.computeIfAbsent(cidr, this::createSubnetInfo);
        return subnet != null && subnet.contains(clientIp);
    }

    /**
     * Preprocesses IPs and subnets to optimize performance.
     */
    private void preprocessIpOrSubnet(String ipOrSubnet) {
        if (ipOrSubnet.contains("/")) {
            subnetCache.put(ipOrSubnet, createSubnetInfo(ipOrSubnet));
        }
    }

    /**
     * Creates subnet information from CIDR notation.
     */
    private SubnetInfo createSubnetInfo(String cidr) {
        try {
            String[] parts = cidr.split("/");
            if (parts.length != 2) return null;

            InetAddress network = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);
            
            return new SubnetInfo(network, prefixLength);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifies if the IP represents localhost.
     */
    private boolean isLocalhost(String ip) {
        return "127.0.0.1".equals(ip) || "::1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip);
    }

    /**
     * Clears cache for optimal memory management.
     */
    public void clearCache() {
        ipCache.clear();
    }

    /**
     * Inner class to efficiently handle subnet information.
     */
    private static class SubnetInfo {
        private final byte[] networkBytes;
        private final byte[] maskBytes;

        public SubnetInfo(InetAddress network, int prefixLength) throws UnknownHostException {
            this.networkBytes = network.getAddress();
            this.maskBytes = createMask(prefixLength);
        }

        public boolean contains(String ip) throws UnknownHostException {
            byte[] ipBytes = InetAddress.getByName(ip).getAddress();
            
            if (ipBytes.length != networkBytes.length) {
                return false;
            }

            for (int i = 0; i < ipBytes.length; i++) {
                if ((ipBytes[i] & maskBytes[i]) != (networkBytes[i] & maskBytes[i])) {
                    return false;
                }
            }
            return true;
        }

        private byte[] createMask(int prefixLength) {
            byte[] mask = new byte[4];
            int remaining = prefixLength;
            
            for (int i = 0; i < 4; i++) {
                if (remaining >= 8) {
                    mask[i] = (byte) 0xFF;
                    remaining -= 8;
                } else if (remaining > 0) {
                    mask[i] = (byte) (0xFF << (8 - remaining));
                    remaining = 0;
                } else {
                    mask[i] = 0;
                }
            }
            return mask;
        }
    }
}