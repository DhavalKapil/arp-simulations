package com.secarp.protocol.secarp;

import java.util.HashMap;
import java.util.Map;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;

/**
 * Represents information corresponding to a sequence number
 */
public class SequenceNumberEntry {
    // Ip address of the target node
    private Ipv4Address ipv4Address;

    // Time after which all replies will be treated as invalid
    private int expirationTime;

    // Map from Mac address to its count
    private HashMap<MacAddress, Integer> macCountMap;

    /**
     * Constructor function
     */
    public SequenceNumberEntry(Ipv4Address ipv4Address,
                               int expirationTime
                               ) {
        this.ipv4Address = ipv4Address;
        this.expirationTime = expirationTime;
        this.macCountMap = new HashMap<>();
    }

    /**
     * Getter and setter functions
     */
    public Ipv4Address getIpv4Address() {
        return this.ipv4Address;
    }

    public void setIpv4Address(Ipv4Address ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    public int getExpirationTime() {
        return this.expirationTime;
    }

    public void setExpirationTime(int expirationTime) {
        this.expirationTime = expirationTime;
    }

    public HashMap<MacAddress, Integer> getMacCountMap() {
        return this.macCountMap;
    }

    public void setMacCountMap(HashMap<MacAddress, Integer> macCountMap) {
        this.macCountMap = macCountMap;
    }

    /**
     * Adds/Updates a new Mac/Count entry
     *
     * @param macAddress
     */
    public void updateMacCount(MacAddress macAddress) {
        this.macCountMap.putIfAbsent(macAddress, 0);
        // Increments existing entry
        this.macCountMap.put(macAddress, this.macCountMap.get(macAddress) + 1);
    }

    /**
     * Returns Mac Address with maximum count
     */
    public MacAddress getMacAddressWithMaxCount() {
        Map.Entry<MacAddress, Integer> maxEntry = null;
        System.out.println("Contents of macCountMap:");
        for (Map.Entry<MacAddress, Integer> entry: this.macCountMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
            if (maxEntry == null || entry.getValue().
                compareTo(maxEntry.getValue())> 0) {
                maxEntry = entry;
            }
        }
        System.out.println();
        if (maxEntry == null) {
            return null;
        }
        return maxEntry.getKey();
    }

    /**
     * Returns the size of the mac to count map
     */
    public int getMacCountMapSize() {
        return this.macCountMap.size();
    }

    /**
     * Checks for conflict
     * If more than 1 MAC appears, conflict has arised
     *
     * @return Whether conflict arised or not
     */
    public boolean conflict() {
        return this.macCountMap.size() != 1;
    }
}
