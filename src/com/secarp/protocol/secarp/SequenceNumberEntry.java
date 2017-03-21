package com.secarp.protocol.secarp;

import com.secarp.address.Ipv4Address;
import com.secarp.address.MacAddress;

import java.util.HashMap;

/**
 * Represents information corresponding to a sequence number
 */
public class SequenceNumberEntry {
    //Ip address of the target node
    private Ipv4Address ipv4Address;

    //Time after which all replies will be treated as invalid
    private int expirationTime;

    //Map from Mac address to its count
    private HashMap<MacAddress, Integer> macCountMap;

    /**
     * Constructor function
     */
    public SequenceNumberEntry(Ipv4Address ipv4Address,
                               int expirationTime,
                               HashMap<MacAddress, Integer> macCountMap
                               ) {
        this.ipv4Address = ipv4Address;
        this.expirationTime = expirationTime;
        this.macCountMap = macCountMap;
    }
    /**
     * Getter and setter functions
     */
    public Ipv4Address getIpv4Address() { return this.ipv4Address; }

    public void setIpv4Address(Ipv4Address ipv4Address) { this.ipv4Address = ipv4Address; }

    public int getExpirationTime() { return this.expirationTime; }

    public void setExpirationTime(int expirationTime) { this.expirationTime = expirationTime; }

    public HashMap<MacAddress, Integer> getMacCountMap() { return this.macCountMap; }

    public void setMacCountMap(HashMap<MacAddress, Integer> macCountMap) { this.macCountMap = macCountMap; }
}
