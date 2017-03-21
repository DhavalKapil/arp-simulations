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

}
